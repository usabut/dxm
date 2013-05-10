/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.menu;

//import com.extjs.gxt.ui.client.util.WidgetHelper;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;


/**
 * 
 * User: hollis
 * Date: 17 juil. 2008
 * Time: 14:30:29
 * 
 */
public class AutoCompletionMenu extends Menu {

    protected DataList choices;

    private AutoCompletionMenuItem item;

    public AutoCompletionMenu() {
      item = new AutoCompletionMenuItem();
      choices = item.choices;
      choices.setWidth(200);
      add(item);
      baseStyle = "x-autocompletion-menu";
      setAutoHeight(true);
      setWidth(200);
    }

    /**
     * Returns the selected choice.
     *
     * @return the date
     */
    public DataListItem getChoice() {
        return item.choices.getSelectedItem();
    }

    public DataList getChoices() {
        return choices;
    }

    @Override
    protected void doAttachChildren() {
      super.doAttachChildren();
//      WidgetHelper.doAttach(item.choices);
    }

    @Override
    protected void doDetachChildren() {
      super.doDetachChildren();
//      WidgetHelper.doDetach(item.choices);
    }

    public void focus() {
      super.focus();
      if (rendered) {
          if (this.getChoices().getItemCount()>0){
            this.getChoices().getItem(0).focus();
          }
      }
    }
}