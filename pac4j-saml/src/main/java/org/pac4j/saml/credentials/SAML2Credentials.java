package org.pac4j.saml.credentials;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.NameID;
import org.pac4j.core.credentials.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Credentials containing the nameId of the SAML subject and all of its attributes.
 *
 * @author Michael Remond
 * @since 1.5.0
 */
public class SAML2Credentials extends Credentials {
    private static final Logger logger = LoggerFactory.getLogger(SAML2Credentials.class);

    private static final long serialVersionUID = 5040516205957826527L;

    private final SAMLNameID nameId;

    private final String sessionIndex;

    private final List<SAMLAttribute> attributes;

    private final SAMLConditions conditions;

    private final String issuerId;

    private final List<String> authnContexts;

    private final String inResponseTo;

    public SAML2Credentials(final SAMLNameID nameId, final String issuerId,
                            final List<SAMLAttribute> samlAttributes, final Conditions conditions,
                            final String sessionIndex, final List<String> authnContexts,
                            final String inResponseTo) {
        this.nameId = nameId;
        this.issuerId = issuerId;
        this.sessionIndex = sessionIndex;
        this.attributes = samlAttributes;
        this.inResponseTo = inResponseTo;

        if (conditions != null) {
            this.conditions = new SAMLConditions();

            if (conditions.getNotBefore() != null) {
                this.conditions.setNotBefore(ZonedDateTime.ofInstant(conditions.getNotBefore(), ZoneOffset.UTC));
            }

            if (conditions.getNotOnOrAfter() != null) {
                this.conditions.setNotOnOrAfter(ZonedDateTime.ofInstant(conditions.getNotOnOrAfter(), ZoneOffset.UTC));
            }
        } else {
            this.conditions = null;
        }
        this.authnContexts = authnContexts;

        logger.info("Constructed SAML2 credentials: {}", this);
    }

    public final SAMLNameID getNameId() {
        return this.nameId;
    }

    public final String getSessionIndex() {
        return this.sessionIndex;
    }

    public final List<SAMLAttribute> getAttributes() {
        return this.attributes;
    }

    public SAMLConditions getConditions() {
        return this.conditions;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var that = (SAML2Credentials) o;

        if (nameId != null ? !nameId.equals(that.nameId) : that.nameId != null) {
            return false;
        }
        if (attributes != null ? !attributes.equals(that.attributes) : that.attributes != null) {
            return false;
        }
        if (sessionIndex != null ? sessionIndex.equals(that.sessionIndex) : that.sessionIndex != null) {
            return false;
        }
        return !(conditions != null ? !conditions.equals(that.conditions) : that.conditions != null);

    }

