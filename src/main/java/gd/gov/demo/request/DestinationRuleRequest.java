package gd.gov.demo.request;

import java.util.List;

import lombok.Data;

@Data
public class DestinationRuleRequest {
    /**
     * 集群编号 {@link gd.gov.demo.common.ClusterEnum}
     */
    private Integer clusterId;

    /**
     * 名称空间
     */
    private String namespace;

    /**
     * destination rule名称
     */
    private String drName;

    /**
     * 全局负载均衡配置，0表示不开启 {@link gd.gov.demo.common.DestinationRuleLoadBalancerEnum}
     */
    private Integer globalLoadBalancer;

    /**
     * 每个服务子集配置的熔断策略
     */
    private List<DemotePolicy> policies;

    public DestinationRuleRequest() { }
    public DestinationRuleRequest(Integer clusterId, String namespace, String drName, Integer globalLoadBalancer, List<DemotePolicy> policies) {
        this.clusterId = clusterId;
        this.namespace = namespace;
        this.drName = drName;
        if (this.globalLoadBalancer == null) {
            this.globalLoadBalancer = 0;
        } else {
            this.globalLoadBalancer = globalLoadBalancer;
        }
        this.policies = policies;
    }
}
