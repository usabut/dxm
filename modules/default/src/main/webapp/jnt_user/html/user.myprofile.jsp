<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<jsp:useBean id="now" class="java.util.Date"/>
<template:addResources type="css" resources="userProfile.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="jquery-ui.datepicker.min.js,jquery.jeditable.datepicker.js"/>

<c:set var="fields" value="${currentNode.propertiesAsString}"/>
<jcr:nodePropertyRenderer node="${currentNode}" name="j:title" renderer="resourceBundle" var="title"/>
<c:if test="${not empty title and not empty fields['j:firstName'] and not empty fields['j:lastName']}">
<c:set var="person" value="${title.displayName} ${fields['j:firstName']} ${fields['j:lastName']}"/>
</c:if>
<c:if test="${empty title and not empty fields['j:firstName'] and not empty fields['j:lastName']}">
<c:set var="person" value="${fields['j:firstName']} ${fields['j:lastName']}"/>
</c:if>
<c:if test="${empty title and empty fields['j:firstName'] and not empty fields['j:lastName']}">
<c:set var="person" value="${fields['j:lastName']}"/>
</c:if>
<c:if test="${empty title and not empty fields['j:firstName'] and empty fields['j:lastName']}">
<c:set var="person" value="${fields['j:firstName']}"/>
</c:if>
<c:if test="${empty title and empty fields['j:firstName'] and empty fields['j:lastName']}">
<c:set var="person" value=""/>
</c:if>
<jcr:nodeProperty node="${currentNode}" name="j:birthDate" var="birthDate"/>
<c:if test="${not empty birthDate}">
    <fmt:formatDate value="${birthDate.date.time}" pattern="yyyy" var="birthYear"/>
    <fmt:formatDate value="${now}" pattern="yyyy" var="currentYear"/>
</c:if>
<c:if test="${not empty birthDate}">
    <fmt:formatDate value="${birthDate.date.time}" pattern="dd/MM/yyyy" var="editBirthDate"/>
</c:if>
<fmt:formatDate value="${now}" pattern="dd/MM/yyyy" var="editNowDate"/>
<jcr:propertyInitializers node="${currentNode}" name="j:gender" var="genderInit"/>
<jcr:propertyInitializers node="${currentNode}" name="j:title" var="titleInit"/>
<script>

    var genderMap = "{<c:forEach items="${genderInit}" varStatus="status" var="gender"><c:if test="${status.index > 0}">,</c:if>'${gender.value.string}':'${gender.displayName}'</c:forEach>}";
    var titleMap = "{<c:forEach items="${titleInit}" varStatus="status" var="title"><c:if test="${status.index > 0}">,</c:if>'${title.value.string}':'${title.displayName}'</c:forEach>}";

    $(document).ready(function() {
        $(".edit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.basePreview}${currentNode.path}", data, function(result) {
                var j_title = "";
                if(result && typeof result.j_title != 'undefined')
                j_title = eval("datas="+titleMap)[result.j_title];
                var j_firstname = "";
                if(result && typeof result.j_firstName != 'undefined')
                j_firstname = result.j_firstName;
                var j_lastname = "";
                if(result && typeof result.j_lastName != 'undefined')
                j_lastname = result.j_lastName;
                $("#personDisplay2").html(j_title + " " + j_firstname + " " + j_lastname);
                $("#personDisplay1").html(j_title + " " + j_firstname + " " + j_lastname);
                if(result && result.j_email != 'undefined')
                $("#emailDisplay").html(result.j_email);
            }, "json");
            return(value);
        }, {
            type    : 'text',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : '<fmt:message key="label.clickToEdit"/>'
        });
        $(".visibilityEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.basePreview}${currentNode.path}", data, null, "json");
            if (value == "true")
                return "Public"; else
                return "Private";
        }, {
            type    : 'select',
            data   : "{'true':'Public','false':'Private'}",
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : '<fmt:message key="label.clickToEdit"/>'
        });

        $(".imageEdit").editable('${url.basePreview}${currentNode.path}', {
            type : 'ajaxupload',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : '<fmt:message key="label.clickToEdit"/>',
            callback : function (data, status) {
                uploadedImageCallback(data, status);
            }
        });

        function uploadedImageCallback(data, status) {
            var datas = {};
            datas['j:picture'] = data.uuids[0];
            datas['methodToCall'] = 'put';
            $.post('${url.basePreview}${currentNode.path}', datas, function(result) {
                var input = $('<div class="itemImage itemImageRight"><img src="' + result.j_picture + '/avatar_120" /></div>');
                $("#portrait").html(input);
            }, "json");
        }

        $(".ckeditorEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.basePreview}${currentNode.path}", data, function(result) {
            }, "json");
            return(value);
        }, {
            type : 'ckeditor',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : '<fmt:message key="label.clickToEdit"/>'
        });

        $(".dateEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
                data[submitId] = value;
                data['methodToCall'] = 'put';
                $.post("${url.basePreview}${currentNode.path}", data, function(result) {
                }, "json");
            return(value);
        }, {
            type : 'datepicker',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : '<fmt:message key="label.clickToEdit"/>'
        });

        $(".genderEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.basePreview}${currentNode.path}", data, null, "json");
            return eval("values="+genderMap)[value];
        }, {
            type    : 'select',
            data   : genderMap,
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : '<fmt:message key="label.clickToEdit"/>'
        });

        $(".titleEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.basePreview}${currentNode.path}", data, function(result) {
                var j_title = result.j_title;
                j_title = eval("datas="+titleMap)[j_title];
                $("#personDisplay2").html(j_title + " " + result.j_firstName + " " + result.j_lastName);
                $("#personDisplay1").html(j_title + " " + result.j_firstName + " " + result.j_lastName);
                $("#emailDisplay").html(result.j_email);
            }, "json");
            return eval("values="+titleMap)[value];
        }, {
            type    : 'select',
            data   : titleMap,
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : '<fmt:message key="label.clickToEdit"/>'
        });

    });