    @Override
    public int hashCode() {
        var result = nameId != null ? nameId.hashCode() : 0;
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        result = 31 * result + (sessionIndex != null ? sessionIndex.hashCode() : 0);
        result = 31 * result + (conditions != null ? conditions.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SAML2Credentials{" +
            "nameId=" + nameId +
            ", sessionIndex='" + sessionIndex + '\'' +
            ", attributes=" + attributes +
            ", conditions=" + conditions +
            ", issuerId='" + issuerId + '\'' +
            ", authnContexts=" + authnContexts +
            '}';
    }

    public String getIssuerId() {
        return issuerId;
    }

    public List<String> getAuthnContexts() {
        return authnContexts;
    }

    public String getInResponseTo() {
        return inResponseTo;
    }

    public static class SAMLNameID implements Serializable {
        private static final long serialVersionUID = -7913473743778305079L;
        private String format;
        private String nameQualifier;
        private String spNameQualifier;
        private String spProviderId;
        private String value;

        public static SAMLNameID from(final NameID nameId) {
            final var result = new SAMLNameID();
            result.setNameQualifier(nameId.getNameQualifier());
            result.setFormat(nameId.getFormat());
            result.setSpNameQualifier(nameId.getSPNameQualifier());
            result.setSpProviderId(nameId.getSPProvidedID());
            result.setValue(nameId.getValue());
            return result;
        }

        public static SAMLNameID from(final SAMLAttribute attribute) {
            final var result = new SAMLNameID();
            result.setValue(attribute.getAttributeValues().get(0));
            result.setFormat(attribute.getNameFormat());
            result.setNameQualifier(attribute.getName());
            result.setSpNameQualifier(attribute.getFriendlyName());
            return result;
        }

        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }

        public String getSpNameQualifier() {
            return spNameQualifier;
        }

        public void setSpNameQualifier(final String spNameQualifier) {
            this.spNameQualifier = spNameQualifier;
        }

        public String getSpProviderId() {
            return spProviderId;
        }

        public void setSpProviderId(final String spProviderId) {
            this.spProviderId = spProviderId;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(final String format) {
            this.format = format;
        }

        public String getNameQualifier() {
            return nameQualifier;
        }

        public void setNameQualifier(final String nameQualifier) {
            this.nameQualifier = nameQualifier;
        }

        @Override
        public String toString() {
            return "SAMLNameID{" +
                "format='" + format + '\'' +
                ", nameQualifier='" + nameQualifier + '\'' +
                ", spNameQualifier='" + spNameQualifier + '\'' +
                ", spProviderId='" + spProviderId + '\'' +
                ", value='" + value + '\'' +
                '}';
        }
    }

    public static class SAMLAttribute implements Serializable {
        private static final long serialVersionUID = 2532838901563948260L;
        private String friendlyName;
        private String name;
        private String nameFormat;
        private List<String> attributeValues = new ArrayList<>();

        public static SAMLAttribute from(Attribute attribute) {
            final var samlAttribute = new SAMLAttribute();
            samlAttribute.setFriendlyName(attribute.getFriendlyName());
            samlAttribute.setName(attribute.getName());
            samlAttribute.setNameFormat(attribute.getNameFormat());
            attribute.getAttributeValues()
                .stream()
                .map(XMLObject::getDOM)
                .filter(dom -> dom != null && dom.getTextContent() != null)
                .forEach(dom -> samlAttribute.getAttributeValues().add(dom.getTextContent()));
            return samlAttribute;
        }

        public static List<SAMLAttribute> from(final List<Attribute> samlAttributes) {

            List<SAMLAttribute> extractedAttributes = new ArrayList<>();

            samlAttributes.forEach(openSamlAttribute -> {
                openSamlAttribute.getAttributeValues().forEach(attributeValue -> {
                    boolean isComplexType = attributeValue.hasChildren();

                    if (isComplexType) {
                        List<SAMLAttribute> attrs = collectAttributesFromNodeList(attributeValue.getDOM().getChildNodes());
                        extractedAttributes.addAll(attrs);
                    } else {
                        extractedAttributes.add(SAMLAttribute.from(openSamlAttribute));
                    }
                });
            });

            return extractedAttributes;
        }

        private static List<SAMLAttribute> collectAttributesFromNodeList(NodeList nodeList)  {

            var results = new ArrayList<SAMLAttribute>();

            if (nodeList == null) {
                return results;
            }

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.hasChildNodes()) {
                    results.addAll(collectAttributesFromNodeList(node.getChildNodes()));
                } else if (!node.getTextContent().isBlank()) {
                    SAMLAttribute samlAttribute = new SAMLAttribute();
                    samlAttribute.setName(node.getParentNode().getLocalName());
                    samlAttribute.getAttributeValues().add(node.getTextContent());
                    results.add(samlAttribute);
                }
            }

            return results;
        }

        public String getFriendlyName() {
            return friendlyName;
        }

        public void setFriendlyName(final String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getNameFormat() {
            return nameFormat;
        }

        public void setNameFormat(final String nameFormat) {
            this.nameFormat = nameFormat;
        }

        public List<String> getAttributeValues() {
            return attributeValues;
        }

        public void setAttributeValues(final List<String> attributeValues) {
            this.attributeValues = attributeValues;
        }

        @Override
        public String toString() {
            return "SAMLAttribute{" +
                "friendlyName='" + friendlyName + '\'' +
                ", name='" + name + '\'' +
                ", nameFormat='" + nameFormat + '\'' +
                ", attributeValues=" + attributeValues +
                '}';
        }
    }

    public static class SAMLConditions implements Serializable {
        private static final long serialVersionUID = -8966585574672014553L;
        private ZonedDateTime notBefore;
        private ZonedDateTime notOnOrAfter;

        public ZonedDateTime getNotBefore() {
            return notBefore;
        }

        public void setNotBefore(final ZonedDateTime notBefore) {
            this.notBefore = notBefore;
        }

        public ZonedDateTime getNotOnOrAfter() {
            return notOnOrAfter;
        }

        public void setNotOnOrAfter(final ZonedDateTime notOnOrAfter) {
            this.notOnOrAfter = notOnOrAfter;
        }

        @Override
        public String toString() {
            return "SAMLConditions{" +
                "notBefore=" + notBefore +
                ", notOnOrAfter=" + notOnOrAfter +
                '}';
        }
    }
}
