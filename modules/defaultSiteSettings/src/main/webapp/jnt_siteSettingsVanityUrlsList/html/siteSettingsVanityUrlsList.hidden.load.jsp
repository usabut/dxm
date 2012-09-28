<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<c:set var="site" value="${currentNode.resolveSite}"/>

<query:definition var="listQuery" statement="select * from [jnt:vanityUrl] as vanity where ISDESCENDANTNODE(vanity,'${functions:sqlencode(site.path)}') order by vanity.[j:url]" scope="request"/>
<c:set target="${moduleMap}" property="listQuery" value="${listQuery}" />
<c:set target="${moduleMap}" property="subNodesView" value="sitesettings" />
