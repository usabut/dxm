<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<jcr:nodeProperty node="${currentNode}" name="boxTitle" var="boxTitle"/>
<div class="box2 ">
    <div class="box2-topright"></div>
    <div class="box2-topleft"></div>
    <c:if test="${not empty boxTitle}">
        <h3 class="box2-header"><span>${boxTitle.string}</span></h3>
    </c:if>
    <div class="box2-text">
        <c:forEach items="${currentNode.children}" var="subchild">
            <template:module node="${subchild}"/>
        </c:forEach>
    </div>
    <div class="box2-bottomright"></div>
    <div class="box2-bottomleft"></div>
    <div class="clear"></div>
</div>