</script>
<%--map all display values --%>
<jsp:useBean id="userProperties" class="java.util.HashMap"/>

<div class='grid_4 alpha'><!--start grid_4-->
    <div class="image imageEdit" id="portrait">
        <div class="itemImage itemImageRight"><jcr:nodeProperty var="picture" node="${currentNode}" name="j:picture"/>
            <c:if test="${not empty picture}">
                <img src="${picture.node.thumbnailUrls['avatar_120']}" alt="${person}"/>
            </c:if>
            <c:if test="${empty picture}">
                <span><fmt:message key="jnt_user.profile.uploadPicture"/></span>
            </c:if>
        </div>
    </div>

            <div class="boxuserprofile">
                <div class=" boxuserprofilepadding16 boxuserprofilemarginbottom16">
                    <div class="boxuserprofile-inner">
                        <div class="boxuserprofile-inner-border"><!--start boxuserprofile -->

                    <h3 class="boxuserprofiletitleh3" id="personDisplay1"><c:out value="${person}"/></h3>

                    <div class="list3 user-profile-list">
                        <ul class="list3 user-profile-list">
                            <li><span class="label"><fmt:message
                                    key="jnt_user.profile.age"/> : </span> <utility:dateDiff startDate="${birthDate.date.time}" endDate="${now}" format="years"/> <fmt:message key="jnt_user.profile.years"/>
                            </li>
                            <li><span class="label"><fmt:message key="jnt_user.profile.sexe"/> : </span> <span
                                    class="genderEdit"
                                    id="j_gender"><jcr:nodePropertyRenderer node="${currentNode}" name="j:gender" renderer="resourceBundle"/></span>
                                <span class="visibilityEdit j_genderPublicEdit" id="j_genderPublic">
            <c:if test="${fields['j:genderPublic'] eq 'true'}">
                <fmt:message key="jnt_user.profile.public"/>
            </c:if>
            <c:if test="${fields['j:genderPublic'] eq 'false' or empty fields['j:genderPublic']}">
                <fmt:message key="jnt_user.profile.nonpublic"/>
            </c:if>
            </span>
                            </li>

                            <li><span class="label"><fmt:message key="jnt_user.j_email"/> : </span> <span id="j_email"
                                                                                                          class="edit">${fields['j:email']}</span><br/>
                                <span class="visibilityEdit" id="j_emailPublic">
                                <c:if test="${fields['j:emailPublic'] eq 'true'}">
                                    <fmt:message key="jnt_user.profile.public"/>
                                </c:if>
            <c:if test="${fields['j:emailPublic'] eq 'false' or empty fields['j:emailPublic']}">
                <fmt:message key="jnt_user.profile.nonpublic"/>
            </c:if></span></li>
                        </ul>
                    </div>
                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
    <!--stop box -->
            <div class="boxuserprofile">
                <div class="boxuserprofilegrey boxuserprofilepadding16 boxuserprofilemarginbottom16">
                    <div class="boxuserprofile-inner">
                        <div class="boxuserprofile-inner-border"><!--start boxuserprofile -->
                    <h3 class="boxuserprofiletitleh3"><fmt:message key="jnt_user.yourPreferences"/></h3>

                    <div class="preferencesForm"><!--start preferencesForm -->
                        <%--<jcr:preference name="preferredLanguage" var="prefLangNode"
                                        defaultValue="${renderContext.request.locale}"/>--%>
                        <fieldset>
                            <legend><fmt:message key="jnt_user.profile.preferences.form.name"/></legend>

                                <script type="text/javascript">
                                    $(document).ready(function() {
                                        $(".prefEdit").editable(function (value, settings) {
                                            var submitId = $(this).attr('id').replace("_", ":");
                                            var data = {};
                                            data[submitId] = value;
                                            data['methodToCall'] = 'put';
                                            $.post("${url.basePreview}${currentNode.path}", data, null, "json");
                                            <c:forEach items='${functions:availableAdminBundleLocale(renderContext.mainResourceLocale)}' var="adLocale" varStatus="status">
                                                <c:choose>
                                                    <c:when test="${status.first}">
                                                        if (value=="${adLocale}") return "${functions:capitalize(functions:displayLocaleNameWith(adLocale,adLocale))}";
                                                    </c:when>
                                            <c:otherwise>
                                            else if (value=="${adLocale}") return "${functions:capitalize(functions:displayLocaleNameWith(adLocale,adLocale))}";
                                            </c:otherwise>
                                                </c:choose>
                                            </c:forEach>
                                        }, {
                                            type    : 'select',
                                            data   : "{<c:forEach items='${functions:availableAdminBundleLocale(renderContext.mainResourceLocale)}' var="adLocale" varStatus="status"><c:if test="${not status.first}">,</c:if>'${adLocale}':'${functions:capitalize(functions:displayLocaleNameWith(adLocale,adLocale))}'</c:forEach>}",
                                            onblur : 'ignore',
                                            submit : 'OK',
                                            cancel : 'Cancel',
                                            tooltip : '<fmt:message key="label.clickToEdit"/>'
                                        });
                                    });
                                </script>
                            <label class="left"><fmt:message
                                    key="jnt_user.preferredLanguage"/></label>
                            <div class="prefEdit" id="preferredLanguage">
                                <c:choose>
                                    <c:when test="${not empty fields.preferredLanguage}">
                                        ${functions:capitalize(functions:displayLocaleNameWith(functions:toLocale(fields.preferredLanguage),functions:toLocale(fields.preferredLanguage)))}
                                    </c:when>
                                </c:choose>
                            </div>
                        </fieldset>
                    </div>
                    <!--stop sendMailForm -->

                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>

    <div class='clear'></div>
