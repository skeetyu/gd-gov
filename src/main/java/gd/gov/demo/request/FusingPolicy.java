package gd.gov.demo.request;

import lombok.Data;

@Data
public class FusingPolicy {
    /**
     * 相应的服务子集
     */
    private String version;

    /**
     * 熔断-连接池配置
     */
    private ConnectionPool connectionPool;

    /**
     * 熔断-异常检测配置
     */
    private OutlierDetection outlierDetection;

    /**
     * 局部负载均衡策略，为空表示不开启即使用全局策略
     */
    private String loadBalancer;

    public FusingPolicy() { }

    public FusingPolicy(String version, ConnectionPool connectionPool, OutlierDetection outlierDetection, String loadBalancer) {
        this.version = version;
        this.connectionPool = connectionPool;
        this.outlierDetection = outlierDetection;
        this.loadBalancer = loadBalancer;
    }

    public FusingPolicy(String version) {
        this(version, null, null, null);
    }

    @Data
    public static class ConnectionPool {
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

        public ConnectionPool() { }
        public ConnectionPool(Integer tcp_maxConnections, Integer tcp_connectTimeout, Integer http_http1MaxPendingRequests, Integer http_maxRequestPerConnection) {
            this.tcp_maxConnections = tcp_maxConnections;
            this.tcp_connectTimeout = tcp_connectTimeout;
            this.http_http1MaxPendingRequests = http_http1MaxPendingRequests;
            this.http_maxRequestPerConnection = http_maxRequestPerConnection;
        }
    }

    @Data
    public static class OutlierDetection{
        /**
         * 熔断异常检测中的连续5xx错误码数量
         */
        private Integer consecutive5xxErrors;

        public OutlierDetection() { }
        public OutlierDetection(Integer consecutive5xxErrors) {
            this.consecutive5xxErrors = consecutive5xxErrors;
        }
    }

}
