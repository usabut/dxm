/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.serversettings.flow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeTypeIterator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.exceptions.JahiaException;
import org.jahia.modules.serversettings.forge.ForgeService;
import org.jahia.modules.serversettings.forge.Module;
import org.jahia.modules.serversettings.moduleManagement.ModuleFile;
import org.jahia.modules.serversettings.moduleManagement.ModuleVersionState;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleVersion;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.webflow.execution.RequestContext;

/**
 * WebFlow handler for managing modules.
 *
 * @author rincevent
 */
public class ModuleManagementFlowHandler implements Serializable {
    private static final long serialVersionUID = -4195379181264451784L;

    private static Logger logger = LoggerFactory.getLogger(ModuleManagementFlowHandler.class);

    @Autowired
    private transient JahiaTemplateManagerService templateManagerService;

    @Autowired
    private transient JahiaSitesService sitesService;

    @Autowired
    private transient ForgeService forgeService;

    private String moduleName;

    public boolean isInModule(RenderContext renderContext) {
        try {
            if (renderContext.getMainResource().getNode().isNodeType("jnt:module")) {
                moduleName = renderContext.getMainResource().getNode().getName();
                return true;
            }
        } catch (RepositoryException e) {
        }
        return false;
    }

    public boolean isStudio(RenderContext renderContext) {
        return renderContext.getEditModeConfigName().equals("studiomode") || renderContext.getEditModeConfigName().equals("studiovisualmode");
    }

    public ModuleFile initModuleFile() {
        return new ModuleFile();
    }

