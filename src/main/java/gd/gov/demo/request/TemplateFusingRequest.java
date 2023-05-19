package gd.gov.demo.request;

import lombok.Data;

@Data
public class TemplateFusingRequest {
    private Integer clusterId;

    private String version;

    private Integer level;

    public TemplateFusingRequest() { }
    public TemplateFusingRequest(Integer clusterId, String version, Integer level) {
        this.clusterId = clusterId;
        this.version = version;
        this.level = level;
    }
}
