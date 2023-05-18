package gd.gov.demo.service.impl;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import gd.gov.demo.common.ClusterEnum;
import gd.gov.demo.common.DestinationRuleLoadBalancerEnum;
import gd.gov.demo.request.DemotePolicy;
import gd.gov.demo.request.DestinationRuleRequest;
import gd.gov.demo.request.NfcRequest;
import gd.gov.demo.request.VirtualServiceRequest;
import gd.gov.demo.service.IstioService;
import gd.gov.demo.util.BackendIstioClientUtil;
import gd.gov.demo.util.DatabaseIstioClientUtil;
import io.fabric8.istio.api.networking.v1alpha3.DestinationRule;
import io.fabric8.istio.api.networking.v1alpha3.DestinationRuleBuilder;
import io.fabric8.istio.api.networking.v1alpha3.EnvoyFilter;
import io.fabric8.istio.api.networking.v1alpha3.EnvoyFilterApplyTo;
import io.fabric8.istio.api.networking.v1alpha3.EnvoyFilterBuilder;
import io.fabric8.istio.api.networking.v1alpha3.EnvoyFilterPatchContext;
import io.fabric8.istio.api.networking.v1alpha3.EnvoyFilterPatchOperation;
import io.fabric8.istio.api.networking.v1alpha3.Gateway;
import io.fabric8.istio.api.networking.v1alpha3.LoadBalancerSettingsSimpleLB;
import io.fabric8.istio.api.networking.v1alpha3.VirtualService;
import io.fabric8.istio.api.networking.v1alpha3.VirtualServiceBuilder;
import io.fabric8.istio.api.networking.v1alpha3.VirtualServiceSpecFluent.HttpNested;
import io.fabric8.istio.api.networking.v1alpha3.VirtualServiceFluent.SpecNested;
import io.fabric8.istio.client.IstioClient;

@Service
public class IstioServiceImpl implements IstioService{
    @Override
    public Gateway getGateway(Integer clusterId) {
        Gateway gateway = null;
        
        ClusterEnum cluster = ClusterEnum.getById(clusterId);
        if (cluster.equals(ClusterEnum.Backend)) {
            gateway = getGateway(BackendIstioClientUtil.getClient());
        } else if (cluster.equals(ClusterEnum.Database)) {
            gateway = getGateway(DatabaseIstioClientUtil.getClient());
        } else {
            return null;
        }

        return gateway;
    }
    
    /**
     * 获取gateway
     */
    private Gateway getGateway(IstioClient client) {
        List<Gateway> gateways = client.v1alpha3().gateways().list().getItems();
        if (gateways != null && !gateways.isEmpty()) return gateways.get(0);
        else return null;
    }

    @Override
    public VirtualService getVirtualService(Integer clusterId) {
        VirtualService vs = null;
        ClusterEnum cluster = ClusterEnum.getById(clusterId);
        if (cluster.equals(ClusterEnum.Backend)) {
            vs = getVirtualService(BackendIstioClientUtil.getClient());
        } else if (cluster.equals(ClusterEnum.Database)) {
            vs = getVirtualService(DatabaseIstioClientUtil.getClient());
        } else {
            return null;
        }

        return vs;
    }

    /**
     * 获取virtual service
     */
    private VirtualService getVirtualService(IstioClient client) {
        List<VirtualService> virtualServices = client.v1alpha3().virtualServices().list().getItems();
        if (virtualServices != null && !virtualServices.isEmpty()) return virtualServices.get(0);
        else return null;
    }

    @Override
    public boolean applyVirtualService(VirtualServiceRequest request) {
        ClusterEnum cluster = ClusterEnum.getById(request.getClusterId());
        if (cluster.equals(ClusterEnum.Backend)) {
            return applyVirtualService(BackendIstioClientUtil.getClient(), request);
        } else if (cluster.equals(ClusterEnum.Database)) {
            return applyVirtualService(DatabaseIstioClientUtil.getClient(), request);
        } else {
            return false;
        }
    }

