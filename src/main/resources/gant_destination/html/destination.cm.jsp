<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<jcr:node var="getAwayFolder" path="${renderContext.site.path}/contents/getaway"/>
<jcr:nodeProperty node="${getAwayFolder}" name="liveURL" var="appURL"/>


<jcr:nodeProperty node="${currentNode}" name="photos" var="destinationImages"/>

<c:forEach items="${destinationImages}" var="phot">
   <c:set var="destinationImage" value="${phot}"/>
</c:forEach>
<c:url value="${url.files}${destinationImage.node.path}" var="imageUrl"/>

<div style="display: block; margin: 0; padding: 0; border:0; outline:0; -ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%;">
	<div style="background: #fff; width: 100%; height: 100%; position: absolute;">

		<!-- Destination top section -->
		<div style="margin: 20px auto; max-width: 760px; margin-bottom: 0; position: relative;">
			<h1 style="font-size: 32px; font-weight: 400; margin-bottom: 8px;">${currentNode.properties['destinationname'].string}
              <span style="padding-left: 24px; font-size: 24px; font-weight: 600; color: rgba(0, 0, 0, 0.44);">
                <jcr:nodePropertyRenderer node="${currentNode}" name="country" renderer="flagcountry"/>
				</span>
          </h1>
          <a target="_blank" style="position: absolute; top: 12px; right: 20; text-decoration: none; color: rgb(255, 56, 124); font-size: 16px; height: 24px; border: 2px solid #ff387c; padding: 2px 12px; border-radius: 50px; padding-top: 0; cursor: pointer; font-weight: 600;" href="${appURL}/destination/${currentNode.properties['j:nodename'].string}">Open in app</a>
          <img style="width: 100%" src="${imageUrl}" alt="${currentNode.properties['destinationname'].string}" />
		</div>

		<!-- Outline section -->
		<div style="max-width: 1080px; margin: 0 auto;">
			<div style="padding: 16px; margin: 20px auto; max-width: 760px; margin-bottom: 30px; font-size: 18px; line-height: 31px; margin-top: 8px;">
				<div style="margin-bottom: 20px;">
                  ${currentNode.properties['outline'].string}
				</div>
			</div>
		</div>

	</div>
</div>