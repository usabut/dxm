<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="box2.css"/>

<div class="box2 box2-style1">
    <jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<c:if test="${not empty title}">
    <div class="box2-topright"></div><div class="box2-topleft"></div>
    <h3 class="box2-header"><span>${title.string}</span></h3>
</c:if>
  <div class="box2-text">
      ${wrappedContent}
  </div>
    <div class="box2-bottomright"></div>
    <div class="box2-bottomleft"></div>
<div class="clear"></div></div>
