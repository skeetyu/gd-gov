package gd.gov.demo.request;

import lombok.Data;

@Data
public class ClusterIdRequest {
    private Integer clusterId;

    public ClusterIdRequest() { }
    public ClusterIdRequest(Integer clusterId) {
        this.clusterId = clusterId;
    }
}
