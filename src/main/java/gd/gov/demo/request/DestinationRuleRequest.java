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
     * 全局负载均衡配置，为空表示不开启
     */
    private String globalLoadBalancer;

    /**
     * 每个服务子集配置的熔断策略
     */
    private List<FusingPolicy> policies;

    public DestinationRuleRequest() { }
    public DestinationRuleRequest(Integer clusterId, String namespace, String drName, String globalLoadBalancer, List<FusingPolicy> policies) {
        this.clusterId = clusterId;
        this.namespace = namespace;
        this.drName = drName;
        this.globalLoadBalancer = globalLoadBalancer;
        this.policies = policies;
    }
}
