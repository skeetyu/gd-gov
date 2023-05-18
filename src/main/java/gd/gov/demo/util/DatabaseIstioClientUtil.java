package gd.gov.demo.util;

import io.fabric8.istio.client.IstioClient;
import lombok.Getter;

public class DatabaseIstioClientUtil {
    @Getter
    private static IstioClient client;

    static {
        while (!DatabaseKubernetesClientUtil.ifConnected());
        client = DatabaseKubernetesClientUtil.buildIstioClient();
    }
}
