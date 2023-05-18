package gd.gov.demo.service;

import java.util.List;

import gd.gov.demo.request.DestinationRuleRequest;
import gd.gov.demo.request.NfcRequest;
import gd.gov.demo.request.VirtualServiceRequest;
import io.fabric8.istio.api.networking.v1alpha3.DestinationRule;
import io.fabric8.istio.api.networking.v1alpha3.EnvoyFilter;
import io.fabric8.istio.api.networking.v1alpha3.Gateway;
import io.fabric8.istio.api.networking.v1alpha3.VirtualService;

public interface IstioService {
    /**
     * 获取Gateway信息，项目默认只存在一个gateway
     * @param clusterId 集群id {@link gd.gov.demo.common.ClusterEnum}
     * @return Gateway
     */
    public Gateway getGateway(Integer clusterId);

    /**
     * 获取VirtualService信息，项目默认只存在一个vs
     * @param clusterId 集群id
     * @return VirtualService
     */
    public VirtualService getVirtualService(Integer clusterId);

    /**
     * 生效Virtual Service负载均衡配置
     * @param request   vs配置请求
     * @return 是否成功生效
     */
    public boolean applyVirtualService(VirtualServiceRequest request);

    /**
     * 生效Virtual Service模板配置
     * @param clusterId 集群id
     * @return 是否成功生效
     */
    public boolean applyTemplateVirtualService(Integer clusterId);

    /**
     * 获取DestinationRule信息，项目默认只存在一个dr
     * @param clusterId 集群id
     * @return DestinationRule
     */
    public DestinationRule getDestinationRule(Integer clusterId);

    /**
     * 生效Destination Rule配置，包含负载均衡与熔断策略
     * @param request   dr配置请求
     * @return 是否成功生效
     */
    public boolean applyDestinationRule(DestinationRuleRequest request);

    /**
     * 生效Destination Rule模板配置
     * @param clusterId 集群id
     * @return 是否成功生效
     */
    public boolean applyTemplateDestinationRule(Integer clusterId);

    /**
     * 获取envoy filter列表
     * @param clusterId 集群id
     * @return EnvoyFilter列表
     */
    public List<EnvoyFilter> getEnvoyFilters(Integer clusterId);

    /**
     * 获取指定envoy filter
     * @param clusterId 集群id
     * @param efName    envoy filter名称
     * @return EnvoyFilter
     */
    public EnvoyFilter getEnvoyFilterByName(Integer clusterId, String efName);

    /**
     * 生效envoy filter配置，包含令牌桶参数
     * @param request   限流配置请求
     * @return 是否成功生效
     */
    public boolean applyEnvoyFilter(NfcRequest request);
}
