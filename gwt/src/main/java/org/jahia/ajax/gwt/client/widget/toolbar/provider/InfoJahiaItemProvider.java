/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.toolbar.provider;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;

import java.util.Map;

/**
 * User: jahia
 * Date: 18 avr. 2008
 * Time: 10:52:59
 */
public class InfoJahiaItemProvider extends AbstractJahiaToolItemProvider {
    public SelectionListener<ComponentEvent> getSelectListener(GWTJahiaToolbarItem gwtToolbarItem) {
        return null;
    }

    public ToolItem createNewToolItem(GWTJahiaToolbarItem gwtToolbarItem) {
        // TO DO: find a better implementation of this item. The current one is ....
        JahiaTextToolItem item = new JahiaTextToolItem();
        item.setIconStyle(gwtToolbarItem.getMinIconStyle());
        Map preferences = gwtToolbarItem.getProperties();
        if (preferences != null) {
            final GWTJahiaProperty info = (GWTJahiaProperty) preferences.get(ToolbarConstants.INFO);
            if (info != null) {
                item.setText(info.getValue());
            }
        }
        return item;
    }
    

    private class JahiaTextToolItem extends ToolItem {
        JahiaTextButton button;

        private JahiaTextToolItem() {
            button = new JahiaTextButton();
        }

        @Override
        public void removeStyleName(String s) {
            button.removeStyleName(s);
        }

        @Override
        public void removeStyleDependentName(String s) {
            button.removeStyleDependentName(s);
        }

        @Override
        public void setEnableState(boolean b) {
            button.setEnableState(b);
        }

        public void setText(String text) {
            button.setText(text);
        }

        public void setIconStyle(String iconStyle) {
            button.setIconStyle(iconStyle);
        }

        @Override
        protected void onRender(Element target, int index) {
            button.render(target, index);
            setElement(button.getElement());
        }

        @Override
        protected void doAttachChildren() {
            super.doAttachChildren();
            ComponentHelper.doAttach(button);
        }

        @Override
        protected void doDetachChildren() {
            super.doDetachChildren();
            ComponentHelper.doDetach(button);
        }
        
    }

    private class JahiaTextButton extends Button {
        @Override
        protected void onMouseEnter(ComponentEvent componentEvent) {
//            super.onMouseEnter(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void onClick(ComponentEvent componentEvent) {
//            super.onClick(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void onFocus(ComponentEvent componentEvent) {
//            super.onFocus(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void onMouseDown(ComponentEvent componentEvent) {
//            super.onMouseDown(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void onMenuHide(ComponentEvent componentEvent) {
//            super.onMenuHide(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void onMenuShow(ComponentEvent componentEvent) {
//            super.onMenuShow(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void onMouseLeave(ComponentEvent componentEvent) {
//            super.onMouseLeave(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void onMouseOut(ComponentEvent componentEvent) {
//            super.onMouseOut(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void onMouseOver(ComponentEvent componentEvent) {
//            super.onMouseOver(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void onMouseUp(ComponentEvent componentEvent) {
//            super.onMouseUp(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }
}
