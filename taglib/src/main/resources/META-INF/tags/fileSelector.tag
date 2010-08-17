<%@ tag body-content="empty" description="Renders the trigger link and the tree control to select a file." %>
<%@ attribute name="fieldId" required="true" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted item value with." %>
<%@ attribute name="displayFieldId" required="false" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted item display title with." %>
<%@ attribute name="label" required="false" type="java.lang.String"
              description="The trigger link text." %>
<%@ attribute name="onSelect" required="false" type="java.lang.String"
              description="The JavaScript function to be called after an item is selectd. Three paramaters are passed as arguments: node identifier, node path and display name. If the function retuns true, the value will be also set into the field value. Otherwise nothing will be done by this tag." %>
<%@ attribute name="nodeTypes" required="false" type="java.lang.String"
              description="Comma-separated list of node types to filter out the tree. [nt:folder,nt:file,jnt:virtualsite]" %>
<%@ attribute name="selectableNodeTypes" required="false" type="java.lang.String"
              description="Comma-separated list of node types that can be selected in the tree. [nt:file]" %>
<%@ attribute name="root" required="false" type="java.lang.String"
              description="The path of the root node for the tree. [current site path]" %>
<%@ attribute name="valueType" required="false" type="java.lang.String"
              description="Either identifier, path or title of the selected item. This value will be stored into the target field. [path]" %>
<%@ attribute name="fancyboxOptions" required="false" type="java.lang.String"
              description="In case the options for the jQuery FancyBox plugin needs to be overridden, they should be specified here in a form {option1: value1, opttion2: value2}. If option value is a literal, please, enclose it into single quotes." %>
<%@ attribute name="treeviewOptions" required="false" type="java.lang.String"
              description="In case the options for the jQuery Treeview plugin needs to be overridden, they should be specified here in a form {option1: value1, opttion2: value2}. If option value is a literal, please, enclose it into single quotes." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<c:if test="${empty label}"><c:set var="label"><fmt:message key="selectors.fileSelector.selectFile"/></c:set></c:if>
<uiComponents:treeItemSelector fieldId="${fieldId}" displayFieldId="${displayFieldId}" displayIncludeChildren="false"
	label="${label}" onSelect="${onSelect}"
	nodeTypes="${functions:default(nodeTypes, 'nt:folder,nt:file,jnt:virtualsite')}" selectableNodeTypes="${functions:default(selectableNodeTypes, 'nt:file')}"
	root="${root}" valueType="${valueType}" fancyboxOptions="${fancyboxOptions}" treeviewOptions="${treeviewOptions}"/>