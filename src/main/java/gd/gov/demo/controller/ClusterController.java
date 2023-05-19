package gd.gov.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import gd.gov.demo.entity.JsonResult;
import gd.gov.demo.request.ClusterIdRequest;
import gd.gov.demo.request.DestinationRuleRequest;
import gd.gov.demo.request.NfcRequest;
import gd.gov.demo.request.TemplateFusingRequest;
import gd.gov.demo.request.VirtualServiceRequest;
import gd.gov.demo.service.IstioService;
import gd.gov.demo.service.KubeService;
import gd.gov.demo.util.BackendKubernetesClientUtil;
import gd.gov.demo.util.DatabaseKubernetesClientUtil;
import io.fabric8.istio.api.networking.v1alpha3.DestinationRule;
import io.fabric8.istio.api.networking.v1alpha3.EnvoyFilter;
import io.fabric8.istio.api.networking.v1alpha3.Gateway;
import io.fabric8.istio.api.networking.v1alpha3.VirtualService;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;

@Controller
public class ClusterController {
    @Autowired
    private KubeService kubeService;
    @Autowired
    private IstioService istioService;

    /**
     * 连接集群
     * @param clusterId 集群id
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "/connect")
    @ResponseBody
    public JsonResult<String> connect() {
        try {
            BackendKubernetesClientUtil.connect();
            DatabaseKubernetesClientUtil.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new JsonResult<>("connect");
    }

    /**
     * 获取集群pod列表
     * @param clusterId 集群id
     * @param namespace 名称空间
     * @param deployment    deployment名称（可选）
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "/pods")
    @ResponseBody
    public JsonResult<List<Pod>> getPods(@RequestParam(required = false, defaultValue = "1") Integer clusterId, 
                                            @RequestParam(required = false, defaultValue = "default") String namespace,
                                            @RequestParam(required = false, defaultValue = "") String deployment) {
        if (clusterId != null && (clusterId.intValue() < 0 || clusterId.intValue() > 2)) return new JsonResult<>("clusterId非法");

        List<Pod> podList = kubeService.getPods(clusterId, namespace, deployment);
        return new JsonResult<>(podList);
    }

    /**
     * 获取集群deployment列表
     * @param clusterId 集群id
     * @param namespace 名称空间
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "/deployments")
    @ResponseBody
    public JsonResult<List<Deployment>> getDeployments(@RequestParam(required = false, defaultValue = "1") Integer clusterId, 
                                            @RequestParam(required = false, defaultValue = "default") String namespace) {
        if (clusterId != null && (clusterId.intValue() < 0 || clusterId.intValue() > 2)) return new JsonResult<>("clusterId非法");

        List<Deployment> deploymentList = kubeService.getDeployments(clusterId, namespace);
        return new JsonResult<>(deploymentList);
    }

    /**
     * 获取集群statefulset列表
     * @param clusterId 集群id
     * @param namespace 名称空间
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "/statefulSets")
    @ResponseBody
    public JsonResult<List<StatefulSet>> getStatefulSets(@RequestParam(required = false, defaultValue = "1") Integer clusterId, 
                                            @RequestParam(required = false, defaultValue = "default") String namespace) {
        if (clusterId != null && (clusterId.intValue() < 0 || clusterId.intValue() > 2)) return new JsonResult<>("clusterId非法");

        List<StatefulSet> statefulSetList = kubeService.getStatefulSets(clusterId, namespace);
        return new JsonResult<>(statefulSetList);
    }

    /**
     * 获取集群service列表
     * @param clusterId 集群id
     * @param namespace 名称空间
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "/services")
    @ResponseBody
    public JsonResult<List<Service>> getServices(@RequestParam(required = false, defaultValue = "1") Integer clusterId, 
                                            @RequestParam(required = false, defaultValue = "default") String namespace) {
        if (clusterId != null && (clusterId.intValue() < 0 || clusterId.intValue() > 2)) return new JsonResult<>("clusterId非法");

        List<Service> serviceList = kubeService.getServices(clusterId, namespace);
        return new JsonResult<>(serviceList);
    }

    /**
     * 获取集群gateway（默认单个）
     * @param clusterId 集群id
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "/gateway")
    @ResponseBody
    public JsonResult<Gateway> getGateway(@RequestParam(required = false, defaultValue = "1") Integer clusterId) {
        if (clusterId != null && (clusterId.intValue() < 0 || clusterId.intValue() > 2)) return new JsonResult<>("clusterId非法");

        Gateway gateway = istioService.getGateway(clusterId);
        return new JsonResult<>(gateway);
    }

    /**
     * 获取集群virtual service（默认单个）
     * @param clusterId 集群id
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "/virtualService")
    @ResponseBody
    public JsonResult<VirtualService> getVirtualService(@RequestParam(required = false, defaultValue = "1") Integer clusterId) {
        if (clusterId != null && (clusterId.intValue() < 0 || clusterId.intValue() > 2)) return new JsonResult<>("clusterId非法");

        VirtualService vs = istioService.getVirtualService(clusterId);
        return new JsonResult<>(vs);
    }

    /**
     * 部署virtual service
     * @param virtualServiceRequest    vs配置请求
     * @return
     */
    @CrossOrigin
    @PostMapping(value = "/virtualService/apply")
    @ResponseBody
    public JsonResult<Boolean> applyVirtualService(@RequestBody VirtualServiceRequest virtualServiceRequest) {
        Integer clusterId = virtualServiceRequest.getClusterId();
        if (clusterId != null && (clusterId.intValue() < 0 || clusterId.intValue() > 2)) return new JsonResult<>("clusterId非法");

        if (virtualServiceRequest.getNamespace() == null || virtualServiceRequest.getNamespace().isEmpty()) virtualServiceRequest.setNamespace("default");

        List<String> versions = virtualServiceRequest.getVersions();
        if (versions == null || versions.isEmpty()) return new JsonResult<>("versions不能为空");

        List<Integer> weights = virtualServiceRequest.getWeights();
        if (weights == null || weights.isEmpty()) return new JsonResult<>("weights不能为空");

        if (versions.size() != weights.size()) return new JsonResult<>("请为每个服务子集配置权重");

        int sumWeights = 0;
        for (Integer w : weights) {
            sumWeights += w;
        }
        if (sumWeights != 100) return new JsonResult<>("请确保权重之和为100");

        boolean ret = istioService.applyVirtualService(virtualServiceRequest);
        // boolean ret = true;
        return new JsonResult<>(ret);
    }

