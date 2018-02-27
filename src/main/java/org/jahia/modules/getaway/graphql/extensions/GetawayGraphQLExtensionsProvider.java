package org.jahia.modules.getaway.graphql.extensions;

import org.jahia.modules.graphql.provider.dxm.DXGraphQLExtensionsProvider;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Component(service = DXGraphQLExtensionsProvider.class, immediate = true)
public class GetawayGraphQLExtensionsProvider implements DXGraphQLExtensionsProvider {

    private static final Logger logger = LoggerFactory.getLogger(GetawayGraphQLExtensionsProvider.class);

    @Override
    public Collection<Class<?>> getExtensions() {
        return Collections.<Class<?>>singletonList(JCRNodeExtensions.class);
    }
}
