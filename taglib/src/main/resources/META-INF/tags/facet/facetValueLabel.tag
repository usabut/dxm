<%@ tag body-content="empty" description="Renders the label of a facet." %>
<%@ tag import="org.apache.solr.schema.DateField"%>
<%@ tag import="java.util.Date"%>
<%@ tag import="java.text.SimpleDateFormat"%>
<%@ tag import="org.jahia.services.render.RenderContext"%>
<%@ tag import="java.util.Locale"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean" description="Should we display the label or just return it in the parameter set by attribute var."%>
<%@ attribute name="currentFacetField" required="false" type="org.apache.solr.client.solrj.response.FacetField" description="The FacetField for the current facet." %>
<%@ attribute name="facetValueCount" required="false" type="java.lang.Object" description="The FacetField.Count for the current facet value." %>
<%@ attribute name="currentActiveFacet" required="false" type="java.lang.Object" description="Alternatively the Map.Entry with KeyValue from the active facet filters variable." %>
<%@ attribute name="currentActiveFacetValue" required="false" type="java.lang.Object" description="The current Key/Value entry from the active facet filters variable." %>
<%@ attribute name="facetValueLabels" required="false" type="java.util.Map" description="Mapping between facet values and label." %>
<%@ attribute name="facetValueFormats" required="false" type="java.util.Map" description="Mapping between facet values and format." %>
<%@ variable name-given="facetValueLabel" scope="AT_END"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>

<c:set var="display" value="${functions:default(display, true)}"/>

<c:choose>
    <c:when test="${currentFacetField != null && facetValueCount != null}">
        <c:set var="currentFacetName" value="${currentFacetField.name}"/>
        <c:set var="facetValueName" value="${facetValueCount.name}"/>        
    </c:when>
    <c:otherwise>
        <c:set var="currentFacetName" value="${currentActiveFacet != null ? currentActiveFacet.key : ''}"/>
        <c:set var="facetValueName" value="${currentActiveFacetValue.key}"/>    
    </c:otherwise>
</c:choose>
<jcr:node var="refNode" uuid="${facetValueName}"/>
<c:choose>    
    <c:when test="${not empty refNode}">        
        <c:set var="mappedLabel" value="${refNode.name}"/>
    </c:when>
    <c:when test="${not empty facetValueLabels}">
        <c:forEach items="${facetValueLabels}" var="currentFacetValueLabel">
            <c:if test="${empty mappedLabel and fn:endsWith(facetValueName, currentFacetValueLabel.key)}">
                <c:set var="mappedLabel" value="${currentFacetValueLabel.value}"/>
            </c:if>
        </c:forEach>
    </c:when>
</c:choose>
<c:if test="${not empty facetValueFormats[currentFacetName]}">
    <c:set var="dateFieldFormat" value="${facetValueFormats[currentFacetName]}"/>
    <jsp:useBean id="dateFieldForFormatting" class="org.apache.solr.schema.DateField" scope="application"/>
    <% 
    Date date = null;
    SimpleDateFormat df = null;
    try {
        DateField dateField = (DateField)application.getAttribute("dateFieldForFormatting");
        date = dateField.toObject((String)jspContext.findAttribute("facetValueName"));
        RenderContext renderContext = (RenderContext)jspContext.findAttribute("renderContext");
        df = new SimpleDateFormat((String)jspContext.findAttribute("dateFieldFormat"), renderContext != null ? renderContext.getMainResource().getLocale() : Locale.ENGLISH);
    } catch (Exception e) {
    %>  <utility:logger value="<%=e.toString()%>" level="WARN"/> <%
    }
    %>
    <c:set var="mappedLabel" value="<%=df != null && date != null ? df.format(date) : null%>"/>
</c:if>
<c:set var="mappedLabel" value="${empty mappedLabel ? facetValueName : mappedLabel}"/>
<c:if test="${display}">
    ${mappedLabel}
</c:if>
<c:set var="facetValueLabel" value="${mappedLabel}"/>