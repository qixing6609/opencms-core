/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsToolbarEditButton.java,v $
 * Date   : $Date: 2010/04/13 14:28:27 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.draganddrop.CmsDragContainerElement;
import org.opencms.gwt.client.ui.CmsToolbarButton;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsStringUtil;

import com.google.gwt.event.dom.client.ClickEvent;

/**
 * The edit button holding all edit related methods.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarEditButton extends A_CmsContainerpageToolbarButton {

    /**
     * Constructor.<p>
     */
    public CmsToolbarEditButton() {

        super(CmsToolbarButton.ButtonData.EDIT, "edit", true, true);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#hasPermissions(org.opencms.ade.containerpage.client.draganddrop.CmsDragContainerElement)
     */
    public boolean hasPermissions(CmsDragContainerElement element) {

        return CmsStringUtil.isEmptyOrWhitespaceOnly(element.getNoEditReason());
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#init()
     */
    public void init() {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#onElementClick(com.google.gwt.event.dom.client.ClickEvent, org.opencms.ade.containerpage.client.draganddrop.CmsDragContainerElement)
     */
    public void onElementClick(ClickEvent event, CmsDragContainerElement element) {

        openEditorForElement(element);
        event.stopPropagation();
        event.preventDefault();

    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#onToolbarActivate()
     */
    public void onToolbarActivate() {

        showSingleElementOption(true);

    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#onToolbarDeactivate()
     */
    public void onToolbarDeactivate() {

        showSingleElementOption(false);

    }

    /**
     * Opens the edit dialog for the specified element.<p>
     * 
     * @param element the element to edit
     */
    public void openEditorForElement(CmsDragContainerElement element) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(element.getNoEditReason())) {
            CmsContentEditorDialog.get().openEditDialog(element.getClientId(), element.getSitePath());
        } else {
            CmsDebugLog.getInstance().printLine(element.getNoEditReason());
        }
    }

}