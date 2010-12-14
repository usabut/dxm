<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:if test="${not omitFormatting && !inWrapper}"><div id="${currentNode.UUID}"></c:if>
<template:include template="hidden.header"/>
<c:set var="isEmpty" value="true"/>
<c:choose>
    <c:when test="${moduleMap.liveOnly eq 'true' && !renderContext.liveMode}">
        <template:addResources type="javascript" resources="jquery.min.js"/>
        <div id="liveList${currentNode.identifier}"></div>
        <script type="text/javascript">
            $('#liveList${currentNode.identifier}').load('${url.baseLive}${currentNode.path}.html.ajax');
        </script>
    </c:when>
    <c:otherwise>
        <c:forEach items="${moduleMap.currentList}" var="subchild" begin="${moduleMap.begin}" end="${moduleMap.end}">
            <template:module node="${subchild}" template="${moduleMap.subNodesView}" editable="${moduleMap.editable}"/>
            <c:set var="isEmpty" value="false"/>
        </c:forEach>
        <c:if test="${not omitFormatting}"><div class="clear"></div></c:if>
        <c:if test="${moduleMap.editable and renderContext.editMode}">
            <template:module path="*"/>
        </c:if>
        <c:if test="${not empty moduleMap.emptyListMessage and renderContext.editMode and isEmpty}">
            ${moduleMap.emptyListMessage}
        </c:if>
        <template:include template="hidden.footer"/>

        <c:if test="${not omitFormatting && !inWrapper}"></div></c:if>
    </c:otherwise>
</c:choose>
