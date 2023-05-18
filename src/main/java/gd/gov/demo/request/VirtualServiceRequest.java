package gd.gov.demo.request;

import java.util.List;

import lombok.Data;

@Data
public class VirtualServiceRequest{
    /**
     * 集群编号 {@link gd.gov.demo.common.ClusterEnum}
     */
    private Integer clusterId;

    /**
     * 名称空间
     */
    private String namespace;

    /**
     * virtual service名称
     */
    private String vsName;

    /**
     * 服务子集
     */
    private List<String> versions;

    /**
     * 负载均衡权重值
     */
    private List<Integer> weights;

    public VirtualServiceRequest() { }
    public VirtualServiceRequest(Integer clusterId, String namespace, String vsName, List<String> versions, List<Integer> weights) {
        this.clusterId = clusterId;
        this.namespace = namespace;
        this.vsName = vsName;
        this.versions = versions;
        this.weights = weights;
    }
}
