package gd.gov.demo.service.impl;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gd.gov.demo.common.ClusterEnum;
import gd.gov.demo.common.TemplateFusingRequestEnum;
import gd.gov.demo.request.DestinationRuleRequest;
import gd.gov.demo.request.FusingPolicy;
import gd.gov.demo.request.NfcRequest;
import gd.gov.demo.request.TemplateFusingRequest;
import gd.gov.demo.request.VirtualServiceRequest;
import gd.gov.demo.service.IstioService;
import gd.gov.demo.service.KubeService;
import gd.gov.demo.util.BackendIstioClientUtil;
import gd.gov.demo.util.DatabaseIstioClientUtil;
import io.fabric8.istio.api.networking.v1alpha3.ConnectionPoolSettings;
import io.fabric8.istio.api.networking.v1alpha3.ConnectionPoolSettingsBuilder;
import io.fabric8.istio.api.networking.v1alpha3.DestinationRule;
import io.fabric8.istio.api.networking.v1alpha3.DestinationRuleBuilder;
import io.fabric8.istio.api.networking.v1alpha3.EnvoyFilter;
import io.fabric8.istio.api.networking.v1alpha3.EnvoyFilterApplyTo;
import io.fabric8.istio.api.networking.v1alpha3.EnvoyFilterBuilder;
import io.fabric8.istio.api.networking.v1alpha3.EnvoyFilterPatchContext;
import io.fabric8.istio.api.networking.v1alpha3.EnvoyFilterPatchOperation;
import io.fabric8.istio.api.networking.v1alpha3.Gateway;
import io.fabric8.istio.api.networking.v1alpha3.LoadBalancerSettingsSimple;
import io.fabric8.istio.api.networking.v1alpha3.LoadBalancerSettingsSimpleLB;
import io.fabric8.istio.api.networking.v1alpha3.Subset;
import io.fabric8.istio.api.networking.v1alpha3.TrafficPolicy;
import io.fabric8.istio.api.networking.v1alpha3.TrafficPolicyBuilder;
import io.fabric8.istio.api.networking.v1alpha3.VirtualService;
import io.fabric8.istio.api.networking.v1alpha3.VirtualServiceBuilder;
import io.fabric8.istio.api.networking.v1alpha3.DestinationRuleSpecFluent.SubsetsNested;
import io.fabric8.istio.api.networking.v1alpha3.LoadBalancerSettingsFluent.LoadBalancerSettingsSimpleLbPolicyNested;
import io.fabric8.istio.api.networking.v1alpha3.TrafficPolicyFluent.ConnectionPoolNested;
import io.fabric8.istio.api.networking.v1alpha3.TrafficPolicyFluent.OutlierDetectionNested;
import io.fabric8.istio.api.networking.v1alpha3.VirtualServiceSpecFluent.HttpNested;
import io.fabric8.istio.api.networking.v1alpha3.VirtualServiceFluent.SpecNested;
import io.fabric8.istio.client.IstioClient;
import io.fabric8.kubernetes.api.model.apps.Deployment;

@Service
public class IstioServiceImpl implements IstioService{
    @Autowired
    private KubeService kubeService = new KubeServiceImpl();

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
        if (clusterId == null || !clusterId.equals(1)) return false;    // 方法默认对server集群生效
        
        List<Deployment> deployments = kubeService.getDeployments(clusterId, "default");
        if (deployments == null || deployments.isEmpty()) return false;
        Integer totalReplicas = 0;
        for (Deployment deployment : deployments) {
            totalReplicas += deployment.getSpec().getReplicas();
        }
        
        String vsName = deployments.get(0).getSpec().getSelector().getMatchLabels().get("app");

        HttpNested<SpecNested<VirtualServiceBuilder>> var1 = new VirtualServiceBuilder()
                                                                .withNewMetadata()
                                                                .withName(vsName)
                                                                .endMetadata()
                                                                .withNewSpec()
                                                                    .addToHosts("*")
                                                                    .addToGateways(vsName + "-gateway")
                                                                    .addNewHttp().addNewMatch().withPort(443).endMatch();

        Integer portNumber = 8088;
        Integer totalWeight = 100;
        for (int i = 0; i < deployments.size(); ++i) {
            Integer weight = null;
            // 计算服务子集的流量权重比例
            if (i != deployments.size() - 1) {
                weight = 100 * deployments.get(i).getSpec().getReplicas() / totalReplicas;
            } else {
                weight = totalWeight;
            }
            totalWeight -= weight;
            
            var1.addNewRoute()
                    .withNewDestination()
                        .withHost(vsName)
                        .withSubset(deployments.get(i).getSpec().getSelector().getMatchLabels().get("version"))
                        .withNewPort(portNumber)
                    .endDestination()
                    .withWeight(weight)
                .endRoute();
        }
       
