package org.jahia.modules.getaway.graphql.extensions;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import org.apache.commons.lang.StringUtils;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrNode;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Locale;

@GraphQLTypeExtension(GqlJcrNode.class)
public class JCRNodeExtensions {

    private static final Logger logger = LoggerFactory.getLogger(JCRNodeExtensions.class);

    private GqlJcrNode node;

    public JCRNodeExtensions(GqlJcrNode node) {
        this.node = node;
    }

    @GraphQLField
    public String country(@GraphQLName("propertyName") String pName, @GraphQLName("locale") String localeCode) {
        final String propertyName = StringUtils.isBlank(pName) ? "country" : pName;
        final Locale locale = StringUtils.isBlank(localeCode) ? Locale.ENGLISH : LanguageCodeConverters.getLocaleFromCode(localeCode);
        final String isoCode = node.getNode().getPropertyAsString(propertyName);
        if (StringUtils.isBlank(isoCode)) return null;
        return new Locale("en", isoCode).getDisplayCountry(locale);
    }

}
