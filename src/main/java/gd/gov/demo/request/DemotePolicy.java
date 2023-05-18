package gd.gov.demo.request;

import lombok.Data;

@Data
public class DemotePolicy {
    /**
     * 相应的服务子集
     */
    private String version;

    /**
     * 是否开启熔断
     */
    private Boolean state;

    /**
     * tcp最大连接数
     */
    private Integer tcp_maxConnections;

    /**
     * tcp连接超时时间，单位默认为ms
     */
    private Integer tcp_connectTimeout;

    /**
     * http1最大排队请求数
     */
    private Integer http_http1MaxPendingRequests;

    /**
     * http连接单个连接能发出的最大请求数量
     */
    private Integer http_maxRequestPerConnection;

    /**
     * 熔断异常检测中的连续5xx错误码数量
     */
    private Integer consecutive5xxErrors;

    /**
     * 局部负载均衡策略，默认为0表示不开启即使用全局策略，{@link gd.gov.demo.common.DestinationRuleLoadBalancerEnum}
     */
    private Integer loadBalancer;

    public DemotePolicy() { }
    public DemotePolicy(String version, Boolean state, Integer tcp_maxConnections, Integer tcp_connectTimeout, Integer http_http1MaxPendingRequests, Integer http_maxRequestPerConnection, Integer consecutive5xxErrors, Integer loadBalancer) {
        this.version = version;
        this.state = state;
        this.tcp_maxConnections = tcp_maxConnections;
        this.tcp_connectTimeout = tcp_connectTimeout;
        this.http_http1MaxPendingRequests = http_http1MaxPendingRequests;
        this.http_maxRequestPerConnection = http_maxRequestPerConnection;
        this.consecutive5xxErrors = consecutive5xxErrors;
        this.loadBalancer = loadBalancer;
    }

    public DemotePolicy(String version, Boolean state) {
        this(version, state, null, null, null, null, null, 0);
    }

}