        VirtualService vs = var1.endHttp().endSpec().build();
        BackendIstioClientUtil.getClient().v1alpha3().virtualServices().inNamespace("default").resource(vs).createOrReplace();
        return true;
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
        String globalLoadBalancerPolicy = request.getGlobalLoadBalancer();
        if (globalLoadBalancerPolicy != null && !globalLoadBalancerPolicy.isEmpty()) {
            var1.withNewTrafficPolicy()
                    .withNewLoadBalancer()
                        .withNewLoadBalancerSettingsSimpleLbPolicy()
                            .withSimple(LoadBalancerSettingsSimpleLB.fromValue(globalLoadBalancerPolicy))
                        .endLoadBalancerSettingsSimpleLbPolicy()
                    .endLoadBalancer()
                .endTrafficPolicy();
        }
        
        List<FusingPolicy> policies = request.getPolicies();
        if (policies != null && !policies.isEmpty()) {
            var1.withSubsets();
            for (FusingPolicy policy : policies) {
                TrafficPolicyBuilder trafficPolicyBuilder = new TrafficPolicyBuilder();
                if (policy.getConnectionPool() != null) {
                    FusingPolicy.ConnectionPool connectionPool = policy.getConnectionPool();
                    ConnectionPoolSettingsBuilder cpsBuilder = new ConnectionPoolSettingsBuilder();
                    if (connectionPool.getTcp_connectTimeout() != null && connectionPool.getTcp_maxConnections() != null) {
                        cpsBuilder.withNewTcp()
                                    .withMaxConnections(connectionPool.getTcp_maxConnections())
                                    .withConnectTimeout(connectionPool.getTcp_connectTimeout() + "ms")
                                    .withNewTcpKeepalive()
                                        .withTime("7200s")
                                        .withInterval("75s")
                                    .endTcpKeepalive()
                                    .endTcp();
                    }

                    if (connectionPool.getHttp_http1MaxPendingRequests() != null && connectionPool.getHttp_maxRequestPerConnection() != null) {
                        io.fabric8.istio.api.networking.v1alpha3.ConnectionPoolSettingsFluent.HttpNested<ConnectionPoolSettingsBuilder> var2 = cpsBuilder.withNewHttp()
                                                            .withHttp1MaxPendingRequests(connectionPool.getHttp_http1MaxPendingRequests())
                                                            .withMaxRequestsPerConnection(connectionPool.getHttp_maxRequestPerConnection());
                        if (connectionPool.getHttp_maxRetries() != null) var2.withMaxRetries(connectionPool.getHttp_maxRetries());
                        var2.endHttp();
                    }

                    ConnectionPoolSettings connectionPoolSettings = cpsBuilder.build();
                    if (connectionPoolSettings.getHttp() != null || connectionPoolSettings.getTcp() != null) {
                        trafficPolicyBuilder.withConnectionPool(connectionPoolSettings);
                    }
                }

                if (policy.getOutlierDetection() != null && !policy.getOutlierDetection().isEmpty()) {
                    FusingPolicy.OutlierDetection outlierDetection = policy.getOutlierDetection();
                    OutlierDetectionNested<TrafficPolicyBuilder> var3 = trafficPolicyBuilder.withNewOutlierDetection();
                    if (outlierDetection.getConsecutiveErrors() != null) var3.withConsecutiveErrors(outlierDetection.getConsecutiveErrors());
                    if (outlierDetection.getConsecutive5xxErrors() != null) var3.withConsecutive5xxErrors(outlierDetection.getConsecutive5xxErrors());
                    if (outlierDetection.getInterval() != null) var3.withInterval(outlierDetection.getInterval() + "s");
                    if (outlierDetection.getBaseEjectionTime() != null) var3.withBaseEjectionTime(outlierDetection.getBaseEjectionTime() + "s");
                    if (outlierDetection.getMaxEjectionPercent() != null) var3.withMaxEjectionPercent(outlierDetection.getMaxEjectionPercent());
                    var3.endOutlierDetection();
                }

                if (policy.getLoadBalancer() != null && !policy.getLoadBalancer().isEmpty()) {
                    trafficPolicyBuilder.withNewLoadBalancer()
                                            .withNewLoadBalancerSettingsSimpleLbPolicy()
                                                .withSimple(LoadBalancerSettingsSimpleLB.fromValue(policy.getLoadBalancer()))
                                            .endLoadBalancerSettingsSimpleLbPolicy()
                                        .endLoadBalancer();
                }

                TrafficPolicy trafficPolicy = trafficPolicyBuilder.build();
                SubsetsNested<io.fabric8.istio.api.networking.v1alpha3.DestinationRuleFluent.SpecNested<DestinationRuleBuilder>> var2 = var1.addNewSubset()
                                                                                                                            .withName(policy.getVersion())
                                                                                                                            .addToLabels("version", policy.getVersion());
                if (trafficPolicy != null && (trafficPolicy.getConnectionPool() != null || 
                    trafficPolicy.getOutlierDetection() != null || trafficPolicy.getLoadBalancer() != null)) {
                    var2.withTrafficPolicy(trafficPolicy);
                }
                var2.endSubset();
            }
        }

