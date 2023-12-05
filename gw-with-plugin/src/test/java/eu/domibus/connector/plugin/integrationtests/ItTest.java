package eu.domibus.connector.plugin.integrationtests;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ItTest {

    @BeforeAll
    public static void startContainer() {
        Config config = new ConfigBuilder()
                .withHttpProxy(null)
                .withHttpsProxy(null)
//                .withNoProxy("*.my.domain.com") // does not work
                .build();

//        final OpenShiftExtensionAdapter ocAdapter = new OpenShiftExtensionAdapter();
//        final OpenShiftClient client = ocAdapter.adapt(new KubernetesClientBuilder().withConfig(config).build())
        try (KubernetesClient client = (new KubernetesClientBuilder().withConfig(config).build())) {
            final String namespace = client.getNamespace();

            final String requiredNamespace = "ju-eu-ejustice-eqs";

            if (namespace != null && !namespace.equals(requiredNamespace)) {
                throw new RuntimeException(String.format("The namespace must be '%s', but was '%s'", requiredNamespace, namespace));
            }

//            deleteDeploymentAndPersistentVolumeClaims(client, "03");
            buildLab(client, namespace);

        }

        //TODO: wait until pod ready...

    }

    private static void buildLab(KubernetesClient client, String namespace) {
        Properties p = new Properties();
        p.put("lab.id", "04");
//        p.put("gateway.image", "default-route-openshift-image-registry.apps.a2.cp.cna.at/ju-eu-ejustice-eqs/domibus-gw-with-connector-plugin:latest");
        p.put("gateway.image", "image-registry.openshift-image-registry.svc:5000/ju-eu-ejustice-eqs/domibus-gw-with-connector-plugin");

        List<HasMetadata> result = new ArrayList<>();

        //TODO: add domibus.properties
        //TODO: upload keystores

        result.addAll(client.load(loadResourceDefinition(new ClassPathResource("/k8s/templates/domibus-gw.yaml"), p)).get());

        client.resourceList(result).inNamespace(namespace).createOrReplace();
    }


    private static InputStream loadResourceDefinition(Resource r, Properties p) {
        try {
            String ecxConnectorClient = StreamUtils.copyToString(r.getInputStream(), StandardCharsets.UTF_8);
            PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}");
            ecxConnectorClient = propertyPlaceholderHelper.replacePlaceholders(ecxConnectorClient, p);
            return new ByteArrayInputStream(ecxConnectorClient.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }





}
