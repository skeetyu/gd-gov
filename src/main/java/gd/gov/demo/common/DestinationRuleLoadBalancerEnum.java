package gd.gov.demo.common;

import lombok.Getter;

public enum DestinationRuleLoadBalancerEnum {
    ROUND_ROBIN(1), LEAST_CONN(2), RANDOM(3), PASSTHROUGH(4);

    @Getter
    private Integer policy;

    DestinationRuleLoadBalancerEnum(Integer policy) {
        this.policy = policy;
    }

    public static DestinationRuleLoadBalancerEnum getByPolicy(Integer policy) {
        for (DestinationRuleLoadBalancerEnum var : values()) {
            if (var.getPolicy().equals(policy)) return var;
        }
        return null;
    }
}
