/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.content.compare;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionToolbarLayoutContainer;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Mar 2, 2010
 * Time: 9:32:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class VersionViewer extends ContentPanel {
    private GWTJahiaNode currentNode;
    private Linker linker = null;
    private String locale;
    private int currentMode = Constants.MODE_LIVE;
    private static JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();
    private String workspace = "default";
    private Frame currentFrame;
    private ComboBox<GWTJahiaNodeVersion> versionComboBox;
    private Slider slider;

    /**
     * Constructor
     *
     * @param node
     * @param mode
     * @param linker
     */
    public VersionViewer(GWTJahiaNode node, int mode, String locale, Linker linker) {
        super();
        this.linker = linker;
        this.currentNode = node;
        this.currentMode = mode;
        this.locale = locale;
        this.workspace = "default";
        if (currentMode == Constants.MODE_LIVE) {
            workspace = "live";
        }
        init();
    }

    /**
     * init component
     */
    private void init() {

        slider = new Slider() {
            @Override protected String onFormatValue(int value) {
                return DateTimeFormat.getMediumDateTimeFormat().format(new Date((long) value * 1000));
            }
        };
        slider.setMinValue(1285142400);
        slider.setMaxValue((int) (System.currentTimeMillis() / 1000));
        slider.setWidth(600);
        slider.addListener(Events.Change, new Listener<SliderEvent>() {
            public void handleEvent(SliderEvent be) {
                contentService
                        .getNodeURL(currentNode.getPath(), new Date(((long)be.getNewValue())*1000), null, workspace, locale, currentMode,
                                new BaseAsyncCallback<String>() {
                                    public void onSuccess(String url) {
                                        currentFrame = setUrl(url);
                                        setHeading(url);

                                        unmask();
                                    }

                                    public void onApplicationFailure(Throwable throwable) {
                                        Log.error("", throwable);
                                        unmask();
                                    }
                                });
            }
        });

        // combo box that allows to select the version
        versionComboBox = new ComboBox<GWTJahiaNodeVersion>();
        versionComboBox.setForceSelection(true);
        versionComboBox.setWidth(400);
        versionComboBox.setEditable(false);
        if (currentMode == Constants.MODE_PREVIEW || currentMode == Constants.MODE_STAGING) {
            versionComboBox.setEmptyText(Messages.get("label_staging_version ", "Staging version "));
        } else {
            versionComboBox.setEmptyText(Messages.get("label_live_version ", "Live version "));
        }

        // prepare data before rendering
        versionComboBox.setView(new ListView<GWTJahiaNodeVersion>() {
            @Override
            protected GWTJahiaNodeVersion prepareData(GWTJahiaNodeVersion version) {
                super.prepareData(version);
                if (version.getVersionNumber() != null) {
                    String value = Messages.get("label.version", "Version") + " ";
                    if (version.getLabel() != null && !"".equals(version.getLabel())) {
                        String[] strings = version.getLabel().split("_at_");
                        if (strings.length == 2) {
                            String s1;
                            if (strings[0].contains("published")) {
                                s1 = Messages.get("label.version.published", "published at");
                            } else if (strings[0].contains("uploaded")) {
                                s1 = Messages.get("label.version.uploaded", "uploaded at");
                            } else {
                                s1 = Messages.get("label.version." + strings[0], strings[0]);
                            }
                            value = value + s1 + " " + DateTimeFormat.getMediumDateTimeFormat()
                                    .format(DateTimeFormat.getFormat("yyyy_MM_dd_HH_mm_ss").parse(strings[1]));
                        } else {
                            value = version.getLabel();
                        }
                    }
                    version.set("displayField", value);
                } else {
                    if (currentMode == Constants.MODE_PREVIEW || currentMode == Constants.MODE_STAGING) {
                        version.set("displayField", Messages.get("label_staging_version", "Staging version"));
                    } else {
                        version.set("displayField", Messages.get("label_live_version", "Live version"));
                    }
                }
                return version;
            }
        });
        versionComboBox.setDisplayField("displayField");

        // load version with pagination
        final ListStore<GWTJahiaNodeVersion> store;

        final RpcProxy<PagingLoadResult<GWTJahiaNodeVersion>> proxy =
                new RpcProxy<PagingLoadResult<GWTJahiaNodeVersion>>() {
                    @Override
                    public void load(Object loadConfig, AsyncCallback<PagingLoadResult<GWTJahiaNodeVersion>> callback) {
                        loadVersions((PagingLoadConfig) loadConfig, callback);
                    }
                };

        // paging loader
        final PagingLoader<PagingLoadResult<GWTJahiaNodeVersion>> loader =
                new BasePagingLoader<PagingLoadResult<GWTJahiaNodeVersion>>(proxy);
        loader.addListener(Loader.BeforeLoad, new Listener<LoadEvent>() {
            public void handleEvent(LoadEvent be) {
                be.<ModelData>getConfig().set("start", be.<ModelData>getConfig().get("offset"));
            }
        });

        // list store that contains previous version and current workspace version
        store = new ListStore<GWTJahiaNodeVersion>(loader);
        versionComboBox.setStore(store);

        // general parameter
        versionComboBox.setLazyRender(false);
        versionComboBox.setTypeAhead(true);
        versionComboBox.setHideTrigger(false);
        versionComboBox.setPageSize(10);


        versionComboBox.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNodeVersion>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNodeVersion> event) {
                refresh();
            }
        });

        final Button refresh = new Button();
        refresh.setIconStyle("gwt-toolbar-icon-refresh");
        refresh.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent componentEvent) {
                refresh();
            }
        });
        // case of preview or edit: no version
        if (currentMode == Constants.MODE_PREVIEW || currentMode == Constants.MODE_STAGING) {
            final ToggleButton hButton = new ToggleButton("Highligthing");
            hButton.setIconStyle("gwt-diff");
            hButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent componentEvent) {
                    if (hButton.isPressed()) {
                        displayHighLigth();
                    } else {
                        refresh();
                    }
                }
            });

            final Button button = new Button(Messages.get("label.restore", "Restore"));
            button.addSelectionListener(new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    final GWTJahiaNodeVersion version = (GWTJahiaNodeVersion) versionComboBox.getValue();
                    contentService.restoreNode(version, false, new BaseAsyncCallback() {
                        public void onSuccess(Object result) {
                            versionComboBox.select(0);
                            versionComboBox.reset();
                            load(null);
                        }
                    });
                }
            });

            // add in the toolbar
            final ActionToolbarLayoutContainer headerToolBar = new ActionToolbarLayoutContainer("compare-engine") {
                public void afterToolbarLoading() {
                    insertItem(button, 0);
                    insertItem(hButton, 0);
                    insertItem(refresh, 0);
                    insertItem(versionComboBox, 0);
                }
            };
            headerToolBar.initWithLinker(linker);
            // add to widget
            setTopComponent(headerToolBar);
        } else {
            // case of th live mode
            LayoutContainer ctn = new LayoutContainer();
            ToolBar headerToolBar = new ToolBar();
            headerToolBar.add(versionComboBox);
            headerToolBar.add(refresh);
            ctn.add(headerToolBar);
            ToolBar slideTool = new ToolBar();
            slideTool.add(slider);
//            ctn.add(slideTool);
            setTopComponent(ctn);
        }


        load(null);
    }


    /**
     * refresh
     */
    private void refresh() {
        if (versionComboBox != null && versionComboBox.getValue() != null) {
            load(versionComboBox.getValue());
        } else {
            load(null);
        }
    }

    /**
     * Load versions
     *
     * @param loadConfig
     * @param callback
     */
    private void loadVersions(PagingLoadConfig loadConfig,
                              final AsyncCallback<PagingLoadResult<GWTJahiaNodeVersion>> callback) {
        int limit = 500;
        int offset = 0;
        if (loadConfig != null) {
            limit = loadConfig.getLimit();
            offset = loadConfig.getOffset();
        }

        JahiaContentManagementService.App.getInstance().getVersions(currentNode, workspace, limit, offset, callback);
    }

    /**
     * Render widget
     */
    private void load(GWTJahiaNodeVersion version) {
        if (currentNode != null) {
            mask();
            if (version == null || (version.getVersionNumber() == null || version.getVersionNumber().length() == 0)) {
                // version is not specified. Current.
                contentService.getNodeURL(currentNode.getPath(), null, null, null, locale, currentMode,
                        new BaseAsyncCallback<String>() {
                            public void onSuccess(String url) {
                                currentFrame = setUrl(url);
                                setHeading(url);

                                unmask();
                            }

                            public void onApplicationFailure(Throwable throwable) {
                                Log.error("", throwable);
                                unmask();
                            }
                        });
            } else {
                contentService
                        .getNodeURL(version.getNode().getPath(), version.getDate(), version.getLabel(), workspace, locale, currentMode,
                                new BaseAsyncCallback<String>() {
                                    public void onSuccess(String url) {
                                        currentFrame = setUrl(url);
                                        setHeading(url);

                                        unmask();
                                    }

                                    public void onApplicationFailure(Throwable throwable) {
                                        Log.error("", throwable);
                                        unmask();
                                    }
                                });
            }
        }
    }

    /**
     * Get html
     *
     * @return
     */
    public String getInnerHTML() {
        IFrameElement frameElement = IFrameElement.as(currentFrame.getElement());
        Document document = frameElement.getContentDocument();
        BodyElement ele = document.getBody();
        if (ele != null) {
            return ele.getInnerHTML();
        }

        // it may happens if the iframe is not yet loaded
        return null;

    }

    /**
     * Compare version
     */
    public void displayHighLigth() {
        contentService.getHighlighted(getCompareWith(), getInnerHTML(), new BaseAsyncCallback<String>() {
            public void onSuccess(String s) {
                IFrameElement frameElement = IFrameElement.as(currentFrame.getElement());
                Document document = frameElement.getContentDocument();
                BodyElement ele = document.getBody();
                if (ele != null) {
                    ele.setInnerHTML(s);
                }
            }

            public void onApplicationFailure(Throwable throwable) {
                Log.error("Error when triing to display higthligthing", throwable);
            }
        });

    }


    /**
     * Override this method to compare with another html
     *
     * @return
     */
    public String getCompareWith() {
        return getInnerHTML();
    }


}
