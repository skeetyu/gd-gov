package gd.gov.demo.request;

import java.util.Map;

import lombok.Data;

@Data
public class NfcRequest {
    /**
     * 集群id
     */
    private Integer clusterId;

    /**
     * envoy filter名称
     */
    private String efName;

    /**
     * 标记是否开启限流
     */
    private Boolean state;

    /**
     * workload selector
     */
    private Map<String, String> labels;

    /**
     * 最大令牌数
     */
    private Integer max_tokens;

    /**
     * 每次填充的令牌数量
     */
    private Integer tokens_per_fill;

    public NfcRequest() { }
    public NfcRequest(Integer clusterId, String efName, Boolean state, Map<String, String> labels, Integer max_tokens, Integer tokens_per_fill) {
        this.clusterId = clusterId;
        this.efName = efName;
        this.state = state;
        this.labels = labels;
        this.max_tokens = max_tokens;
        this.tokens_per_fill = tokens_per_fill;
    }
}
