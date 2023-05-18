package gd.gov.demo.service;

import java.util.List;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;

public interface KubeService {
    

    /**
     * 获取指定集群下运行的pod列表
     * @param clusterId 集群编号 {@link gd.gov.demo.common.ClusterEnum}
     * @param namespace 
     * @param deployment 可选，根据deployment获取pods
     * @return pod列表
     */
    public List<Pod> getPods(Integer clusterId, String namespace, String deployment);

    /**
     * 获取指定集群下运行的deployment列表
     * @param clusterId
     * @param namespace
     * @return deployment列表
     */
    public List<Deployment> getDeployments(Integer clusterId, String namespace);

    /**
     * 获取指定集群下运行的statefulset列表
     * @param clusterId
     * @param namespace
     * @return
     */
    public List<StatefulSet> getStatefulSets(Integer clusterId, String namespace);

    /**
     * 获取指定集群下运行的service列表
     * @param clusterId
     * @param namespace
     * @return
     */
    public List<Service> getServices(Integer clusterId, String namespace);
}
