package org.jahia.modules.getaway.servlet;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GetAwayFilter extends AbstractServletFilter {

    private static final Logger logger = LoggerFactory.getLogger(GetAwayFilter.class);

    private String corsOrigin;

    @java.lang.Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @java.lang.Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        logger.debug("Received request");

        if (servletResponse instanceof HttpServletResponse && servletRequest instanceof HttpServletRequest) {
            final HttpServletResponse response = (HttpServletResponse) servletResponse;
            final HttpServletRequest request = (HttpServletRequest) servletRequest;

            if (StringUtils.isNotBlank(corsOrigin))
                response.setHeader("Access-Control-Allow-Origin", corsOrigin);
            if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
                response.addHeader("Access-Control-Allow-Headers", "content-type");
                response.addHeader("Access-Control-Allow-Headers", "X-GETAWAY");
            } else {
                // TODO test that the origin is allowed
                final String origin = request.getHeader("origin");
                if (!corsOrigin.equalsIgnoreCase(origin)) {
                    logger.error(String.format("Unexpected origin: %s", origin));
                    return;
                }
                final JahiaUser oldUser = JCRSessionFactory.getInstance().getCurrentUser();
                JCRSessionFactory.getInstance().setCurrentUser(JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser());
                servletRequest.getRequestDispatcher("/modules/graphql").forward(servletRequest, servletResponse);
                JCRSessionFactory.getInstance().setCurrentUser(oldUser);
            }
        } else {
            logger.error("Expected HTTP request, got {}", servletResponse.getClass().getName());
            logger.error("Expected HTTP response, got {}", servletResponse.getClass().getName());
        }
    }

    @java.lang.Override
    public void destroy() {
    }

    public void setCorsOrigin(String corsOrigin) {
        this.corsOrigin = corsOrigin;
    }
}
