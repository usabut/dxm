package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.dnd.Insert;
import com.extjs.gxt.ui.client.fx.Draggable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Element;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:25:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleModule extends ContentPanel {

    private HTML html;

    public SimpleModule(final String path, String s, final EditManager editManager) {
//        super(new FitLayout());
        setHeaderVisible(false);
        setScrollMode(Style.Scroll.AUTO);
        setBorders(false);
        html = new HTML(s);
        add(html);
        boolean last = !ModuleHelper.parse(this, html, editManager);

        if (last) {

            DragSource source = new DragSource(this) {
                @Override
                protected void onDragDrop(DNDEvent e) {

                }

                @Override
                protected void onDragStart(DNDEvent e) {
                    e.setCancelled(false);
                    e.setData(this);

                    if (getStatusText() == null) {
                        e.getStatus().update("Moving content");
                    }

                }
            };
            source.addDNDListener(editManager.getDndListener());

            DropTarget target = new DropTarget(this) {
                @Override
                protected void onDragMove(DNDEvent event) {
                    event.setCancelled(false);
                }

                @Override
                protected void showFeedback(DNDEvent event) {
                    showInsert(event, this.getComponent().getElement(), true);
                }

                private void showInsert(DNDEvent event, Element row, boolean before) {
                    Insert insert = Insert.get();
                    insert.setVisible(true);
                    Rectangle rect = El.fly(row).getBounds();
                    int y = !before ? (rect.y + rect.height - 4) : rect.y - 2;
                    insert.el().setBounds(rect.x, y, rect.width, 6);
                }

            };
            target.setOperation(DND.Operation.COPY);
            target.setFeedback(DND.Feedback.INSERT);

            target.addDNDListener(editManager.getDndListener());

            Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                    Log.info("click" + path);
                    editManager.setSelection(SimpleModule.this);
                }
            };
            addListener(Events.OnMouseDown, listener);
        }
    }

    public HTML getHtml() {
        return html;
    }

}