    /**
     * 部署模板virtual service
     * @param clusterId 集群id
     * @return
     */
    @CrossOrigin
    @PostMapping(value = "/virtualService/template")
    @ResponseBody
    public JsonResult<Boolean> applyTemplateVirtualService(@RequestBody ClusterIdRequest clusterIdRequest) {
        Integer clusterId = clusterIdRequest.getClusterId();
        if (clusterId != null && (clusterId.intValue() < 0 || clusterId.intValue() > 2)) return new JsonResult<>("clusterId非法");
        boolean ret = istioService.applyTemplateVirtualService(clusterId);
        // boolean ret = true;
        return new JsonResult<>(ret);
    }
    
    /**
     * 获取集群destination rule（默认单个）
     * @param clusterId 集群id
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "/destinationRule")
    @ResponseBody
    public JsonResult<DestinationRule> getDestinationRule(@RequestParam(required = false, defaultValue = "1") Integer clusterId) {
        if (clusterId != null && (clusterId.intValue() < 0 || clusterId.intValue() > 2)) return new JsonResult<>("clusterId非法");

        DestinationRule dr = istioService.getDestinationRule(clusterId);
        return new JsonResult<>(dr);
    }

    /**
     * 部署destination rule
     * @param destinationRuleRequest    dr配置请求
     * @return
     */
    @CrossOrigin
    @PostMapping(value = "/destinationRule/apply")
    @ResponseBody
    public JsonResult<Boolean> applyDestinationRule(@RequestBody DestinationRuleRequest destinationRuleRequest) {
        Integer clusterId = destinationRuleRequest.getClusterId();
        if (clusterId != null && (clusterId.intValue() < 0 || clusterId.intValue() > 2)) return new JsonResult<>("clusterId非法");

        if (destinationRuleRequest.getNamespace() == null || destinationRuleRequest.getNamespace().isEmpty()) destinationRuleRequest.setNamespace("default");

        // System.out.println(destinationRuleRequest);
        // boolean ret = true;
        boolean ret = istioService.applyDestinationRule(destinationRuleRequest);
        return new JsonResult<>(ret);
    }

