package gd.gov.demo.common;

import gd.gov.demo.request.FusingPolicy;
import lombok.Getter;

public enum TemplateFusingRequestEnum {
    HIGH(0, new FusingPolicy.ConnectionPool(1024, 25, 1024, null, 1), new FusingPolicy.OutlierDetection(1, 1, 10, 10, 10)),
    MID(1, new FusingPolicy.ConnectionPool(1024, 50, 1024, null, 3), new FusingPolicy.OutlierDetection(5, 5, 5, 20, 30)),
    LOW(2, new FusingPolicy.ConnectionPool(1024, 100, 1024, null, 5), new FusingPolicy.OutlierDetection(10, 10, 5, 20, 80));

    @Getter
    private Integer level;

    @Getter
    private FusingPolicy.ConnectionPool connectionPool;

    @Getter
    private FusingPolicy.OutlierDetection outlierDetection;

    TemplateFusingRequestEnum(Integer level, FusingPolicy.ConnectionPool connectionPool, FusingPolicy.OutlierDetection outlierDetection) {
        this.level = level;
        this.connectionPool = connectionPool;
        this.outlierDetection = outlierDetection;
    }

    public static TemplateFusingRequestEnum getByLevel(Integer level) {
        for (TemplateFusingRequestEnum var : values()) {
            if (var.getLevel().equals(level)) return var;
        }
        return null;
    }
}