    public boolean installModule(String forgeId, String url, MessageContext context) {
        try {
            File file = forgeService.downloadModuleFromForge(forgeId, url);
            return installModule(file, context);
        } catch (Exception e) {
            context.addMessage(new MessageBuilder().source("moduleFile")
                    .code("serverSettings.manageModules.install.failed")
                    .arg(e.getMessage())
                    .error()
                    .build());
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean uploadModule(ModuleFile moduleFile, MessageContext context) {
        String originalFilename = moduleFile.getModuleFile().getOriginalFilename();
        if (!FilenameUtils.isExtension(originalFilename, Arrays.<String>asList("war","jar","WAR","JAR"))) {
            context.addMessage(new MessageBuilder().error().source("moduleFile")
                    .code("serverSettings.manageModules.install.wrongFormat").build());
            return false;
        }
        try {
            final File file = File.createTempFile("module-", "."+StringUtils.substringAfterLast(originalFilename,"."));
            moduleFile.getModuleFile().transferTo(file);
            return installModule(file, context);
        } catch (Exception e) {
            context.addMessage(new MessageBuilder().source("moduleFile")
                    .code("serverSettings.manageModules.install.failed")
                    .arg(e.getMessage())
                    .error()
                    .build());
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    private boolean installModule(File file, MessageContext context) throws IOException, BundleException {
        JarFile jarFile = new JarFile(file);
        try {
            Manifest manifest = jarFile.getManifest();
            String symbolicName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
            if (symbolicName == null) {
                symbolicName = manifest.getMainAttributes().getValue("root-folder");
            }
            String version = manifest.getMainAttributes().getValue("Implementation-Version");
            String groupId = manifest.getMainAttributes().getValue("Jahia-GroupId");
            if (templateManagerService.differentModuleWithSameIdExists(symbolicName, groupId)) {
                context.addMessage(new MessageBuilder().source("moduleFile")
                        .code("serverSettings.manageModules.install.moduleWithSameIdExists")
                        .arg(symbolicName)
                        .error()
                        .build());
                return false;
            }

            Bundle bundle = BundleUtils.getBundle(symbolicName, version);

            String location = file.toURI().toString();
            if (file.getName().toLowerCase().endsWith(".war")) {
                location = "jahiawar:"+location;
            }

            if (bundle != null) {
                InputStream is = new URL(location).openStream();
                try {
                    bundle.update(is);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            } else {
                InputStream is = new URL(location).openStream();
                try {
                    bundle = FrameworkService.getBundleContext().installBundle(location, is);
                    bundle.adapt(BundleStartLevel.class).setStartLevel(2);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
            List<String> deps = BundleUtils.getModule(bundle).getDepends();
            List<String> missingDeps = getMissingDependenciesFrom(deps);
            if (!missingDeps.isEmpty()) {
                createMessageForMissingDependencies(context, missingDeps);
            } else {
                Set<ModuleVersion> allVersions = templateManagerService.getTemplatePackageRegistry().getAvailableVersionsForModule(bundle.getSymbolicName());
                if (allVersions.contains(new ModuleVersion(version)) && allVersions.size() == 1) {
                    bundle.start();
                    context.addMessage(new MessageBuilder().source("moduleFile")
                            .code("serverSettings.manageModules.install.uploadedAndStarted")
                            .build());
                } else {
                    context.addMessage(new MessageBuilder().source("moduleFile")
                            .code("serverSettings.manageModules.install.uploaded")
                            .build());
                }
            }
        } finally {
            if (jarFile != null) {
                jarFile.close();
            }
        }

        return true;
    }

    private void createMessageForMissingDependencies(MessageContext context, List<String> missingDeps) {
        context.addMessage(new MessageBuilder().source("moduleFile")
                .code("serverSettings.manageModules.install.missingDependencies")
                .arg(StringUtils.join(missingDeps, ","))
                .error()
                .build());
    }

    private List<String> getMissingDependenciesFrom(List<String> deps) {
        List<String> missingDeps = new ArrayList<String>(deps.size());
        for (String dep : deps) {
            if (templateManagerService.getTemplatePackageById(dep) == null && templateManagerService.getTemplatePackage(dep) == null) {
                missingDeps.add(dep);
            }
        }
        return missingDeps;
    }

    public void loadModuleInformation(RequestContext context) {
        String selectedModuleName = moduleName != null ? moduleName : (String) context.getFlowScope().get("selectedModule");
        Map<ModuleVersion, JahiaTemplatesPackage> selectedModule = templateManagerService.getTemplatePackageRegistry().getAllModuleVersions().get(selectedModuleName);
        if (selectedModule != null) {
            if(selectedModule.size()>1) {
                boolean foundActiveVersion = false;
                for (Map.Entry<ModuleVersion, JahiaTemplatesPackage> entry : selectedModule.entrySet()) {
                    JahiaTemplatesPackage value = entry.getValue();
                    if (value.isActiveVersion()) {
                        foundActiveVersion = true;
                        populateActiveVersion(context, value);
                    }
                }
                if(!foundActiveVersion) {
                    // there is no active version take information from most recent installed version
                    LinkedList<ModuleVersion> sortedVersions = new LinkedList<ModuleVersion>(selectedModule.keySet());
                    Collections.sort(sortedVersions);
                    populateActiveVersion(context, selectedModule.get(sortedVersions.getFirst()));
                }
            }
            else {
                populateActiveVersion(context, selectedModule.values().iterator().next());
            }
            context.getRequestScope().put("otherVersions",selectedModule);
        } else {
            // module is not yet parsed probably because it depends on unavailable modules so look for it in module states
            final Map<Bundle, ModuleState> moduleStates = templateManagerService.getModuleStates();
            for (Bundle bundle : moduleStates.keySet()) {
                JahiaTemplatesPackage module = BundleUtils.getModule(bundle);
                if (module.getId().equals(selectedModuleName)) {
                    populateActiveVersion(context, module);
                    final List<String> missing = getMissingDependenciesFrom(module.getDepends());
                    createMessageForMissingDependencies(context.getMessageContext(), missing);
                    break;
                }

            }
        }

        populateSitesInformation(context);
        Set<String> systemSiteRequiredModules = getSystemSiteRequiredModules();
        context.getRequestScope().put("systemSiteRequiredModules", systemSiteRequiredModules);
        // Get list of definitions
        NodeTypeIterator nodeTypes = NodeTypeRegistry.getInstance().getNodeTypes(selectedModuleName);
        Map<String,Boolean> booleanMap = new TreeMap<String, Boolean>();
        while (nodeTypes.hasNext()) {
            ExtendedNodeType nodeType = (ExtendedNodeType) nodeTypes.next();
            booleanMap.put(nodeType.getLabel(LocaleContextHolder.getLocale()),nodeType.isNodeType(
                    "jmix:droppableContent"));
        }
        context.getRequestScope().put("nodeTypes", booleanMap);
    }

    public void populateSitesInformation(RequestContext context) {
        //populate information about sites
        List<String> siteKeys = new ArrayList<String>();
        Map<String,List<String>> directSiteDep = new HashMap<String,List<String>>();
        Map<String,List<String>> templateSiteDep = new HashMap<String,List<String>>();
        Map<String,List<String>> transitiveSiteDep = new HashMap<String,List<String>>();
        try {
            List<JCRSiteNode> sites = sitesService.getSitesNodeList();
            for (JCRSiteNode site : sites) {
                siteKeys.add(site.getSiteKey());
                List<JahiaTemplatesPackage> directDependencies = templateManagerService.getInstalledModulesForSite(
                        site.getSiteKey(), false, true, false);
                for (JahiaTemplatesPackage directDependency : directDependencies) {
                    if(!directSiteDep.containsKey(directDependency.getId())) {
                        directSiteDep.put(directDependency.getId(), new ArrayList<String>());
                    }
                    directSiteDep.get(directDependency.getId()).add(site.getSiteKey());
                }
                if (site.getTemplatePackage() != null) {
                    if(!templateSiteDep.containsKey(site.getTemplatePackage().getId())) {
                        templateSiteDep.put(site.getTemplatePackage().getId(), new ArrayList<String>());
                    }
                    templateSiteDep.get(site.getTemplatePackage().getId()).add(site.getSiteKey());
                }
                List<JahiaTemplatesPackage> transitiveDependencies = templateManagerService.getInstalledModulesForSite(
                        site.getSiteKey(), true, false, true);
                for (JahiaTemplatesPackage transitiveDependency : transitiveDependencies) {
                    if(!transitiveSiteDep.containsKey(transitiveDependency.getId())) {
                        transitiveSiteDep.put(transitiveDependency.getId(), new ArrayList<String>());
                    }
                    transitiveSiteDep.get(transitiveDependency.getId()).add(site.getSiteKey());
                }
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        context.getRequestScope().put("sites",siteKeys);
        context.getRequestScope().put("sitesDirect",directSiteDep);
        context.getRequestScope().put("sitesTemplates",templateSiteDep);
        context.getRequestScope().put("sitesTransitive", transitiveSiteDep);

        if (!((RenderContext) context.getExternalContext().getRequestMap().get("renderContext"))
                .getEditModeConfigName().startsWith("studio")) {
            populateModuleVersionStateInfo(context, directSiteDep, templateSiteDep, transitiveSiteDep);
        }
    }

    /**
     * Returns a map, keyed by the module name, with the sorted map (by version ascending) of {@link JahiaTemplatesPackage} objects.
     *
     * @return a map, keyed by the module name, with the sorted map (by version ascending) of {@link JahiaTemplatesPackage} objects
     */
    public Map<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> getAllModuleVersions() {
        Map<Bundle,ModuleState> moduleStates = templateManagerService.getModuleStates();
        Map<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> allModuleVersions = templateManagerService.getTemplatePackageRegistry().getAllModuleVersions();
        Map<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> result = new TreeMap<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>>(allModuleVersions);
        for (Bundle bundle : moduleStates.keySet()) {
            JahiaTemplatesPackage module = BundleUtils.getModule(bundle);
            if(!allModuleVersions.containsKey(module.getId())) {
                TreeMap<ModuleVersion, JahiaTemplatesPackage> map = new TreeMap<ModuleVersion, JahiaTemplatesPackage>();
                map.put(module.getVersion(),module);
                result.put(module.getId(),map);
            }
        }
        return result;
    }

    /**
     * Returns a map, keyed by the module name, with all available module updates.
     *
     * @return a map, keyed by the module name, with all available module updates
     */
    public Map<String,Module> getAvailableUpdates() {
        Map<String,Module> availableUpdate = new HashMap<String, Module>();
        Map<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> moduleStates = templateManagerService.getTemplatePackageRegistry().getAllModuleVersions();
        for (String key : moduleStates.keySet()) {
            Module forgeModule = forgeService.findModule(key,moduleStates.get(key).get(moduleStates.get(key).firstKey()).getGroupId());
            if (forgeModule != null) {
                ModuleVersion forgeVersion = new ModuleVersion(forgeModule.getVersion());
                if (!moduleStates.get(key).containsKey(forgeVersion) && forgeVersion.compareTo(moduleStates.get(key).lastKey()) > 0) {
                    availableUpdate.put(key,forgeModule);
                }
            }
        }
        return availableUpdate;
    }

    private void populateModuleVersionStateInfo(RequestContext context, Map<String, List<String>> directSiteDep,
                                                Map<String, List<String>> templateSiteDep, Map<String, List<String>> transitiveSiteDep) {
        Map<String, Map<ModuleVersion, ModuleVersionState>> states = new TreeMap<String, Map<ModuleVersion, ModuleVersionState>>();
        Set<String> systemSiteRequiredModules = getSystemSiteRequiredModules();
        context.getRequestScope().put("systemSiteRequiredModules", systemSiteRequiredModules);
        for (Map.Entry<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> entry : templateManagerService
                .getTemplatePackageRegistry().getAllModuleVersions().entrySet()) {
            Map<ModuleVersion, ModuleVersionState> moduleVersions = states.get(entry.getKey());
            if (moduleVersions == null) {
                moduleVersions = new TreeMap<ModuleVersion, ModuleVersionState>();
                states.put(entry.getKey(), moduleVersions);
            }

            for (Map.Entry<ModuleVersion, JahiaTemplatesPackage> moduleVersionEntry : entry.getValue().entrySet()) {
                ModuleVersionState state = getModuleVersionState(moduleVersionEntry.getKey(),
                        moduleVersionEntry.getValue(), entry.getValue().size() > 1, directSiteDep, templateSiteDep, transitiveSiteDep, systemSiteRequiredModules);
                moduleVersions.put(moduleVersionEntry.getKey(), state);
            }
        }
        context.getRequestScope().put("moduleStates", states);
    }

    private ModuleVersionState getModuleVersionState(ModuleVersion moduleVersion, JahiaTemplatesPackage pkg,
                                                     boolean multipleVersionsOfModuleInstalled, Map<String, List<String>> directSiteDep,
                                                     Map<String, List<String>> templateSiteDep, Map<String, List<String>> transitiveSiteDep, Set<String> systemSiteRequiredModules) {
        ModuleVersionState state = new ModuleVersionState();
        Map<String, JahiaTemplatesPackage> registeredModules = templateManagerService.getTemplatePackageRegistry()
                .getRegisteredModules();
        String moduleId = pkg.getId();

        // check for unresolved dependencies
        if (!pkg.getDepends().isEmpty()) {
            for (String dependency : pkg.getDepends()) {
                if (templateManagerService.getTemplatePackageRegistry().getAvailableVersionsForModule(dependency).isEmpty()) {
                    state.getUnresolvedDependencies().add(dependency);
                }
            }
        }
        List<JahiaTemplatesPackage> dependantModules = templateManagerService.getTemplatePackageRegistry()
                .getDependantModules(pkg);
        for (JahiaTemplatesPackage dependant : dependantModules) {
            state.getDependencies().add(dependant.getId());
        }

        // check site usage and system dependency
        if (templateSiteDep.containsKey(moduleId)) {
            state.getUsedInSites().addAll(templateSiteDep.get(moduleId));
        }
        if (directSiteDep.containsKey(moduleId)) {
            state.getUsedInSites().addAll(directSiteDep.get(moduleId));
        }
        if (transitiveSiteDep.containsKey(moduleId)) {
            state.getUsedInSites().addAll(transitiveSiteDep.get(moduleId));
        }
        state.setSystemDependency(systemSiteRequiredModules.contains(moduleId));

        if (registeredModules.containsKey(moduleId)
                && registeredModules.get(moduleId).getVersion().equals(moduleVersion)) {
            // this is the currently active version of a module
            state.setCanBeStopped(!state.isSystemDependency());
        } else {
            // not currently active version of a module
            if (state.getUnresolvedDependencies().isEmpty()) {
                // no unresolved dependencies -> can start module version
                state.setCanBeStarted(true);

                // if the module is not used in sites or this is not the only version of a module installed -> allow to uninstall it
                state.setCanBeUninstalled(state.getUsedInSites().isEmpty() || multipleVersionsOfModuleInstalled);
            } else {
                state.setCanBeUninstalled(!state.isSystemDependency());
            }
        }

        return state;
    }

    private void populateActiveVersion(RequestContext context, JahiaTemplatesPackage value) {
        context.getRequestScope().put("activeVersion", value);
        Map<String,String> bundleInfo = new HashMap<String, String>();
        Dictionary<String,String> dictionary = value.getBundle().getHeaders();
        Enumeration<String> keys = dictionary.keys();
        while (keys.hasMoreElements()) {
            String s = keys.nextElement();
            bundleInfo.put(s,dictionary.get(s));
        }
        context.getRequestScope().put("bundleInfo", bundleInfo);
        context.getRequestScope().put("activeVersionDate",new Date(value.getBundle().getLastModified()));

        context.getRequestScope().put("dependantModules", templateManagerService.getTemplatePackageRegistry().getDependantModules(value));
    }

    private Set<String> getSystemSiteRequiredModules() {
        Set<String> modules = new TreeSet<String>();
        for (String module : templateManagerService.getNonManageableModules()) {
            JahiaTemplatesPackage pkg = templateManagerService.getTemplatePackageById(module);
            if (pkg != null) {
                modules.add(pkg.getId());
                for (JahiaTemplatesPackage dep : pkg.getDependencies()) {
                    modules.add(dep.getId());
                }
            }
        }

        return modules;
    }

    public Date getLastModulesUpdateTime(){
        return new Date(forgeService.getLastUpdateTime());
    }

    public void initModules(RequestContext requestContext, RenderContext renderContext) {
        // generate tables ids, used by datatable jquery plugin to store the state of a table in the user localestorage.
        // new flow = new ids
        reloadTablesUUIDFromSession(requestContext);
        if(!requestContext.getFlowScope().contains("adminModuleTableUUID")){
            requestContext.getFlowScope().put("adminModuleTableUUID", UUID.randomUUID().toString());
            requestContext.getFlowScope().put("storeModuleTableUUID", UUID.randomUUID().toString());
        }

        if(!isStudio(renderContext)){
            forgeService.loadModules();
            final Object moduleHasBeenStarted = requestContext.getExternalContext().getSessionMap().get(
                    "moduleHasBeenStarted");
            if(moduleHasBeenStarted !=null) {
                requestContext.getMessageContext().addMessage(new MessageBuilder().info().source(moduleHasBeenStarted).code("serverSettings.manageModules.module.started").arg(moduleHasBeenStarted).build());
                requestContext.getExternalContext().getSessionMap().remove("moduleHasBeenStarted");
            }
            final Object moduleHasBeenStopped = requestContext.getExternalContext().getSessionMap().get(
                    "moduleHasBeenStopped");
            if(moduleHasBeenStopped !=null) {
                requestContext.getMessageContext().addMessage(new MessageBuilder().info().source(moduleHasBeenStopped).code("serverSettings.manageModules.module.stopped").arg(moduleHasBeenStopped).build());
                requestContext.getExternalContext().getSessionMap().remove("moduleHasBeenStopped");
            }
        }
    }

    public void reloadModules(){
        forgeService.flushModules();
        forgeService.loadModules();
    }

    public List<Module> getForgeModules() {
        List<Module> installedModule = new ArrayList<Module>();
        List<Module> newModules = new ArrayList<Module>();
        for (Module module : forgeService.getModules()) {
            module.setInstallable(!templateManagerService.differentModuleWithSameIdExists(module.getId(), module.getGroupId()));
            JahiaTemplatesPackage pkg = templateManagerService.getTemplatePackageRegistry().lookupById(module.getId());
            if (pkg != null && pkg.getGroupId().equals(module.getGroupId())) {
                installedModule.add(module);
            } else {
                newModules.add(module);
            }
        }

        newModules.addAll(installedModule);
        return newModules;
    }


    /**
     * Logs the specified exception details.
     *
     * @param e
     *            the occurred exception to be logged
     */
    public void logError(Exception e) {
        logger.error(e.getMessage(), e);
    }

    public void hasAModuleBeenStarted(RequestContext requestContext) {
        logger.debug("Testing if a module has been started");
        if (requestContext.getRequestParameters().get("module")!=null) {
            requestContext.getExternalContext().getSessionMap().put("moduleHasBeenStarted",
                requestContext.getRequestParameters().get("module"));
        }
    }

    public void hasAModuleBeenStopped(RequestContext requestContext) {
        logger.debug("Testing if a module has been started");
        if (requestContext.getRequestParameters().get("module")!=null) {
            requestContext.getExternalContext().getSessionMap().put("moduleHasBeenStopped",
                    requestContext.getRequestParameters().get("module"));
        }
    }

    public void storeTablesUUID(RequestContext requestContext) {
        requestContext.getExternalContext().getSessionMap().put("adminModuleTableUUID", requestContext.getFlowScope().get("adminModuleTableUUID"));
        requestContext.getExternalContext().getSessionMap().put("forgeModuleTableUUID", requestContext.getFlowScope().get("forgeModuleTableUUID"));
    }

    private void reloadTablesUUIDFromSession(RequestContext requestContext) {
        if(requestContext.getExternalContext().getSessionMap().contains("adminModuleTableUUID") && !requestContext.getFlowScope().contains("adminModuleTableUUID")){
            requestContext.getFlowScope().put("adminModuleTableUUID", requestContext.getExternalContext().getSessionMap().get("adminModuleTableUUID"));
            requestContext.getFlowScope().put("forgeModuleTableUUID", requestContext.getExternalContext().getSessionMap().get("forgeModuleTableUUID"));

            requestContext.getExternalContext().getSessionMap().remove("adminModuleTableUUID");
            requestContext.getExternalContext().getSessionMap().remove("forgeModuleTableUUID");
        }
    }
}
