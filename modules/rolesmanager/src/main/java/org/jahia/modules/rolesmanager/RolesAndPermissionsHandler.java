package org.jahia.modules.rolesmanager;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.io.Serializable;
import java.util.*;

public class RolesAndPermissionsHandler implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(RolesAndPermissionsHandler.class);

//    enum Scope {CONTENT, SITE, SERVER_SETTINGS, STUDIO, JCR, OTHER};


    @Autowired
    private transient RoleTypeConfiguration roleTypes;

    @Autowired
    private transient JahiaTemplateManagerService templateManagerService;

    private RoleBean roleBean = new RoleBean();

    private String currentContext;
    private String currentGroup;
    private List<String> uuids;

    private transient List<JCRNodeWrapper> allPermissions;

    public RolesAndPermissionsHandler() {
    }

    public RoleTypeConfiguration getRoleTypes() {
        return roleTypes;
    }

    public RoleBean getRoleBean() {
        return roleBean;
    }

    public void setRoleBean(RoleBean roleBean) {
        this.roleBean = roleBean;
        this.currentContext = "current";
        this.currentGroup = roleBean.getPermissions().get(currentContext).keySet().iterator().next();
    }

    private JCRSessionWrapper getSession() throws RepositoryException {
        return JCRSessionFactory.getInstance().getCurrentUserSession("default", LocaleContextHolder.getLocale());
    }

    public Map<String, List<RoleBean>> getRoles() throws RepositoryException {
        return getRoles(false,false);
    }

    public Map<String, List<RoleBean>> getRolesToDelete() throws RepositoryException {
        return getRoles(true,true);
    }


    public Map<String, List<RoleBean>> getSelectedRoles() throws RepositoryException {
        return getRoles(true,false);
    }

    public Map<String, List<RoleBean>> getRoles(boolean filterUUIDs, boolean getChildren) throws RepositoryException {

        QueryManager qm = getSession().getWorkspace().getQueryManager();
        String statement = "select * from [jnt:role] as role";
        if (filterUUIDs) {
            statement += " where ";
            Iterator<String> it = uuids.iterator();
            while (it.hasNext()) {
                String uuid = it.next();
                statement += "[jcr:uuid] = '" + uuid + "'";
                if (getChildren) {
                    statement +=  " or isdescendantnode(role, ['" + getSession().getNodeByIdentifier(uuid).getPath() + "'])";
                }
                if (it.hasNext()) {
                    statement += " or ";
                }
            }
        }
        Query q = qm.createQuery(statement, Query.JCR_SQL2);
        Map<String, List<RoleBean>> all = new LinkedHashMap<String, List<RoleBean>>();
        if (!filterUUIDs) {
            for (RoleType roleType : roleTypes.getValues()) {
                all.put(roleType.getName(), new ArrayList<RoleBean>());
            }
        }

        NodeIterator ni = q.execute().getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            if (!next.getName().equals("privileged")) {
                RoleBean role = getRole(next.getIdentifier(), false);
                String key = role.getRoleType().getName();
                if (!all.containsKey(key)) {
                    all.put(key, new ArrayList<RoleBean>());
                }
                all.get(key).add(role);
            }
        }
        for (List<RoleBean> roleBeans : all.values()) {
            Collections.sort(roleBeans, new Comparator<RoleBean>() {
                @Override
                public int compare(RoleBean o1, RoleBean o2) {
                    return o1.getPath().compareTo(o2.getPath());
                }
            });
        }

        return all;
    }

    public RoleBean getRole(String uuid, boolean getPermissions) throws RepositoryException {
        JCRSessionWrapper currentUserSession = getSession();

        JCRNodeWrapper role = currentUserSession.getNodeByIdentifier(uuid);

        RoleBean roleBean = new RoleBean();
        roleBean.setUuid(role.getIdentifier());
        roleBean.setName(role.getName());
        roleBean.setPath(role.getPath());
        roleBean.setDepth(role.getDepth());
        if (role.hasProperty("jcr:title")) {
            roleBean.setTitle(role.getProperty("jcr:title").getString());
        }
        if (role.hasProperty("jcr:description")) {
            roleBean.setDescription(role.getProperty("jcr:description").getString());
        }
        if (role.hasProperty("j:hidden")) {
            roleBean.setHidden(role.getProperty("j:hidden").getBoolean());
        }

        String roleGroup = role.getProperty("j:roleGroup").getString();

        RoleType roleType = roleTypes.get(roleGroup);
        roleBean.setRoleType(roleType);
        if (getPermissions) {
            List<String> tabs = new ArrayList<String>(roleBean.getRoleType().getScopes());

            Map<String, List<String>> permIdsMap = new HashMap<String, List<String>>();
            fillPermIds(role, tabs, permIdsMap, false);

            Map<String, List<String>> inheritedPermIdsMap = new HashMap<String, List<String>>();
            fillPermIds(role.getParent(), tabs, inheritedPermIdsMap, true);


            Map<String, Map<String, Map<String, PermissionBean>>> permsForRole = new LinkedHashMap<String, Map<String, Map<String, PermissionBean>>>();
            roleBean.setPermissions(permsForRole);

            for (String tab : tabs) {
                addPermissionsForScope(roleBean, tab, permIdsMap, inheritedPermIdsMap);
            }

            if (roleType.getAvailableNodeTypes() != null) {
                List<String> nodeTypesOnRole = new ArrayList<String>();
                if (role.hasProperty("j:nodeTypes")) {
                    for (Value value : role.getProperty("j:nodeTypes").getValues()) {
                        nodeTypesOnRole.add(value.getString());
                    }
                }


                SortedSet<NodeType> nodeTypes = new TreeSet<NodeType>();
                for (String s : roleType.getAvailableNodeTypes()) {
                    boolean includeSubtypes = false;
                    if (s.endsWith("/*")) {
                        s = StringUtils.substringBeforeLast(s,"/*");
                        includeSubtypes = true;
                    }
                    ExtendedNodeType t = NodeTypeRegistry.getInstance().getNodeType(s);
                    nodeTypes.add(new NodeType(t.getName(),t.getLabel(LocaleContextHolder.getLocale()),nodeTypesOnRole.contains(t.getName())));
                    if (includeSubtypes) {
                        for (ExtendedNodeType sub : t.getSubtypesAsList()) {
                            nodeTypes.add(new NodeType(sub.getName(),sub.getLabel(LocaleContextHolder.getLocale()),nodeTypesOnRole.contains(sub.getName())));
                        }
                    }
                }
                roleBean.setNodeTypes(nodeTypes);
            }
        }

        return roleBean;
    }

    public void revertRole() throws RepositoryException {
        roleBean = getRole(roleBean.getUuid(), true);
    }

    private void fillPermIds(JCRNodeWrapper role, List<String> tabs, Map<String, List<String>> permIdsMap, boolean recursive) throws RepositoryException {
        if (!role.isNodeType(Constants.JAHIANT_ROLE)) {
            return;
        }

        if (recursive) {
            fillPermIds(role.getParent(), tabs, permIdsMap, true);
        }

        final ArrayList<String> setPermIds = new ArrayList<String>();
        permIdsMap.put("current", setPermIds);

        if (role.hasProperty("j:permissionNames")) {
            Value[] values = role.getProperty("j:permissionNames").getValues();
            for (Value value : values) {
                String valueString = value.getString();
                if (!setPermIds.contains(valueString)) {
                    setPermIds.add(valueString);
                }
            }
        }


        NodeIterator ni = role.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            if (next.isNodeType("jnt:externalPermissions")) {
                try {
                    String path = next.getProperty("j:path").getString();
                    permIdsMap.put(path, new ArrayList<String>());
                    Value[] values = next.getProperty("j:permissionNames").getValues();
                    for (Value value : values) {
                        List<String> ids = permIdsMap.get(path);
                        String valueString = value.getString();
                        if (!ids.contains(valueString)) {
                            ids.add(valueString);
                        }
                        if (!tabs.contains(path)) {
                            tabs.add(path);
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error("Cannot initialize role " + next.getPath(), e);
                } catch (IllegalStateException e) {
                    logger.error("Cannot initialize role " + next.getPath(), e);
                }
            }
        }
    }

    public boolean addRole(String roleName, String parentRoleId, String roleTypeString, MessageContext messageContext) throws RepositoryException {
        JCRSessionWrapper currentUserSession = getSession();

        if (StringUtils.isBlank(roleName)) {
            messageContext.addMessage(new MessageBuilder().source("roleName")
                    .defaultText(getMessage("rolesmanager.rolesAndPermissions.role.noName"))
                    .error()
                    .build());
            return false;
        }
        roleName = JCRContentUtils.generateNodeName(roleName);

        NodeIterator nodes = currentUserSession.getWorkspace().getQueryManager().createQuery(
                "select * from [" + Constants.JAHIANT_ROLE + "] as r where localname()='" + roleName + "' and isdescendantnode(r,['/roles'])",
                Query.JCR_SQL2).execute().getNodes();
        if (nodes.hasNext()) {
            messageContext.addMessage(new MessageBuilder().source("roleName")
                    .defaultText(getMessage("rolesmanager.rolesAndPermissions.role.exists"))
                    .error()
                    .build());
            return false;
        }

        JCRNodeWrapper parent;
        if (StringUtils.isBlank(parentRoleId)) {
            parent = currentUserSession.getNode("/roles");
        } else {
            parent = currentUserSession.getNodeByIdentifier(parentRoleId);
        }
        JCRNodeWrapper role = parent.addNode(roleName, "jnt:role");
        RoleType roleType = roleTypes.get(roleTypeString);
        role.setProperty("j:roleGroup", roleType.getName());
        role.setProperty("j:privilegedAccess", roleType.isPrivileged());
        if (roleType.getDefaultNodeTypes() != null) {
            List<Value> values = new ArrayList<Value>();
            for (String nodeType : roleType.getDefaultNodeTypes()) {
                values.add(currentUserSession.getValueFactory().createValue(nodeType));
            }
            role.setProperty("j:nodeTypes", values.toArray(new Value[values.size()]));
        }
        role.setProperty("j:roleGroup", roleType.getName());

        currentUserSession.save();
        this.setRoleBean(getRole(role.getIdentifier(), true));
        return true;
    }

    public void selectRoles(String uuids) throws RepositoryException {
        this.uuids = Arrays.asList(uuids.split(","));
    }

    public boolean deleteRoles() throws RepositoryException {
        JCRSessionWrapper currentUserSession = getSession();
        for (String uuid : uuids) {
            try {
                currentUserSession.getNodeByIdentifier(uuid).remove();
            } catch (ItemNotFoundException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cannot find role " + uuid);
                }
            }
        }
        currentUserSession.save();
        return true;
    }

    private void addPermissionsForScope(RoleBean roleBean, String scope, Map<String, List<String>> permIdsMap, Map<String, List<String>> inheritedPermIdsMap) throws RepositoryException {
        final Map<String, Map<String, Map<String, PermissionBean>>> permissions = roleBean.getPermissions();
        if (!permissions.containsKey(scope)) {
            permissions.put(scope, new LinkedHashMap<String, Map<String, PermissionBean>>());
        }
        List<JCRNodeWrapper> allPermissions = getAllPermissions();

        String type = null;
        final Map<String, List<String>> globalPermissionsGroups = roleTypes.getPermissionsGroups();
        final Map<String, List<String>> permissionsGroupsForRoleType = roleBean.getRoleType().getPermissionsGroups();

        if (scope.equals("current")) {
            if (roleBean.getRoleType().getDefaultNodeTypes() != null && roleBean.getRoleType().getDefaultNodeTypes().size() == 1) {
                type = roleBean.getRoleType().getDefaultNodeTypes().get(0);
            }
        } else {
            if (scope.equals("currentSite")) {
                type = "jnt:virtualsite";
            } else {
                try {
                    type = getSession().getNode(scope).getPrimaryNodeTypeName();
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }
        }
        if (type == null || (!globalPermissionsGroups.containsKey(type) && (permissionsGroupsForRoleType == null || !permissionsGroupsForRoleType.containsKey(type)))) {
            type = "nt:base";
        }
        if (permissionsGroupsForRoleType != null && permissionsGroupsForRoleType.containsKey(type)) {
            for (String s : permissionsGroupsForRoleType.get(type)) {
                permissions.get(scope).put(s, new TreeMap<String, PermissionBean>());
            }
        } else {
            for (String s : globalPermissionsGroups.get(type)) {
                permissions.get(scope).put(s, new TreeMap<String, PermissionBean>());
            }
        }

        Map<String, PermissionBean> mappedPermissions = new HashMap<String, PermissionBean>();

        Map<String, String> allGroups = new HashMap<String, String>();
        for (String s : permissions.get(scope).keySet()) {
            for (String s1 : Arrays.asList(s.split(","))) {
                allGroups.put(s1, s);
            }
        }

        // Create mapped permissions
        for (Map.Entry<String, List<String>> entry : roleTypes.getPermissionsMapping().entrySet()) {
            String[] splitPath = entry.getKey().split("/");
            String permissionGroup = splitPath[2];
            if (allGroups.containsKey(permissionGroup)) {
                Map<String, PermissionBean> p = permissions.get(scope).get(allGroups.get(permissionGroup));
                PermissionBean bean = new PermissionBean();
                bean.setUuid(null);
                bean.setParentPath(StringUtils.substringBeforeLast(entry.getKey(), "/"));
                bean.setName(StringUtils.substringAfterLast(entry.getKey(), "/"));
                String localName = StringUtils.substringAfterLast(entry.getKey(), "/");
                if (localName.contains(":")) {
                    localName = StringUtils.substringAfter(localName, ":");
                }
                String title = StringUtils.capitalize(localName.replaceAll("([A-Z])", " $0").replaceAll("[_-]", " ").toLowerCase());
                final String rbName = localName.replaceAll("-", "_");
                bean.setTitle(Messages.getInternal("label.permission." + rbName, LocaleContextHolder.getLocale(), title));
                bean.setDescription(Messages.getInternal("label.permission." + rbName + ".description", LocaleContextHolder.getLocale(), ""));
                bean.setPath(entry.getKey());
                bean.setDepth(splitPath.length - 1);
                bean.setMappedPermissions(new TreeMap<String, PermissionBean>());
                if (p.containsKey(bean.getParentPath())) {
                    p.get(bean.getParentPath()).setHasChildren(true);
                }

                p.put(entry.getKey(), bean);

                for (String s : entry.getValue()) {
                    createMappedPermission(s, bean, mappedPermissions);
                }
            }
        }

        // Create standard permissions
        for (JCRNodeWrapper permissionNode : allPermissions) {
            JCRNodeWrapper permissionGroup = getPermissionGroupNode(permissionNode);
            final String permissionPath = getPermissionPath(permissionNode);

            if (!mappedPermissions.containsKey(permissionPath) && mappedPermissions.containsKey(getPermissionPath(permissionNode.getParent()))) {
                final PermissionBean bean = mappedPermissions.get(getPermissionPath(permissionNode.getParent()));
                createMappedPermission(permissionPath, bean, mappedPermissions);
            }

            if (allGroups.containsKey(permissionGroup.getName()) && !mappedPermissions.containsKey(permissionPath)) {
                Map<String, PermissionBean> p = permissions.get(scope).get(allGroups.get(permissionGroup.getName()));
                if (!p.containsKey(permissionPath) || permissionNode.getPath().startsWith("/permissions")) {
                    PermissionBean bean = new PermissionBean();
                    setPermissionBeanProperties(permissionNode, bean);
                    if (p.containsKey(bean.getParentPath())) {
                        p.get(bean.getParentPath()).setHasChildren(true);
                    }
                    p.put(permissionPath, bean);
                    setPermissionFlags(permissionNode, p, bean, permIdsMap.get(scope), inheritedPermIdsMap.get(scope), p.get(bean.getParentPath()));
                }
            }
            if (mappedPermissions.containsKey(permissionPath)) {
                PermissionBean bean = mappedPermissions.get(permissionPath);

                Map<String, PermissionBean> p = permissions.get(scope).get(allGroups.get(bean.getPath().split("/")[2]));
                setPermissionFlags(permissionNode, p, bean, permIdsMap.get(scope), inheritedPermIdsMap.get(scope), p.get(bean.getParentPath()));
            }
        }

        // Auto expand permissions where mapped permissions are partially set
        for (Map<String, Map<String, PermissionBean>> map : roleBean.getPermissions().values()) {
            for (Map<String, PermissionBean> map2 : map.values()) {
                final Collection<PermissionBean> values = new ArrayList<PermissionBean>(map2.values());
                for (PermissionBean bean : values) {
                    if (bean.getMappedPermissions() != null) {
                        Boolean lastValue = null;
                        for (PermissionBean value : bean.getMappedPermissions().values()) {
                            if (lastValue == null) {
                                lastValue = value.isSuperSet() || value.isSet();
                            }
                            if (!lastValue.equals(value.isSuperSet() || value.isSet())) {
                                bean.setMappedPermissionsExpanded(true);
                                bean.setSet(false);
                                bean.setSuperSet(false);
                                bean.setPartialSet(true);
                                break;
                            }
                        }
                        if (bean.isMappedPermissionsExpanded()) {
                            for (PermissionBean mapped : bean.getMappedPermissions().values()) {
                                map2.put(mapped.getPath(), mapped);
                            }
                        }
                    }
                }
            }
        }
    }

    private void createMappedPermission(String permissionPath, PermissionBean parent, Map<String, PermissionBean> mappedPermissions) throws RepositoryException {
        PermissionBean mapped = new PermissionBean();
        setPermissionBeanProperties(getSession().getNode(permissionPath), mapped);
        mapped.setPath(parent.getPath() + "/" + mapped.getName());
        mapped.setParentPath(parent.getPath());
        mapped.setDepth(parent.getDepth() + 1);
        parent.getMappedPermissions().put(permissionPath, mapped);
        mappedPermissions.put(permissionPath, parent);
    }

    private void setPermissionFlags(JCRNodeWrapper permissionNode, Map<String, PermissionBean> permissions, PermissionBean bean, List<String> permIds, List<String> inheritedPermIds, PermissionBean parentBean) throws RepositoryException {
        if ((permIds != null && permIds.contains(permissionNode.getName()))
                || (parentBean != null && parentBean.isSet())) {
            bean.setSet(true);
            if (bean.getMappedPermissions() != null && bean.getMappedPermissions().containsKey(permissionNode.getPath())) {
                bean.getMappedPermissions().get(permissionNode.getPath()).setSet(true);
            }
            while (parentBean != null && !parentBean.isSet() && !parentBean.isSuperSet()) {
                parentBean.setPartialSet(true);
                parentBean = permissions.get(parentBean.getParentPath());
            }
        }
        parentBean = permissions.get(bean.getParentPath());
        if ((inheritedPermIds != null && inheritedPermIds.contains(permissionNode.getName()))
                || (parentBean != null && parentBean.isSuperSet())) {
            bean.setSuperSet(true);
            if (bean.getMappedPermissions() != null && bean.getMappedPermissions().containsKey(permissionNode.getPath())) {
                bean.getMappedPermissions().get(permissionNode.getPath()).setSuperSet(true);
            }
            while (parentBean != null && !parentBean.isSet() && !parentBean.isSuperSet()) {
                parentBean.setPartialSet(true);
                parentBean = permissions.get(parentBean.getParentPath());
            }
        }
    }

    private String getPermissionPath(JCRNodeWrapper permissionNode) {
        String path = permissionNode.getPath();
        if (path.startsWith("/modules")) {
            path = "/permissions/" + StringUtils.substringAfter(path, "/permissions/");
        }
        return path;
    }

    private String getPermissionModule(JCRNodeWrapper permissionNode) {
        String path = permissionNode.getPath();
        if (path.startsWith("/modules/")) {
            String s = StringUtils.substringAfter(path, "/modules/");
            return StringUtils.substringBefore(s, "/");
        }
        return null;
    }

    private int getPermissionDepth(JCRNodeWrapper permissionNode) throws RepositoryException {
        String path = permissionNode.getPath();
        if (path.startsWith("/modules")) {
            return permissionNode.getDepth() - 3;
        }
        return permissionNode.getDepth();
    }

    private JCRNodeWrapper getPermissionGroupNode(JCRNodeWrapper permissionNode) throws RepositoryException {
        JCRNodeWrapper permissionGroup = (JCRNodeWrapper) permissionNode.getAncestor(2);
        if (permissionGroup.isNodeType("jnt:module")) {
            permissionGroup = (JCRNodeWrapper) permissionNode.getAncestor(5);
        }
        return permissionGroup;
    }

    private void setPermissionBeanProperties(JCRNodeWrapper permissionNode, PermissionBean bean) throws RepositoryException {
        final String module = getPermissionModule(permissionNode);

        bean.setUuid(permissionNode.getIdentifier());

        bean.setParentPath(getPermissionPath(permissionNode.getParent()));
        bean.setName(permissionNode.getName());
        String localName = permissionNode.getName();
        if (localName.contains(":")) {
            localName = StringUtils.substringAfter(localName, ":");
        }
        String title = StringUtils.capitalize(localName.replaceAll("([A-Z])", " $0").replaceAll("[_-]", " ").toLowerCase());
        final String rbName = localName.replaceAll("-", "_");
        if (module != null) {
            bean.setTitle(Messages.get(templateManagerService.getTemplatePackageByFileName(module), "label.permission." + rbName, LocaleContextHolder.getLocale(), title));
        } else {
            bean.setTitle(Messages.getInternal("label.permission." + rbName, LocaleContextHolder.getLocale(), title));
        }
        bean.setModule(module);
        bean.setDescription(Messages.getInternal("label.permission." + rbName + ".description", LocaleContextHolder.getLocale(), ""));
        bean.setPath(getPermissionPath(permissionNode));
        bean.setDepth(getPermissionDepth(permissionNode));
    }

    public String getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(String tab) {
        currentContext = tab;
        this.currentGroup = roleBean.getPermissions().get(currentContext).keySet().iterator().next();
    }

    public String getCurrentGroup() {
        return currentGroup;
    }

    public void setCurrentGroup(String context, String currentGroup) {
        setCurrentContext(context);
        this.currentGroup = currentGroup;
    }

    public void storeValues(String[] selectedValues, String[] partialSelectedValues) {
        Map<String, PermissionBean> permissionBeans = roleBean.getPermissions().get(currentContext).get(currentGroup);
        List<String> perms = selectedValues != null ? Arrays.asList(selectedValues) : new ArrayList<String>();
        for (PermissionBean permissionBean : permissionBeans.values()) {
            if (permissionBean.isSet() != perms.contains(permissionBean.getPath())) {
                roleBean.setDirty(true);
                permissionBean.setSet(perms.contains(permissionBean.getPath()));
                if (!permissionBean.isMappedPermissionsExpanded() && permissionBean.getMappedPermissions() != null) {
                    for (PermissionBean mapped : permissionBean.getMappedPermissions().values()) {
                        mapped.setSet(perms.contains(permissionBean.getPath()));
                    }
                }
            }

        }

        perms = partialSelectedValues != null ? Arrays.asList(partialSelectedValues) : new ArrayList<String>();
        for (PermissionBean permissionBean : permissionBeans.values()) {
            if (permissionBean.isPartialSet() != perms.contains(permissionBean.getPath())) {
                roleBean.setDirty(true);
                permissionBean.setPartialSet(perms.contains(permissionBean.getPath()));
                if (!permissionBean.isMappedPermissionsExpanded() && permissionBean.getMappedPermissions() != null) {
                    for (PermissionBean mapped : permissionBean.getMappedPermissions().values()) {
                        mapped.setPartialSet(perms.contains(permissionBean.getPath()));
                    }
                }
            }
        }
    }

    public void addContext(String newContext) throws RepositoryException {
        if (!newContext.startsWith("/")) {
            return;
        }

        if (!roleBean.getPermissions().containsKey(newContext)) {
            addPermissionsForScope(roleBean, newContext, new HashMap<String, List<String>>(), new HashMap<String, List<String>>());
        }
        setCurrentContext(newContext);
    }

    public void removeContext(String scope) throws RepositoryException {

        if (roleBean.getPermissions().containsKey(scope)) {
            roleBean.getPermissions().remove(scope);
        }
        if (currentContext.equals(scope)) {
            setCurrentContext(roleBean.getPermissions().keySet().iterator().next());
        }
    }

    public void save() throws RepositoryException {
        JCRSessionWrapper currentUserSession = getSession();

        Map<String, List<Value>> permissions = new HashMap<String, List<Value>>();

        for (Map.Entry<String, Map<String, Map<String, PermissionBean>>> entry : roleBean.getPermissions().entrySet()) {
            ArrayList<Value> permissionValues = new ArrayList<Value>();
            permissions.put(entry.getKey(), permissionValues);
            for (Map<String, PermissionBean> map : entry.getValue().values()) {
                for (PermissionBean bean : map.values()) {
                    PermissionBean parentBean = map.get(bean.getParentPath());
                    if (bean.isSet() && (parentBean == null || !parentBean.isSet())) {
                        if (bean.getMappedPermissions() != null) {
                            for (PermissionBean mapped : bean.getMappedPermissions().values()) {
                                if (mapped.isSet()) {
                                    permissionValues.add(currentUserSession.getValueFactory().createValue(mapped.getName(), PropertyType.STRING));
                                }
                            }
                        } else {
                            permissionValues.add(currentUserSession.getValueFactory().createValue(bean.getName(), PropertyType.STRING));
                        }
                    }
                }
            }
        }

        JCRNodeWrapper role = currentUserSession.getNodeByIdentifier(roleBean.getUuid());
        Set<String> externalPermissionNodes = new HashSet<String>();
        for (Map.Entry<String, List<Value>> s : permissions.entrySet()) {
            String key = s.getKey();
            if (key.equals("current")) {
                role.setProperty("j:permissionNames", permissions.get("current").toArray(new Value[permissions.get("current").size()]));
            } else {
                if (key.equals("/")) {
                    key = "root-access";
                } else {
                    key = ISO9075.encode((key.startsWith("/") ? key.substring(1) : key).replace("/", "-")) + "-access";
                }
                if (!s.getValue().isEmpty()) {
                    if (!role.hasNode(key)) {
                        JCRNodeWrapper extPermissions = role.addNode(key, "jnt:externalPermissions");
                        extPermissions.setProperty("j:path", s.getKey());
                        extPermissions.setProperty("j:permissionNames", s.getValue().toArray(new Value[s.getValue().size()]));
                    } else {
                        role.getNode(key).setProperty("j:permissionNames", s.getValue().toArray(new Value[s.getValue().size()]));
                    }
                    externalPermissionNodes.add(key);
                }
            }
        }
        NodeIterator ni = role.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            if (next.getPrimaryNodeTypeName().equals("jnt:externalPermissions") && !externalPermissionNodes.contains(next.getName())) {
                next.remove();
            }
        }
        role.setProperty("jcr:title", roleBean.getTitle());
        role.setProperty("jcr:description", roleBean.getDescription());
        role.setProperty("j:hidden", roleBean.isHidden());
        if (roleBean.getNodeTypes() != null) {
            List<Value> values = new ArrayList<Value>();
            for (NodeType nodeType : roleBean.getNodeTypes()) {
                if (nodeType.isSet()) {
                    values.add(currentUserSession.getValueFactory().createValue(nodeType.getName()));
                }
            }
            role.setProperty("j:nodeTypes", values.toArray(new Value[values.size()]));
        }
        roleBean.setDirty(false);
        currentUserSession.save();
    }

    public List<JCRNodeWrapper> getAllPermissions() throws RepositoryException {
        if (allPermissions != null) {
            return allPermissions;
        }

        allPermissions = new ArrayList<JCRNodeWrapper>();
        JCRSessionWrapper currentUserSession = getSession();

        QueryManager qm = currentUserSession.getWorkspace().getQueryManager();
        String statement = "select * from [jnt:permission]";

        Query q = qm.createQuery(statement, Query.JCR_SQL2);
        NodeIterator ni = q.execute().getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            int depth = 2;
            if (((JCRNodeWrapper) next.getAncestor(1)).isNodeType("jnt:modules")) {
                depth = 5;
                JahiaTemplatesPackage pack = templateManagerService.getTemplatePackageByFileName(next.getAncestor(2).getName());
                if (pack == null || !pack.getVersion().toString().equals(next.getAncestor(3).getName())) {
                    continue;
                }
            }
            if (next.getDepth() >= depth) {
                allPermissions.add(next);
            }
        }
        Collections.sort(allPermissions, new Comparator<JCRNodeWrapper>() {
            @Override
            public int compare(JCRNodeWrapper o1, JCRNodeWrapper o2) {
                if (getPermissionPath(o1).equals(getPermissionPath(o2))) {
                    return -o1.getPath().compareTo(o2.getPath());
                }
                return getPermissionPath(o1).compareTo(getPermissionPath(o2));
            }
        });

        return allPermissions;
    }

    public void storeDetails(String title, String description, Boolean hidden, String[] nodeTypes) {
        if (!title.equals(roleBean.getTitle())) {
            roleBean.setDirty(true);
            roleBean.setTitle(title);
        }
        if (!description.equals(roleBean.getDescription())) {
            roleBean.setDirty(true);
            roleBean.setDescription(description);
        }
        roleBean.setHidden(hidden != null && hidden);

        if (roleBean.getNodeTypes() != null) {
            if (nodeTypes != null) {
                List<String> values = Arrays.asList(nodeTypes);
                for (NodeType nodeType : roleBean.getNodeTypes()) {
                    nodeType.setSet(values.contains(nodeType.getName()));
                }
            } else {
                for (NodeType nodeType : roleBean.getNodeTypes()) {
                    nodeType.setSet(false);
                }
            }
        }
    }

    public void expandMapped(String path) {
        Map<String, PermissionBean> permissionBeans = roleBean.getPermissions().get(currentContext).get(currentGroup);
        final PermissionBean permissionBean = permissionBeans.get(path);
        if (permissionBean != null && !permissionBean.isPartialSet()) {
            if (!permissionBean.isMappedPermissionsExpanded()) {
                permissionBean.setMappedPermissionsExpanded(true);
                for (PermissionBean mapped : permissionBean.getMappedPermissions().values()) {
                    permissionBeans.put(mapped.getPath(), mapped);
                }
            } else {
                permissionBean.setMappedPermissionsExpanded(false);
                for (PermissionBean mapped : permissionBean.getMappedPermissions().values()) {
                    permissionBeans.remove(mapped.getPath());
                }
            }

        }
    }

    private String getMessage(String key) {
        return Messages.get("resources.JahiaRolesManager", key, LocaleContextHolder.getLocale());
    }

}
