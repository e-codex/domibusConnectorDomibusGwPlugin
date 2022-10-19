package eu.domibus.connector.plugin.config;

import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.ws.policy.WSPolicyFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WsPolicyLoader {


    private static final Logger LOGGER = LoggerFactory.getLogger(WsPolicyLoader.class);

    public static WSPolicyFeature loadPolicyFeature(Resource wsPolicy) {
        LOGGER.debug("Loading policy from resource: [{}]", wsPolicy);
        WSPolicyFeature policyFeature = new WSPolicyFeature();
        policyFeature.setEnabled(true);


        try (InputStream is = wsPolicy.getInputStream()) {

//            List<Element> policyElements = new ArrayList<Element>();
            try {
                Element policyElement = StaxUtils.read(is).getDocumentElement();
                LOGGER.debug("adding policy element [{}]", policyElement.getNodeName());
    //            policyElements.add(policyElement);
                policyFeature.setPolicyElements(Collections.singletonList(policyElement));
            } catch (XMLStreamException ex) {
                throw new RuntimeException("cannot parse policy " + wsPolicy, ex);
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(String.format("ws policy [%s] cannot be read!", wsPolicy), ioe);
        }
        return policyFeature;
    }


}