</div>
<!--stop grid_4-->


<div class='grid_8'><!--start grid_8-->

            <div class="boxuserprofile">
                <div class=" boxuserprofilepadding16 boxuserprofilemarginbottom16">
                    <div class="boxuserprofile-inner">
                        <div class="boxuserprofile-inner-border"><!--start boxuserprofile -->
                    <template:module node="${currentNode}" template="detailNew"/>
                    <!--stop box -->
                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
    <!--stop box -->

            <div class="boxuserprofile">
                <div class=" boxuserprofilepadding16 boxuserprofilemarginbottom16">
                    <div class="boxuserprofile-inner">
                        <div class="boxuserprofile-inner-border"><!--start boxuserprofile -->
                        
                    <span class="visibilityEdit" id="j:aboutPublic">
                    <c:if test="${fields['j:aboutPublic'] eq 'true'}">
                        <fmt:message key="jnt_user.profile.public"/>
                    </c:if>
                    <c:if test="${fields['j:aboutPublic'] eq 'false' or empty fields['j:aboutPublic']}">
                        <fmt:message key="jnt_user.profile.nonpublic"/>
                    </c:if>
                    </span>

                    <h3 class="boxuserprofiletitleh3"><fmt:message key="jnt_user.j_about"/></h3>
                    <div class="ckeditorEdit j_aboutEdit" id="j_about">${fields['j:about']}</div>
                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
    <!--stop box -->


