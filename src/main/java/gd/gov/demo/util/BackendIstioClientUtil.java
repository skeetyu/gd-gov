package gd.gov.demo.util;

import io.fabric8.istio.client.IstioClient;
import lombok.Getter;

public class BackendIstioClientUtil {
    @Getter
    private static IstioClient client;

    static {
        while (!BackendKubernetesClientUtil.ifConnected());
        client = BackendKubernetesClientUtil.buildIstioClient();
    }
}
