/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapController.java,v $
 * Date   : $Date: 2010/04/21 14:29:20 $
 * Version: $Revision: 1.3 $
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapChangeDelete;
import org.opencms.ade.sitemap.shared.CmsSitemapChangeEdit;
import org.opencms.ade.sitemap.shared.CmsSitemapChangeMove;
import org.opencms.ade.sitemap.shared.CmsSitemapChangeNew;
import org.opencms.ade.sitemap.shared.I_CmsSitemapChange;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.Window;

/**
 * Sitemap editor controller.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapController {

    /** The list of changes. */
    private List<I_CmsSitemapChange> m_changes;

    /** The current available sitemap data. */
    private CmsClientSitemapEntry m_data;

    private I_CmsSitemapControllerHandler m_handler;

    /** The toolbar. */
    private CmsSitemapToolbar m_toolbar;

    /** The list of undone changes. */
    private List<I_CmsSitemapChange> m_undone;

    /**
     * Constructor.<p>
     */
    public CmsSitemapController() {

        m_changes = new ArrayList<I_CmsSitemapChange>();
        m_undone = new ArrayList<I_CmsSitemapChange>();
    }

    /**
     * Commits the changes.<p>
     */
    public void commit() {

        // save the sitemap
        CmsRpcAction<Void> saveAction = new CmsRpcAction<Void>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                CmsSitemapProvider.getService().save(CmsSitemapProvider.get().getUri(), getChanges(), this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(Void result) {

                if (result == null) {
                    // ok
                    return;
                }
                // error
                String title = org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_ERROR_0);
                String text = Messages.get().key(Messages.ERR_LOCK_2, CmsSitemapProvider.get().getUri(), result);

                new CmsAlertDialog(title, text).center();
            }
        };
        saveAction.execute();
    }

    /**
     * Registers a new sitemap entry.<p>
     * 
     * @param newEntry the new entry
     */
    public void create(CmsClientSitemapEntry newEntry) {

        assert (getEntry(newEntry.getSitePath()) == null);
        assert (getEntry(CmsResource.getParentFolder(newEntry.getSitePath())) != null);

        newEntry.setPosition(-1); // ensure it will be inserted at the end
        addChange(new CmsSitemapChangeNew(newEntry), false);
    }

    /**
     * Deletes the given entry and all its descendants.<p>
     * 
     * @param entry the entry to delete
     */
    public void delete(CmsClientSitemapEntry entry) {

        assert (getEntry(entry.getSitePath()) != null);
        addChange(new CmsSitemapChangeDelete(entry), false);
    }

    /**
     * Edits the given sitemap entry.<p>
     * 
     * @param entry the sitemap entry to update
     * @param title the new title, can be <code>null</code> to keep the old one
     * @param vfsReference the new VFS reference, can be <code>null</code> to keep the old one
     * @param properties the new properties, can be <code>null</code> to keep the old properties
     */
    public void edit(CmsClientSitemapEntry entry, String title, String vfsReference, Map<String, String> properties) {

        boolean changedTitle = ((title != null) && !title.trim().equals(entry.getTitle()));
        boolean changedVfsRef = ((vfsReference != null) && !vfsReference.trim().equals(entry.getVfsPath()));
        boolean changedProperties = ((properties != null) && !properties.equals(entry.getProperties()));
        assert (!changedTitle && !changedVfsRef && !changedProperties);

        CmsClientSitemapEntry newEntry = entry.cloneEntry();
        if (changedTitle) {
            newEntry.setTitle(title);
        }
        if (changedVfsRef) {
            newEntry.setVfsPath(vfsReference);
        }
        if (changedProperties) {
            newEntry.setProperties(properties);
        }
        addChange(new CmsSitemapChangeEdit(entry, newEntry), false);
    }

    /**
     * Returns the changes.<p>
     *
     * @return the changes
     */
    public List<I_CmsSitemapChange> getChanges() {

        return m_changes;
    }

    /**
     * Returns the toolbar.<p>
     *
     * @return the toolbar
     */
    public CmsSitemapToolbar getToolbar() {

        return m_toolbar;
    }

    /**
     * Checks if any change made.<p>
     * 
     * @return <code>true</code> if there is at least a change to commit
     */
    public boolean isDirty() {

        return !m_changes.isEmpty();
    }

    /**
     * Moves the given sitemap entry with all its descendants to the new position.<p>
     * 
     * @param entry the sitemap entry to move
     * @param toPath the destination path
     * @param position the new position between its siblings
     */
    public void move(CmsClientSitemapEntry entry, String toPath, int position) {

        assert (getEntry(entry.getSitePath()) != null);
        assert ((toPath != null) && (!entry.getSitePath().equals(toPath) || (entry.getPosition() != position)));
        assert (getEntry(CmsResource.getParentFolder(toPath)) != null);

        addChange(new CmsSitemapChangeMove(entry.getSitePath(), entry.getPosition(), toPath, position), false);
    }

    /**
     * Re-does the last undone change.<p>
     */
    public void redo() {

        if (m_undone.isEmpty()) {
            return;
        }

        // redo
        I_CmsSitemapChange change = m_undone.remove(m_undone.size() - 1);
        addChange(change, true);

        // state
        if (m_undone.isEmpty()) {
            getToolbar().getRedoButton().setEnabled(false);
        }
    }

    /**
     * Discards all changes, even unlocking the sitemap resource.<p>
     */
    public void reset() {

        m_changes.clear();
        m_undone.clear();
        internalReset(true);
    }

    /**
     * Sets the controller handler.<p>
     * 
     * @param handler the handler to set
     */
    public void setHandler(I_CmsSitemapControllerHandler handler) {

        m_handler = handler;
    }

    /**
     * Sets the root of the current sitemap.<p>
     * 
     * @param roots the root of the current sitemap
     */
    public void setRoots(List<CmsClientSitemapEntry> roots) {

        m_data = new CmsClientSitemapEntry();
        if (roots.isEmpty()) {
            return;
        }
        m_data.setSitePath(CmsResource.getParentFolder(roots.get(0).getSitePath()));
        m_data.setChildren(roots);
    }

    /**
     * Sets the toolbar.<p>
     * 
     * @param toolbar the toolbar
     */
    public void setToolbar(CmsSitemapToolbar toolbar) {

        m_toolbar = toolbar;
    }

    /**
     * Undoes the last change.<p>
     */
    public void undo() {

        if (!isDirty()) {
            return;
        }

        // pre-state
        if (m_undone.isEmpty()) {
            getToolbar().getRedoButton().setEnabled(true);
        }

        // undo
        I_CmsSitemapChange change = m_changes.remove(m_changes.size() - 1);
        m_undone.add(change);
        // refresh view
        if (m_handler != null) {
            m_handler.onChange(change);
        }

        // post-state
        if (!isDirty()) {
            internalReset(false);
        }
    }

    /**
     * Adds a change to the queue.<p>
     * 
     * @param oldEntry the old entry
     * @param newEntry the new entry
     * @param changeType the change type
     * @param position the new position between its siblings, only used when moving
     * @param redo if redoing a change
     */
    private void addChange(I_CmsSitemapChange change, boolean redo) {

        // state
        if (!isDirty()) {
            startEdit();
        }

        if (!redo) {
            // after a new change no changes can be redone
            m_undone.clear();
            m_toolbar.getRedoButton().setEnabled(false);
        }

        // add it
        m_changes.add(change);

        // update data
        if (change instanceof CmsSitemapChangeDelete) {
            CmsSitemapChangeDelete changeDelete = (CmsSitemapChangeDelete)change;
            CmsClientSitemapEntry deleteParent = getEntry(CmsResource.getParentFolder(changeDelete.getEntry().getSitePath()));
            deleteParent.removeChild(changeDelete.getEntry().getPosition());
        } else if (change instanceof CmsSitemapChangeEdit) {
            CmsSitemapChangeEdit changeEdit = (CmsSitemapChangeEdit)change;
            CmsClientSitemapEntry editEntry = getEntry(changeEdit.getOldEntry().getSitePath());
            editEntry.setTitle(changeEdit.getNewEntry().getTitle());
            editEntry.setVfsPath(changeEdit.getNewEntry().getVfsPath());
            editEntry.setProperties(changeEdit.getNewEntry().getProperties());
        } else if (change instanceof CmsSitemapChangeMove) {
            CmsSitemapChangeMove changeMove = (CmsSitemapChangeMove)change;
            CmsClientSitemapEntry sourceParent = getEntry(CmsResource.getParentFolder(changeMove.getSourcePath()));
            CmsClientSitemapEntry moved = sourceParent.removeChild(changeMove.getSourcePosition());
            CmsClientSitemapEntry destParent = getEntry(CmsResource.getParentFolder(changeMove.getDestinationPath()));
            destParent.insertChild(moved, changeMove.getDestinationPosition());
        } else if (change instanceof CmsSitemapChangeNew) {
            CmsSitemapChangeNew changeNew = (CmsSitemapChangeNew)change;
            CmsClientSitemapEntry newParent = getEntry(CmsResource.getParentFolder(changeNew.getEntry().getSitePath()));
            newParent.addChild(changeNew.getEntry());
        }

        // refresh view
        if (m_handler != null) {
            m_handler.onChange(change);
        }
    }

    /**
     * Returns the tree entry with the given path.<p>
     * 
     * @param entryPath the path to look for
     * 
     * @return the tree entry with the given path, or <code>null</code> if not found
     */
    private CmsClientSitemapEntry getEntry(String entryPath) {

        if (!entryPath.startsWith(m_data.getSitePath())) {
            return null;
        }
        String path = entryPath.substring(m_data.getSitePath().length());
        String[] names = CmsStringUtil.splitAsArray(path, "/");
        CmsClientSitemapEntry result = m_data;
        for (String name : names) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
                // in case of leading slash
                continue;
            }
            boolean found = false;
            for (CmsClientSitemapEntry child : result.getChildren()) {
                if (child.getName().equals(name)) {
                    found = true;
                    result = child;
                    break;
                }
            }
            if (!found) {
                // not found
                break;
            }
        }
        if (result == m_data) {
            result = null;
        }
        return result;
    }

    /**
     * Discards all changes, even unlocking the sitemap resource.<p>
     * 
     * @param reload if to reload after unlocking
     */
    private void internalReset(final boolean reload) {

        // state
        getToolbar().getSaveButton().setEnabled(false);
        getToolbar().getResetButton().setEnabled(false);
        getToolbar().getUndoButton().setEnabled(false);

        // unlock
        CmsRpcAction<String> unlockAction = new CmsRpcAction<String>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                start(0);
                CmsCoreProvider.getCoreService().unlock(CmsSitemapProvider.get().getUri(), this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(String result) {

                stop();

                if (result == null) {
                    // ok
                    if (reload) {
                        Window.Location.reload();
                    }
                    return;
                }
                // error
                String title = org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_ERROR_0);
                String text = Messages.get().key(Messages.ERR_UNLOCK_2, CmsSitemapProvider.get().getUri(), result);

                new CmsAlertDialog(title, text).center();
            }
        };
        unlockAction.execute();
    }

    /**
     * Sets the state when start editing, even locking the sitemap resource.<p>
     */
    private void startEdit() {

        // lock the sitemap
        CmsRpcAction<String> lockAction = new CmsRpcAction<String>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                start(0);
                CmsCoreProvider.getCoreService().lock(CmsSitemapProvider.get().getUri(), this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(String result) {

                // state
                getToolbar().getSaveButton().setEnabled(true);
                getToolbar().getResetButton().setEnabled(true);
                getToolbar().getUndoButton().setEnabled(true);

                stop();
                if (result == null) {
                    // ok
                    return;
                }
                // error
                String title = org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_ERROR_0);
                String text = Messages.get().key(Messages.ERR_LOCK_2, CmsSitemapProvider.get().getUri(), result);

                new CmsAlertDialog(title, text).center();
            }
        };
        lockAction.execute();
    }
}