    /**
     * 生效virtual service配置
     */
    private boolean applyVirtualService(IstioClient client, VirtualServiceRequest request) {
        String vsName = request.getVsName();
        client.v1alpha3().virtualServices().inNamespace(request.getNamespace()).withName(vsName).delete();
        HttpNested<SpecNested<VirtualServiceBuilder>> routes = new VirtualServiceBuilder()
                                            .withNewMetadata()
                                            .withName(vsName)
                                            .endMetadata()
                                            .withNewSpec()
                                                .addToHosts("*")
                                                .addToGateways(vsName + "-gateway")
                                                .addNewHttp()
                                                    .addNewMatch().withPort(443).endMatch();
        for (int i = 0; i < request.getVersions().size(); ++i) {
            routes.addNewRoute()
                .withNewDestination()
                    .withHost(vsName)
                    .withSubset(request.getVersions().get(i))
                    .withNewPort(8088)
                .endDestination()
                .withWeight(request.getWeights().get(i))
                .endRoute();
        }
        VirtualService vs = routes.endHttp().endSpec().build();
        client.v1alpha3().virtualServices().inNamespace(request.getNamespace()).resource(vs).createOrReplace();
        return true;
    }

    public boolean applyTemplateVirtualService(Integer clusterId) {
        ClusterEnum cluster = ClusterEnum.getById(clusterId);
        if (cluster.equals(ClusterEnum.Backend)) {
            IstioClient client = BackendIstioClientUtil.getClient();
            String vsName = "server";
            VirtualService vs = new VirtualServiceBuilder()
                                .withNewMetadata()
                                .withName(vsName)
                                .endMetadata()
                                .withNewSpec()
                                    .addToHosts("*")
                                    .addToGateways(vsName + "-gateway")
                                    .addNewHttp()
                                        .addNewMatch().withPort(443).endMatch()
                                        .addNewRoute()
                                            .withNewDestination()
                                                .withHost(vsName)
                                                .withSubset("v1")
                                                .withNewPort()
                                                    .withNumber(8088)
                                                .endPort()
                                            .endDestination()
                                            .withWeight(40)
                                        .endRoute()
                                        .addNewRoute()
                                            .withNewDestination()
                                                .withHost(vsName)
                                                .withSubset("v2")
                                                .withNewPort()
                                                    .withNumber(8088)
                                                .endPort()
                                            .endDestination()
                                            .withWeight(20)
                                        .endRoute()
                                        .addNewRoute()
                                            .withNewDestination()
                                                .withHost(vsName)
                                                .withSubset("v3")
                                                .withNewPort()
                                                    .withNumber(8088)
                                                .endPort()
                                            .endDestination()
                                            .withWeight(40)
                                        .endRoute()
                                    .endHttp()
                                .endSpec()
                                .build();
            client.v1alpha3().virtualServices().inNamespace("default").resource(vs).createOrReplace();
            return true;
        } else if (cluster.equals(ClusterEnum.Database)) {
            IstioClient client = DatabaseIstioClientUtil.getClient();
            String vsName = "mysql";
            VirtualService vs = new VirtualServiceBuilder()
                                .withNewMetadata()
                                .withName(vsName)
                                .endMetadata()
                                .withNewSpec()
                                    .addToHosts("*")
                                    .addToGateways(vsName + "-gateway")
                                    .addNewHttp()
                                        .addNewRoute()
                                            .withNewDestination()
                                                .withHost(vsName)
                                                .withSubset("v1")
                                                .withNewPort()
                                                    .withNumber(3306)
                                                .endPort()
                                            .endDestination()
                                        .endRoute()
                                        .addNewRoute()
                                            .withNewDestination()
                                                .withHost(vsName)
                                                .withSubset("v2")
                                                .withNewPort()
                                                    .withNumber(3306)
                                                .endPort()
                                            .endDestination()
                                        .endRoute()
                                        .addNewRoute()
                                            .withNewDestination()
                                                .withHost(vsName)
                                                .withSubset("v3")
                                                .withNewPort()
                                                    .withNumber(3306)
                                                .endPort()
                                            .endDestination()
                                        .endRoute()
                                    .endHttp()
                                .endSpec()
                                .build();
            client.v1alpha3().virtualServices().inNamespace("default").resource(vs).createOrReplace();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public DestinationRule getDestinationRule(Integer clusterId) {
        DestinationRule dr = null;
        ClusterEnum cluster = ClusterEnum.getById(clusterId);
        if (cluster.equals(ClusterEnum.Backend)) {
            dr = getDestinationRule(BackendIstioClientUtil.getClient());
        } else if (cluster.equals(ClusterEnum.Database)) {
            dr = getDestinationRule(DatabaseIstioClientUtil.getClient());
        } else {
            return null;
        }

        return dr;
    }

    /**
     * 获取destination rule
     */
    private DestinationRule getDestinationRule(IstioClient client) {
        List<DestinationRule> destinationRules = client.v1alpha3().destinationRules().list().getItems();
        if (destinationRules != null && !destinationRules.isEmpty()) return destinationRules.get(0);
        else return null;
    }

    @Override
    public boolean applyDestinationRule(DestinationRuleRequest request) {
        ClusterEnum cluster = ClusterEnum.getById(request.getClusterId());
        if (cluster.equals(ClusterEnum.Backend)) {
            return applyDestinationRule(BackendIstioClientUtil.getClient(), request);
        } else if (cluster.equals(ClusterEnum.Database)) {
            return applyDestinationRule(DatabaseIstioClientUtil.getClient(), request);
        } else {
            return false;
        }
    }

    /**
     * 生效destination rule配置
     */
    private boolean applyDestinationRule(IstioClient client, DestinationRuleRequest request) {
        String drName = request.getDrName();
        io.fabric8.istio.api.networking.v1alpha3.DestinationRuleFluent.SpecNested<DestinationRuleBuilder> var1 = new DestinationRuleBuilder()
                                                                                                                        .withNewMetadata()
                                                                                                                            .withName(drName)
                                                                                                                        .endMetadata()
                                                                                                                        .withNewSpec()
                                                                                                                            .withHost(drName);
        Integer globalLoadBalancerPolicy = request.getGlobalLoadBalancer();
        if (globalLoadBalancerPolicy != null && !globalLoadBalancerPolicy.equals(0)) {
            DestinationRuleLoadBalancerEnum globalLoadBalancer = DestinationRuleLoadBalancerEnum.getByPolicy(globalLoadBalancerPolicy);
            var1.withNewTrafficPolicy()
                    .withNewLoadBalancer()
                        .withNewLoadBalancerSettingsSimpleLbPolicy()
                            .withSimple(LoadBalancerSettingsSimpleLB.fromValue(globalLoadBalancer.name()))
                        .endLoadBalancerSettingsSimpleLbPolicy()
                    .endLoadBalancer()
                .endTrafficPolicy();
        }
        List<DemotePolicy> policies = request.getPolicies();
        if (policies != null && !policies.isEmpty()) {
            var1.withSubsets();
            for (int i = 0; i < policies.size(); ++i) {
                DemotePolicy demotePolicy = policies.get(i);
                DestinationRuleLoadBalancerEnum localLoadBalancer = DestinationRuleLoadBalancerEnum.getByPolicy(demotePolicy.getLoadBalancer());
                if (demotePolicy.getState()) {
                    var1.addNewSubset()
                            .withName(demotePolicy.getVersion())
                            .addToLabels("version", demotePolicy.getVersion())
                            .withNewTrafficPolicy()
                                .withNewConnectionPool()
                                    .withNewTcp()
                                        .withMaxConnections(demotePolicy.getTcp_maxConnections())
                                        .withConnectTimeout(String.valueOf(demotePolicy.getTcp_connectTimeout()) + "ms")
                                        .withNewTcpKeepalive()
                                            .withTime("7200s")
                                            .withInterval("75s")
                                        .endTcpKeepalive()
                                    .endTcp()
                                    .withNewHttp()
                                        .withHttp1MaxPendingRequests(demotePolicy.getHttp_http1MaxPendingRequests())
                                        .withMaxRequestsPerConnection(demotePolicy.getHttp_maxRequestPerConnection())
                                    .endHttp()
                                .endConnectionPool()
                                .withNewOutlierDetection()
                                    .withConsecutive5xxErrors(demotePolicy.getConsecutive5xxErrors())
                                    .withInterval("1s")
                                    .withBaseEjectionTime("3m")
                                    .withMaxEjectionPercent(100)
                                .endOutlierDetection()
                                .withNewLoadBalancer()
                                    .withNewLoadBalancerSettingsSimpleLbPolicy()
                                        .withSimple(LoadBalancerSettingsSimpleLB.fromValue(localLoadBalancer.name()))
                                    .endLoadBalancerSettingsSimpleLbPolicy()
                                .endLoadBalancer()
                            .endTrafficPolicy()
                        .endSubset();
                } else if (demotePolicy.getLoadBalancer() != null && !demotePolicy.getLoadBalancer().equals(0)) {
                    var1.addNewSubset()
                            .withName(demotePolicy.getVersion())
                            .addToLabels("version", demotePolicy.getVersion())
                            .withNewTrafficPolicy()
                                .withNewLoadBalancer()
                                    .withNewLoadBalancerSettingsSimpleLbPolicy()
                                        .withSimple(LoadBalancerSettingsSimpleLB.fromValue(localLoadBalancer.name()))
                                    .endLoadBalancerSettingsSimpleLbPolicy()
                                .endLoadBalancer()
                            .endTrafficPolicy()
                        .endSubset();
                } else {
                    var1.addNewSubset()
                        .withName(demotePolicy.getVersion())
                        .addToLabels("version", demotePolicy.getVersion())
                        .endSubset();
                }
            }
        }

        DestinationRule dr = var1.endSpec().build();
        client.v1alpha3().destinationRules().inNamespace(request.getNamespace()).resource(dr).createOrReplace();
        return true;
    }

    @Override
    public boolean applyTemplateDestinationRule(Integer clusterId) {
        ClusterEnum cluster = ClusterEnum.getById(clusterId);
        if (cluster.equals(ClusterEnum.Backend)) {
            IstioClient client = BackendIstioClientUtil.getClient();
            String drName = "server";
            
            DestinationRule dr = new DestinationRuleBuilder()
                                    .withNewMetadata()
                                        .withName(drName)
                                    .endMetadata()
                                    .withNewSpec()
                                        .withHost(drName)
                                        .withNewTrafficPolicy()
                                            .withNewLoadBalancer()
                                                .withNewLoadBalancerSettingsSimpleLbPolicy()
                                                    .withSimple(LoadBalancerSettingsSimpleLB.ROUND_ROBIN)
                                                .endLoadBalancerSettingsSimpleLbPolicy()
                                            .endLoadBalancer()
                                        .endTrafficPolicy()
                                        .withSubsets()
                                        .addNewSubset()
                                            .withName("v1")
                                            .addToLabels("version", "v1")
                                            // .withNewTrafficPolicy()
                                            //     .withNewConnectionPool()
                                            //         .withNewTcp()
                                            //             .withMaxConnections(5)
                                            //             .withConnectTimeout("30ms")
                                            //             .withNewTcpKeepalive()
                                            //                 .withTime("7200s")
                                            //                 .withInterval("75s")
                                            //             .endTcpKeepalive()
                                            //         .endTcp()
                                            //         .withNewHttp()
                                            //             .withHttp1MaxPendingRequests(5)
                                            //             .withMaxRequestsPerConnection(5)
                                            //         .endHttp()
                                            //     .endConnectionPool()
                                            //     .withNewOutlierDetection()
                                            //         .withConsecutive5xxErrors(1)
                                            //         .withInterval("1s")
                                            //         .withBaseEjectionTime("3m")
                                            //         .withMaxEjectionPercent(100)
                                            //     .endOutlierDetection()
                                            // .endTrafficPolicy()
                                        .endSubset()
                                        .addNewSubset()
                                            .withName("v2")
                                            .addToLabels("version", "v2")
                                            // .withNewTrafficPolicy()
                                            //     .withNewConnectionPool()
                                            //         .withNewTcp()
                                            //             .withMaxConnections(5)
                                            //             .withConnectTimeout("30ms")
                                            //             .withNewTcpKeepalive()
                                            //                 .withTime("7200s")
                                            //                 .withInterval("75s")
                                            //             .endTcpKeepalive()
                                            //         .endTcp()
                                            //         .withNewHttp()
                                            //             .withHttp1MaxPendingRequests(5)
                                            //             .withMaxRequestsPerConnection(5)
                                            //         .endHttp()
                                            //     .endConnectionPool()
                                            //     .withNewOutlierDetection()
                                            //         .withConsecutive5xxErrors(1)
                                            //         .withInterval("1s")
                                            //         .withBaseEjectionTime("3m")
                                            //         .withMaxEjectionPercent(100)
                                            //     .endOutlierDetection()
                                            // .endTrafficPolicy()
                                        .endSubset()
                                        .addNewSubset()
                                            .withName("v3")
                                            .addToLabels("version", "v3")
                                            .withNewTrafficPolicy()
                                                .withNewConnectionPool()
                                                    .withNewTcp()
                                                        .withMaxConnections(1)
                                                        .withConnectTimeout("30ms")
                                                        .withNewTcpKeepalive()
                                                            .withTime("7200s")
                                                            .withInterval("75s")
                                                        .endTcpKeepalive()
                                                    .endTcp()
                                                    .withNewHttp()
                                                        .withHttp1MaxPendingRequests(1)
                                                        .withMaxRequestsPerConnection(1)
                                                    .endHttp()
                                                .endConnectionPool()
                                                .withNewOutlierDetection()
                                                    .withConsecutive5xxErrors(1)
                                                    .withInterval("1s")
                                                    .withBaseEjectionTime("3m")
                                                    .withMaxEjectionPercent(100)
                                                .endOutlierDetection()
                                            .endTrafficPolicy()
                                        .endSubset()
                                    .endSpec()
                                .build();
            client.v1alpha3().destinationRules().inNamespace("default").resource(dr).createOrReplace();
            return true;
        } else if (cluster.equals(ClusterEnum.Database)) {
            IstioClient client = DatabaseIstioClientUtil.getClient();
            String drName = "mysql";
            
            DestinationRule dr = new DestinationRuleBuilder()
                                    .withNewMetadata()
                                        .withName(drName)
                                    .endMetadata()
                                    .withNewSpec()
                                        .withHost(drName)
                                        .withNewTrafficPolicy()
                                            .withNewLoadBalancer()
                                                .withNewLoadBalancerSettingsSimpleLbPolicy()
                                                    .withSimple(LoadBalancerSettingsSimpleLB.ROUND_ROBIN)
                                                .endLoadBalancerSettingsSimpleLbPolicy()
                                            .endLoadBalancer()
                                        .endTrafficPolicy()
                                        .withSubsets()
                                        .addNewSubset()
                                            .withName("v1")
                                            .addToLabels("version", "v1")
                                        .endSubset()
                                        .addNewSubset()
                                            .withName("v2")
                                            .addToLabels("version", "v2")
                                        .endSubset()
                                        .addNewSubset()
                                            .withName("v3")
                                            .addToLabels("version", "v3")
                                        .endSubset()
                                    .endSpec()
                                .build();
            client.v1alpha3().destinationRules().inNamespace("default").resource(dr).createOrReplace();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<EnvoyFilter> getEnvoyFilters(Integer clusterId) {
        ClusterEnum cluster = ClusterEnum.getById(clusterId);
        if (cluster.equals(ClusterEnum.Backend)) {
            return getEnvoyFilters(BackendIstioClientUtil.getClient());
        } else if (cluster.equals(ClusterEnum.Database)) {
            return getEnvoyFilters(DatabaseIstioClientUtil.getClient());
        } else {
            return null;
        }
    }

    /**
     * 获取envoy filter列表
     */
    private List<EnvoyFilter> getEnvoyFilters(IstioClient client) {
        List<EnvoyFilter> envoyFilters = client.v1alpha3().envoyFilters().inNamespace("istio-system").list().getItems();
        List<EnvoyFilter> retList = new ArrayList<EnvoyFilter>(envoyFilters.size());

        for (EnvoyFilter ef : envoyFilters) {
            String efName = ef.getMetadata().getName();
            if (efName != null && !efName.equals("stats-filter-1.14-1-14-5") && !efName.equals("tcp-stats-filter-1.14-1-14-5")) {
                retList.add(ef);
            }
        }
        return retList;
    }

    @Override
    public EnvoyFilter getEnvoyFilterByName(Integer clusterId, String efName) {
        ClusterEnum cluster = ClusterEnum.getById(clusterId);
        if (cluster.equals(ClusterEnum.Backend)) {
            IstioClient client = BackendIstioClientUtil.getClient();
            return client.v1alpha3().envoyFilters().inNamespace("istio-system").withName(efName).get();
        } else if (cluster.equals(ClusterEnum.Database)) {
            IstioClient client = DatabaseIstioClientUtil.getClient();
            return client.v1alpha3().envoyFilters().inNamespace("istio-system").withName(efName).get();
        } else {
            return null;
        }
    }

    @Override
    public boolean applyEnvoyFilter(NfcRequest request) {
        ClusterEnum cluster = ClusterEnum.getById(request.getClusterId());
        if (cluster.equals(ClusterEnum.Backend)) {
            return applyEnvoyFilter(BackendIstioClientUtil.getClient(), request);
        } else if (cluster.equals(ClusterEnum.Database)) {
            return applyEnvoyFilter(DatabaseIstioClientUtil.getClient(), request);
        } else {
            return false;
        }
    }

    /**
     * 生效envoy filter配置
     */
    private boolean applyEnvoyFilter(IstioClient client, NfcRequest request) {
        Map<String, String> labels = new HashMap<String, String>();
        if (!request.getLabels().getOrDefault("app", "").isEmpty()) labels.put("app", request.getLabels().get("app"));
        if (!request.getLabels().getOrDefault("version", "").isEmpty()) labels.put("version", request.getLabels().get("version"));
        if (request.getState()) {
            EnvoyFilter envoyFilter = new EnvoyFilterBuilder()
                                            .withNewMetadata()
                                                .withName(request.getEfName())
                                                .withNamespace("istio-system")
                                            .endMetadata()
                                            .withNewSpec()
                                                .withNewWorkloadSelector()
                                                    .addToLabels(labels)
                                                .endWorkloadSelector()
                                                .withConfigPatches()
                                                .addNewConfigPatch()
                                                    .withApplyTo(EnvoyFilterApplyTo.HTTP_FILTER)
                                                    .withNewMatch()
                                                        .withContext(EnvoyFilterPatchContext.SIDECAR_INBOUND)
                                                        .withNewEnvoyFilterEnvoyConfigObjectMatchListenerTypes()
                                                            .withNewListener()
                                                                .withNewFilterChain()
                                                                    .withNewFilter()
                                                                        .withName("envoy.filters.network.http_connection_manager")
                                                                    .endFilter()
                                                                .endFilterChain()
                                                            .endListener()
                                                        .endEnvoyFilterEnvoyConfigObjectMatchListenerTypes()
                                                    .endMatch()
                                                    .withNewPatch()
                                                        .withOperation(EnvoyFilterPatchOperation.INSERT_BEFORE)
                                                        .addToValue("name", "envoy.filters.http.local_ratelimit")
                                                        .addToValue("typed_config", new HashMap() {{
                                                            put("@type", "type.googleapis.com/udpa.type.v1.TypedStruct");
                                                            put("type_url", "type.googleapis.com/envoy.extensions.filters.http.local_ratelimit.v3.LocalRateLimit");
                                                            put("value", new HashMap() {{
                                                                put("stat_prefix", "http_local_rate_limiter");
                                                                put("token_bucket", new HashMap() {{
                                                                    put("max_tokens", request.getMax_tokens());
                                                                    put("tokens_per_fill", request.getTokens_per_fill());
                                                                    put("fill_interval", "60s");
                                                                }});
                                                                put("filter_enabled", new HashMap() {{
                                                                    put("runtime_key", "local_rate_limit_enabled");
                                                                    put("default_value", new HashMap() {{
                                                                        put("numerator", 100);
                                                                        put("denominator", "HUNDRED");
                                                                    }});
                                                                }});
                                                                put("filter_enforced", new HashMap() {{
                                                                    put("runtime_key", "local_rate_limit_enforced");
                                                                    put("default_value", new HashMap() {{
                                                                        put("numerator", 100);
                                                                        put("denominator", "HUNDRED");
                                                                    }});
                                                                }});
                                                                put("response_headers_to_add", new ArrayList(){{
                                                                    add(new HashMap() {{
                                                                        put("append", false);
                                                                        put("header", new HashMap() {{
                                                                            put("key", "x-local-rate-limit");
                                                                            put("value", "true");
                                                                        }});
                                                                    }});
                                                                }});
                                                            }});
                                                        }})
                                                    .endPatch()
                                                .endConfigPatch()
                                            .endSpec()
                                        .build();
            client.v1alpha3().envoyFilters().inNamespace("istio-system").resource(envoyFilter).createOrReplace();
        } else {
            client.v1alpha3().envoyFilters().inNamespace("istio-system").withName(request.getEfName()).delete();
        }
        return true;
    }

}