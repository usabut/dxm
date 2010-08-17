<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="person.css"/>
<script type="text/javascript">
    function ShowHideLayer(boxID) {
	/* Obtain reference for the selected boxID layer and its button */
	var box = document.getElementById("collapseBox"+boxID);

	/* If the selected box is currently invisible, show it */
	if(box.style.display == "none" || box.style.display=="") {
		box.style.display = "block";
	}
	/* otherwise hide it */
	else {
		box.style.display = "none";
	}
}
</script>
<div class="personListItem">
    <jcr:nodeProperty var="picture" node="${currentNode}" name="picture"/>
    <c:if test="${not empty picture}">
        <div class="personPhoto"><img src="${picture.node.thumbnailUrls['thumbnail']}" alt="${currentNode.properties.lastname.string} picture">
       </div>
    </c:if>
    <div class="personBody">
        <h4>${currentNode.properties.firstname.string}&nbsp;${currentNode.properties.lastname.string}</h4>

        <p class="personFonction">${currentNode.properties.function.string}</p>

        <p class="personBusinessUnit">${currentNode.properties.businessUnit.string}</p>

        <p class="personEmail"><a href='mailto:${currentNode.properties.email.string}'>${currentNode.properties.email.string}</a></p>

        <div class="personAction">
			<a class="personEnlarge" href="${picture.node.url}" rel="facebox"> <fmt:message key='FullSizePicture'/></a>
            <a class="personBiographiy" href="javascript:;" onclick="ShowHideLayer('${currentNode.identifier}');"><fmt:message
                    key='jahia.person.biography'/></a>
            <a class="personBiographiy" href="${url.base}${currentNode.path}.vcf"><fmt:message
                    key='jahia.person.vcard'/></a>
        </div>
         <div id="collapseBox${currentNode.identifier}" class="collapsible" >
            <jcr:nodeProperty node="${currentNode}" name="biography"/>
        </div>
        <!--stop collapsible -->
        <div class="clear"></div>
    </div>
    <!--stop personBody -->
    <div class="clear"></div>
</div>
