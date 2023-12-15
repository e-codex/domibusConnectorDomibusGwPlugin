package eu.domibus.connector.plugin.ws;

import eu.domibus.connector.plugin.config.property.AbstractDCPluginPropertyManager;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.security.SecurityContext;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.token.BinarySecurity;
import org.apache.wss4j.common.token.X509Security;
import org.apache.wss4j.dom.util.WSSecurityUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuthenticationService extends AbstractPhaseInterceptor {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(AuthenticationService.class);

    private final UseUsernameFrom usernameSource;
    private final String defaultUsername;
    private final List<SimpleGrantedAuthority> defaultRoles;
    private final KeyStore keyStore;

    public enum UseUsernameFrom {
        DEFAULT,
        ALIAS,
        DN;
    }

    public AuthenticationService(AbstractDCPluginPropertyManager abstractDcPluginPropertyManager,
                                 ApplicationContext ctx) {
        super(Phase.PRE_INVOKE);

        String usernameFrom = abstractDcPluginPropertyManager.getKnownPropertyValue(abstractDcPluginPropertyManager.withPluginPrefix(AbstractDCPluginPropertyManager.DC_PLUGIN_USE_USERNAME_FROM_PROPERTY_NAME));
        if (usernameFrom == null || !ArrayUtils.contains(Arrays.stream(UseUsernameFrom.values()).map(Objects::toString).toArray(), usernameFrom)) {
            throw new IllegalArgumentException(String.format("The property [%s] with value [%s] is not valid, please use on of these values: [%s]",
                    abstractDcPluginPropertyManager.withPluginPrefix(AbstractDCPluginPropertyManager.DC_PLUGIN_USE_USERNAME_FROM_PROPERTY_NAME),
                    usernameFrom,
                    Stream.of(UseUsernameFrom.values()).map(Objects::toString).collect(Collectors.joining(","))
            ));
        }
        this.usernameSource = UseUsernameFrom.valueOf(usernameFrom);
        this.defaultUsername = abstractDcPluginPropertyManager.getKnownPropertyValue(abstractDcPluginPropertyManager.withPluginPrefix(AbstractDCPluginPropertyManager.DC_PLUGIN_DEFAULT_USER_PROPERTY_NAME));
        if (usernameSource == UseUsernameFrom.DEFAULT && !StringUtils.hasText(defaultUsername)) {
            throw new IllegalArgumentException(String.format("If Username Source is [%s] then default username property [%s] must not be empty", usernameSource, abstractDcPluginPropertyManager.withPluginPrefix(AbstractDCPluginPropertyManager.DC_PLUGIN_DEFAULT_USER_PROPERTY_NAME)));
        }
        String roles = abstractDcPluginPropertyManager.getKnownPropertyValueWithPrefix(AbstractDCPluginPropertyManager.DC_PLUGIN_DEFAULT_ROLES_PROPERTY_NAME);
        if (!StringUtils.hasText(roles)) {
            throw new IllegalArgumentException(String.format("The property [%s] is empty!", abstractDcPluginPropertyManager.withPluginPrefix(AbstractDCPluginPropertyManager.DC_PLUGIN_DEFAULT_ROLES_PROPERTY_NAME)));
        }
        this.defaultRoles = Stream.of(roles.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        if (usernameSource == UseUsernameFrom.ALIAS) {
            String storeType = abstractDcPluginPropertyManager.getKnownPropertyValue(abstractDcPluginPropertyManager.withPluginPrefix(AbstractDCPluginPropertyManager.CXF_TRUST_STORE_TYPE_PROPERTY_NAME));
            String location = abstractDcPluginPropertyManager.getKnownPropertyValue(abstractDcPluginPropertyManager.withPluginPrefix(AbstractDCPluginPropertyManager.CXF_TRUST_STORE_PATH_PROPERTY_NAME));
            String password = abstractDcPluginPropertyManager.getKnownPropertyValue(abstractDcPluginPropertyManager.withPluginPrefix(AbstractDCPluginPropertyManager.CXF_TRUST_STORE_PASSWORD_PROPERTY_NAME));
            try {
                KeyStore ks = KeyStore.getInstance(storeType);
                Resource resource = ctx.getResource(location);
                ks.load(resource.getInputStream(), password.toCharArray());
                this.keyStore = ks;

            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
                String error = String.format("Property: [%s] is invalid:Failed to load KeyStore from location [%s]", abstractDcPluginPropertyManager.withPluginPrefix(AbstractDCPluginPropertyManager.CXF_TRUST_STORE), location);
                throw new RuntimeException(error, e);
            }
        } else {
            this.keyStore = null;
        }

    }



    @Override
    public void handleMessage(Message message) throws Fault {
        String username = defaultUsername;

        if (usernameSource != UseUsernameFrom.DEFAULT) {


            SecurityContext context = message.get(SecurityContext.class);
            if (context == null || context.getUserPrincipal() == null) {
                //reportSecurityException("User Principal is not available on the current message");
                throw new RuntimeException("User Principal is not available on the current message");
            }


            String userPrincipal = context.getUserPrincipal().getName();
            if (usernameSource == UseUsernameFrom.DN) {
                username = userPrincipal;
            } else if (usernameSource == UseUsernameFrom.ALIAS) {
                try {
                    SoapMessage soapMessage = (SoapMessage) message;

                    SOAPMessage doc = soapMessage.getContent(SOAPMessage.class);

                    WSSecurityUtil w;
                    Element elem = WSSecurityUtil.getSecurityHeader(doc.getSOAPPart(), "");
                    // get a BinarySignature tag
                    Node binarySignatureTag = elem.getFirstChild();
                    BinarySecurity token = new X509Security((Document) binarySignatureTag);

                    // a X509Certificate construction
                    InputStream in = new ByteArrayInputStream(token.getToken());
                    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                    X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
                    username = this.keyStore.getCertificateAlias(cert);
                } catch (CertificateException | KeyStoreException | WSSecurityException ce) {
                    throw new RuntimeException(ce);
                }
            }


        }
        LOGGER.debug("Username is [{}]", username);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, "", defaultRoles);
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

    }


    @Override
    public void handleFault(Message message) {

    }
}