    /**
     * 部署模板destination rule
     * @param clusterId 集群id
     * @return
     */
    @CrossOrigin
    @PostMapping(value = "/destinationRule/template")
    @ResponseBody
    public JsonResult<Boolean> applyTemplateDestinationRule(@RequestBody ClusterIdRequest clusterIdRequest) {
        Integer clusterId = clusterIdRequest.getClusterId();
        if (clusterId != null && (clusterId.intValue() < 0 || clusterId.intValue() > 2)) return new JsonResult<>("clusterId非法");
        boolean ret = istioService.applyTemplateDestinationRule(clusterId);
        return new JsonResult<>(ret);
    }

    @CrossOrigin
    @PostMapping(value = "/destinationRule/fuse/template")
    @ResponseBody
    public JsonResult<Boolean> applyTemplateFusePolicy(@RequestBody TemplateFusingRequest templateFusingRequest) {
        Integer clusterId = templateFusingRequest.getClusterId();
        if (clusterId != null && clusterId.intValue() != 1) return new JsonResult<>("clusterId非法"); // 方法默认对server集群生效

        boolean ret = istioService.applyTemplateFusingPolicy(templateFusingRequest);
        return new JsonResult<>(ret);
    }
    
    /**
     * 获取envoy filter列表
     * @param clusterId 集群id
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "/envoyFilters")
    @ResponseBody
    public JsonResult<List<EnvoyFilter>> getEnvoyFilters(@RequestParam(required = false, defaultValue = "1") Integer clusterId) {
        if (clusterId != null && (clusterId.intValue() < 0 || clusterId.intValue() > 2)) return new JsonResult<>("clusterId非法");

        List<EnvoyFilter> envoyFilters = istioService.getEnvoyFilters(clusterId);
        return new JsonResult<>(envoyFilters);
    }

    // /**
    //  * 按指定名称获取envoy filter配置
    //  * @param clusterId 集群id
    //  * @param efName    envoy filter名称
    //  * @return
    //  */
    // @CrossOrigin
    // @GetMapping(value = "/envoyFilters/get")
    // @ResponseBody
    // public JsonResult<EnvoyFilter> getEnvoyFilter(@RequestParam(required = false, defaultValue = "1") Integer clusterId,
    //                                                 @RequestParam(required = true) String efName) {
    //     if (clusterId != null && (clusterId.intValue() < 0 || clusterId.intValue() > 2)) return new JsonResult<>("clusterId非法");
    //     if (efName == null || efName.isEmpty()) return new JsonResult<>("envoy filter名称不能为空");

    //     EnvoyFilter ef = istioService.getEnvoyFilterByName(clusterId, efName);
    //     return new JsonResult<>(ef);
    // }

    /**
     * 部署envoy filter限流配置
     * @param nfcRequest    限流配置请求
     * @return
     */
    @CrossOrigin
    @PostMapping(value = "/envoyFilter/apply")
    @ResponseBody
    public JsonResult<Boolean> applyEnvoyFilter(@RequestBody NfcRequest nfcRequest) {
        Integer clusterId = nfcRequest.getClusterId();
        if (clusterId != null && (clusterId.intValue() < 0 || clusterId.intValue() > 2)) return new JsonResult<>("clusterId非法");

        boolean ret = istioService.applyEnvoyFilter(nfcRequest);
        return new JsonResult<>(ret);
    }
}
