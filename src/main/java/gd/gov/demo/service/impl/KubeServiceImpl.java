package gd.gov.demo.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import gd.gov.demo.common.ClusterEnum;
import gd.gov.demo.service.KubeService;
import gd.gov.demo.util.BackendKubernetesClientUtil;
import gd.gov.demo.util.DatabaseKubernetesClientUtil;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.KubernetesClient;

@Service
public class KubeServiceImpl implements KubeService {

    @Override
    public List<Pod> getPods(Integer clusterId, String namespace, String deployment) {
        ClusterEnum cluster = ClusterEnum.getById(clusterId);
        if (cluster.equals(ClusterEnum.Backend)) {
            return getPods(BackendKubernetesClientUtil.getClient(), namespace, deployment);
        } else if (cluster.equals(ClusterEnum.Database)) {
            return getPods(DatabaseKubernetesClientUtil.getClient(), namespace, deployment);
        } else if (cluster.equals(ClusterEnum.Frontend)) {
            return null;
        }
        return null;
    }    

    // 使用该方法只能获取deployments对应的pod
    private List<Pod> getPods(KubernetesClient client, String namespace, String deployment) {
        if (deployment == null || deployment.isEmpty()) return getAllPods(client, namespace);
        else {
            Map<String, String> matchLabels = client.apps().deployments().inNamespace(namespace).withName(deployment).get().getSpec().getSelector().getMatchLabels();
            List<Pod> pods = client.pods().inNamespace(namespace).withLabels(matchLabels).list().getItems();
            return pods;
        }
    }

    private List<Pod> getAllPods(KubernetesClient client, String namespace) {
        List<Pod> pods = client.pods().inNamespace(namespace).list().getItems();
        List<Pod> retPods = new ArrayList<Pod>(pods.size());

        pods.forEach(pod -> {
            String podName = pod.getMetadata().getName();
            if (podName != null && !podName.startsWith("kubernetes")) {
                retPods.add(pod);
            }
        });
        
        return retPods;
    }

    @Override
    public List<Deployment> getDeployments(Integer clusterId, String namespace) {
        ClusterEnum cluster = ClusterEnum.getById(clusterId);
        if (cluster.equals(ClusterEnum.Backend)) {
            return getDeployments(BackendKubernetesClientUtil.getClient(), namespace);
        } else if (cluster.equals(ClusterEnum.Database)) {
            return getDeployments(DatabaseKubernetesClientUtil.getClient(), namespace);
        } else if (cluster.equals(ClusterEnum.Frontend)) {
            return null;
        }
        return null;
    }

    private List<Deployment> getDeployments(KubernetesClient client, String namespace) {
        List<Deployment> deployments = client.apps().deployments().inNamespace(namespace).list().getItems();
        List<Deployment> retDeployments = new ArrayList<Deployment>(deployments.size());

        deployments.forEach(deployment -> {
            String deploymentName = deployment.getMetadata().getName();
            if (deploymentName != null && !deploymentName.startsWith("kubernetes")) {
                retDeployments.add(deployment);
            }
        });

        return retDeployments;
    }

    @Override
    public List<StatefulSet> getStatefulSets(Integer clusterId, String namespace) {
        ClusterEnum cluster = ClusterEnum.getById(clusterId);
        if (cluster.equals(ClusterEnum.Backend)) {
            return getStatefulSets(BackendKubernetesClientUtil.getClient(), namespace);
        } else if (cluster.equals(ClusterEnum.Database)) {
            return getStatefulSets(DatabaseKubernetesClientUtil.getClient(), namespace);
        } else if (cluster.equals(ClusterEnum.Frontend)) {
            return null;
        }
        return null;
    }

    private List<StatefulSet> getStatefulSets(KubernetesClient client, String namespace) {
        List<StatefulSet> statefulSets = client.apps().statefulSets().inNamespace(namespace).list().getItems();
        return statefulSets;
    }

    @Override
    public List<io.fabric8.kubernetes.api.model.Service> getServices(Integer clusterId, String namespace) {
        ClusterEnum cluster = ClusterEnum.getById(clusterId);
        if (cluster.equals(ClusterEnum.Backend)) {
            return getServices(BackendKubernetesClientUtil.getClient(), namespace);
        } else if (cluster.equals(ClusterEnum.Database)) {
            return getServices(DatabaseKubernetesClientUtil.getClient(), namespace);
        } else if (cluster.equals(ClusterEnum.Frontend)) {
            return null;
        }
        return null;
    }

    private List<io.fabric8.kubernetes.api.model.Service> getServices(KubernetesClient client, String namespace) {
        List<io.fabric8.kubernetes.api.model.Service> services = client.services().inNamespace(namespace).list().getItems();
        List<io.fabric8.kubernetes.api.model.Service> retServices = new ArrayList<io.fabric8.kubernetes.api.model.Service>(services.size());

        services.forEach(service -> {
            String svcName = service.getMetadata().getName();
            if (svcName != null && !svcName.startsWith("kubernetes")) {
                retServices.add(service);
            }
        });

        return retServices;
    }
}
