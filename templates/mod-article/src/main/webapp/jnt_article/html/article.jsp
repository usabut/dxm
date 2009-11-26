<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h2><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h2>


<div class="intro">
    ${currentNode.properties.intro.string}
</div>
<c:forEach items="${currentNode.editableChildren}" var="paragraph">
    <template:module node="${currentNode.properties.paragraph}" template="default"/>
</c:forEach>
<div>
	<fmt:message key="tag.this.article"/>:&nbsp;<template:option node="${currentNode}" nodetype="jmix:tagged" template="hidden.tags"/>
	<template:option node="${currentNode}"  nodetype="jmix:tagged" template="hidden.addTag"/>
</div>