        DestinationRule dr = var1.endSpec().build();
        client.v1alpha3().destinationRules().inNamespace(request.getNamespace()).resource(dr).createOrReplace();
        return true;
    }

    @Override
    public boolean applyTemplateDestinationRule(Integer clusterId) {
        if (clusterId == null || !clusterId.equals(1)) return false;    // 方法默认对server集群生效
        
        List<Deployment> deployments = kubeService.getDeployments(clusterId, "default");
        if (deployments == null || deployments.isEmpty()) return false;

        String drName = deployments.get(0).getSpec().getSelector().getMatchLabels().get("app");
        io.fabric8.istio.api.networking.v1alpha3.DestinationRuleFluent.SpecNested<DestinationRuleBuilder> var1 = 
                    new DestinationRuleBuilder().withNewMetadata().withName(drName).endMetadata().withNewSpec().withHost(drName).withSubsets();
        
        for (Deployment deployment : deployments) {
            String version = deployment.getSpec().getSelector().getMatchLabels().get("version");
            var1.addNewSubset().withName(version).addToLabels("version", version).endSubset();
        }

        DestinationRule dr = var1.endSpec().build();
        IstioClient client = BackendIstioClientUtil.getClient();
        client.v1alpha3().destinationRules().inNamespace("default").resource(dr).createOrReplace();
        return true;
    }

    @Override
    public boolean applyTemplateFusingPolicy(TemplateFusingRequest request) {
        DestinationRule dr = getDestinationRule(request.getClusterId());
        if (dr == null) return false;

        List<Subset> subsets = dr.getSpec().getSubsets();
        for (int i = 0 ; i < subsets.size(); ++i) {
            Subset subset = subsets.get(i);
            if (subset.getLabels().getOrDefault("version", "").equals(request.getVersion())) {
                TemplateFusingRequestEnum fusingEnum = TemplateFusingRequestEnum.getByLevel(request.getLevel());
                if (fusingEnum == null) return false;

                FusingPolicy.ConnectionPool connectionPool = fusingEnum.getConnectionPool();
                FusingPolicy.OutlierDetection outlierDetection = fusingEnum.getOutlierDetection();

                TrafficPolicyBuilder trafficPolicyBuilder = new TrafficPolicyBuilder().withNewConnectionPool()
                                                                .withNewTcp()
                                                                    .withMaxConnections(connectionPool.getTcp_maxConnections())
                                                                    .withConnectTimeout(connectionPool.getTcp_connectTimeout() + "ms")
                                                                .endTcp()
                                                                .withNewHttp()
                                                                    .withHttp1MaxPendingRequests(connectionPool.getHttp_http1MaxPendingRequests())
                                                                    .withMaxRetries(connectionPool.getHttp_maxRetries())
                                                                .endHttp()
                                                            .endConnectionPool()
                                                            .withNewOutlierDetection()
                                                                .withConsecutiveErrors(outlierDetection.getConsecutiveErrors())
                                                                .withConsecutive5xxErrors(outlierDetection.getConsecutive5xxErrors())
                                                                .withBaseEjectionTime(outlierDetection.getBaseEjectionTime() + "s")
                                                                .withInterval(outlierDetection.getInterval() + "s")
                                                                .withMaxEjectionPercent(outlierDetection.getMaxEjectionPercent())
                                                            .endOutlierDetection();

                TrafficPolicy trafficPolicy = trafficPolicyBuilder.build();
                if (subset.getTrafficPolicy() != null && subset.getTrafficPolicy().getLoadBalancer() != null) {
                    trafficPolicy.setLoadBalancer(subset.getTrafficPolicy().getLoadBalancer());
                }
                
                subset.setTrafficPolicy(trafficPolicy);
                subsets.set(i, subset);
            }
        }
        dr.getSpec().setSubsets(subsets);

        IstioClient istioClient = BackendIstioClientUtil.getClient();
        istioClient.v1alpha3().destinationRules().inNamespace("default").resource(dr).createOrReplace();  
        return true;
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