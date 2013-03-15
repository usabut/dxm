<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="mailSettings" type="org.jahia.services.mail.MailSettings"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>

<script type="text/javascript">
    <!--
    function testSettings() {
        if (document.jahiaAdmin.host.value.length == 0) {
        <fmt:message key="message.mailServer_mustSet.label" var="msg"/>
            alert("${functions:escapeJavaScript(msg)}");
            document.jahiaAdmin.host.focus();
        } else if (document.jahiaAdmin.to.value.length == 0) {
        <fmt:message key="message.mailAdmin_mustSet" var="msg"/>
            alert("${functions:escapeJavaScript(msg)}");
            document.jahiaAdmin.to.focus();
        } else if (document.jahiaAdmin.from.value.length == 0) {
        <fmt:message key="message.mailFrom_mustSet.label" var="msg"/>
            alert("${functions:escapeJavaScript(msg)}");
            document.jahiaAdmin.from.focus();
        } else {
            if (typeof workInProgressOverlay != 'undefined') {
                workInProgressOverlay.start();
            }

            $.ajax({
                url:'${url.context}/cms/notification/testEmail',
                type:'POST',
                dataType:'text',
                cache:false,
                data:{
                    host:document.jahiaAdmin.host.value,
                    from:document.jahiaAdmin.from.value,
                    to:document.jahiaAdmin.to.value
                },
                success:function (data, textStatus) {
                    if (typeof workInProgressOverlay != 'undefined') {
                        workInProgressOverlay.stop();
                    }
                    if ("success" == textStatus) {
                    <fmt:message key="label.mailServer.testSettings.success" var="msg"/>
                        alert("${functions:escapeJavaScript(msg)}");
                    } else {
                    <fmt:message key="label.mailServer.testSettings.failure" var="msg"/>
                        alert("${functions:escapeJavaScript(msg)}");
                    }
                },
                error:function (xhr, textStatus, errorThrown) {
                    if (typeof workInProgressOverlay != 'undefined') {
                        workInProgressOverlay.stop();
                    }
                    <fmt:message key="label.mailServer.testSettings.failure" var="msg"/>
                    alert("${functions:escapeJavaScript(msg)}" + "\n" + xhr.status + " " + xhr.statusText + "\n" +
                          xhr.responseText);
                }
            });
        }
    }//-->
</script>

<div>
    <span style="font-size: larger; font: bold">
        <fmt:message key="label.emailSettings"/>
    </span>
    <p>
        <c:forEach items="${flowRequestContext.messageContext.allMessages}" var="message">
            <c:if test="${message.severity eq 'ERROR'}">
                <span style="color: red;">${message.text}</span><br/>
            </c:if>
        </c:forEach>
    </p>
    <div >
        <form name="jahiaAdmin" action='${flowExecutionUrl}' method="post">
            <table cellpadding="5" cellspacing="0" border="0">

                <tr>
                    <td>
                        <label for="serviceActivated">
                            <fmt:message key="label.mailserver.serviceEnabled"/>&nbsp;:
                        </label>
                    </td>
                    <td>
                        <input type="checkbox" name="serviceActivated" id="serviceActivated"<c:if test='${mailSettings.serviceActivated}'> checked="checked"</c:if>/>
                        <input type="hidden" name="_serviceActivated"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="label.mailServer.address"/>&nbsp;:
                    </td>
                    <td>
                        <input type="text" name="host" size="70" maxlength="250" value="<c:out value='${mailSettings.host}'/>"/>
                        &nbsp;
                        <a href="http://jira.jahia.org/browse/JKB-20" target="_blank" style="cursor: pointer;"><img src="${pageContext.request.contextPath}/engines/images/about.gif" alt="info" style="cursor: pointer;"/></a>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="label.mailServer.administrator"/>&nbsp;:
                    </td>
                    <td>
                        <input type="text" name="to" size="64" maxlength="250" value="<c:out value='${mailSettings.to}'/>">
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="label.mailServer.from"/>&nbsp;:
                    </td>
                    <td>
                        <input type="text" name="from" size="64" maxlength="250" value="<c:out value='${mailSettings.from}'/>">
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="label.mailServer.eventNotificationLevel"/>&nbsp;:
                    </td>
                    <td>
                        <select name="notificationLevel">
                            <option value="Disabled" ${mailSettings.notificationLevel == 'Disabled' ? 'selected="selected"' : ''}>
                                <fmt:message key="label.mailServer.eventNotificationLevel.disabled"/></option>
                            <option value="Standard" ${mailSettings.notificationLevel == 'Standard' ? 'selected="selected"' : ''}>
                                <fmt:message key="label.mailServer.eventNotificationLevel.standard"/></option>
                            <option value="Wary" ${mailSettings.notificationLevel == 'Wary' ? 'selected="selected"' : ''}>
                                <fmt:message key="label.mailServer.eventNotificationLevel.wary"/></option>
                            <option value="Paranoid" ${mailSettings.notificationLevel == 'Paranoid' ? 'selected="selected"' : ''}>
                                <fmt:message key="label.mailServer.eventNotificationLevel.paranoid"/></option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" align="right">
                        <a href="#" onclick="testSettings(); return false;"><fmt:message key="label.mailServer.testSettings"/></a>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" align="center">
                        <input type="submit" name="_eventId_submitMailSettings" value='<fmt:message key="label.save"/>'>
                    </td>
                </tr>
            </table>
        </form>
    </div>
</div>