</div>
<!--stop grid_8-->
<div class='grid_4 omega'><!--start grid_4-->
    <%--<div class="boxuserprofile">
                <div class="boxuserprofilegrey boxuserprofilepadding16 boxuserprofilemarginbottom16">
                    <div class="boxuserprofile-inner">
                        <div class="boxuserprofile-inner-border"><!--start boxuserprofile -->

              <div class="thumbnail">
                <a href="#"><img src="img-text/rss.png" alt="" border="0"/></a>
              <div class='clear'></div></div>
              <h3 class="boxuserprofiletitleh3"><a href="#">Follow me</a></h3>
              <p>dolor sit amet, consectetuer adipiscing elit. Morbi adipiscing, metus non ultricies pharetra</p>
                          <div class="clear"></div>

                    </div>
              </div>
          </div>
  </div>--%><!--stop box -->

    <h3 class="user-profile-title-icon"><a href="#"><fmt:message key="jnt_user.profile.groups"/><img title="" alt=""
                                                                                       src="${url.currentModule}/images/groups.png"/></a>
    </h3>
    <ul class="group-list">
        <c:forEach items="${jcr:getUserMembership(currentNode)}" var="group" varStatus="status">
            <li <c:if test="${status.last}">class="last"</c:if>>
                <div class="thumbnail">
                    <a href="#"><img src="${url.currentModule}/images/group-icon.png" alt="group" border="0"/></a>

                </div>
                <h4>
                    <jcr:node var="node" path="${group.value.properties['j:fullpath']}"/>
                    <a href="${url.base}${group.value.properties['j:fullpath']}.html?jsite=${node.resolveSite.identifier}">${group.value.groupname}(${fn:length(group.value.members)})</a>
                </h4>

                <div class='clear'></div>
            </li>
        </c:forEach>
    </ul>

    <!--stop box -->
    <%--

            <h3 class="user-profile-title-icon">Friends<img title="" alt="" src="img-text/friends.png"/></h3>
            <ul class="friends-list">
    <li>
                <div class="thumbnail">
                  <a href="#"><img src="img-text/friend.png" alt="friend" border="0"/></a>            </div>
                <h4><a href="#"> Follower</a></h4>
    <div class='clear'></div></li>
    <li><div class="thumbnail"><a href="#"><img src="img-text/friend.png" alt="friend" border="0"/></a></div>

                <h4><a href="#">Follower</a></h4>
                <div class='clear'></div></li>
                <li><div class="thumbnail"><a href="#"><img src="img-text/friend.png" alt="friend" border="0"/></a></div>
                <h4><a href="#">Follower</a></h4>
                <div class='clear'></div></li>
                <li><div class="thumbnail"><a href="#"><img src="img-text/friend.png" alt="friend" border="0"/></a></div>
                <h4><a href="#">Follower</a></h4>

                <div class='clear'></div></li>
                <li><div class="thumbnail"><a href="#"><img src="img-text/friend.png" alt="friend" border="0"/></a></div>
                <h4><a href="#">Follower</a></h4>
                <div class='clear'></div></li>
                <li><div class="thumbnail"><a href="#"><img src="img-text/friend.png" alt="friend" border="0"/></a></div>
                <h4><a href="#">Follower</a></h4>
                <div class='clear'></div></li>
    <li class="last">

                <div class="thumbnail">
                <a href="#"><img src="img-text/friend.png" alt="friend" border="0"/></a>            </div><h4><a href="#"> Follower</a></h4>
                <div class='clear'></div></li>
            </ul>

    --%>


    <!--stop box -->


    <%--            <div class="boxuserprofile">
                <div class="boxuserprofilegrey boxuserprofilepadding16 boxuserprofilemarginbottom16">
                    <div class="boxuserprofile-inner">
                        <div class="boxuserprofile-inner-border"><!--start boxuserprofile -->
                    <h3 class="boxuserprofiletitleh3">M’envoyer un email</h3>

                    <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. In ut sapien at nulla ultrices volutpat
                        vel nec velit. Ut vel tortor tellus.
                    </p>

                    <div class="sendMailForm">
                        <!--start sendMailForm -->
                        <form method="post" action="#">
                            <fieldset>
                                <legend>Send mail</legend>
                                <p class="field">
                                    <label for="from">From :</label>
                                    <input type="text" name="from" id="from" class="sendMailFormFrom"
                                           value="Votre email" tabindex="6"/>

                                </p>

                                <p class="field">
                                    <label for="message">Message :</label>
                                    <textarea rows="7" cols="35" id="message" name="message" tabindex="7">Votre
                                        message</textarea>
                                </p>

                                <div class="divButton">
                                    <a class="aButton" tabindex="8" href="#"><span>Send mail</span></a>

                                    <div class="clear"></div>
                                </div>
                            </fieldset>
                        </form>
                    </div>
                    <!--stop sendMailForm -->
                    <div class="clear"></div>
                </div>
            </div>
        </div>

    </div>--%>
    <!--stop box -->
    <div class='clear'></div>
</div>
<!--stop grid_4-->


<div class='clear'></div>

