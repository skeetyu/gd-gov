package gd.gov.demo.common;

import lombok.Getter;

public enum ClusterEnum {
    Frontend("frontend", 0), Backend("backend", 1), Database("database", 2);

    @Getter
    private String cluster;

    @Getter
    private Integer id;

    ClusterEnum(String cluster, Integer id) {
        this.cluster = cluster;
        this.id = id;
    }

    public static ClusterEnum getById(Integer id) {
        for (ClusterEnum cluster : values()) {
            if (cluster.getId().equals(id)) return cluster;
        }
        return null;
    }
}
