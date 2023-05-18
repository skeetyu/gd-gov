package gd.gov.demo.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import io.fabric8.istio.client.DefaultIstioClient;
import io.fabric8.istio.client.IstioClient;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.Getter;

public class DatabaseKubernetesClientUtil {
    @Getter
    private static KubernetesClient client;
    private static Config config;
    private static String kubeConfigPath = "kubeconfig/database_cluster";

    static {
        try {
            connect();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error in building k8s client");
        }
    }

    public static void connect() throws IOException {
        InputStream is = BackendKubernetesClientUtil.class.getClassLoader().getResourceAsStream(kubeConfigPath);
        File configFile = new File("config");
        InputStreamToFile.inputstreamtofile(is, configFile);
        final String configYaml = String.join("\n", Files.readAllLines(configFile.toPath()));
        config = Config.fromKubeconfig(configYaml);
        config.setTrustCerts(true);
        client = new KubernetesClientBuilder().withConfig(config).build();
    }

    public static IstioClient buildIstioClient() {
        return new DefaultIstioClient(config);
    }

    public static boolean ifConnected() {
        return client != null;
    }

}
