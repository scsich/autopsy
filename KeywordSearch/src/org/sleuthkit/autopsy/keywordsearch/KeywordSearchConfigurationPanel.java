/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sleuthkit.autopsy.keywordsearch;

import java.util.logging.Level;

import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.corecomponents.OptionsPanel;

/**
 * Container panel for keyword search advanced configuration options
 */
final class KeywordSearchConfigurationPanel extends javax.swing.JPanel implements OptionsPanel {

    private static final Logger logger = Logger.getLogger(KeywordSearchConfigurationPanel.class.getName());
    private KeywordSearchConfigurationPanel1 listsPanel;
    private KeywordSearchConfigurationPanel3 languagesPanel;
    private KeywordSearchConfigurationPanel2 generalPanel;

    KeywordSearchConfigurationPanel() {
        initComponents();
        customizeComponents();
    }
    
    private void customizeComponents() {
        setName(NbBundle.getMessage(this.getClass(), "KeywordSearchConfigurationPanel.customizeComponents.title"));
        listsPanel = new KeywordSearchConfigurationPanel1();
        languagesPanel = new KeywordSearchConfigurationPanel3();
        generalPanel = new KeywordSearchConfigurationPanel2();
        tabbedPane.insertTab(NbBundle.getMessage(this.getClass(), "KeywordSearchConfigurationPanel.customizeComponents.listTabTitle"), null,
                listsPanel, NbBundle.getMessage(this.getClass(), "KeywordSearchConfigurationPanel.customizeComponents.listLabToolTip"), 0);
        tabbedPane.insertTab(NbBundle.getMessage(this.getClass(), "KeywordSearchConfigurationPanel.customizeComponents.stringExtTitle"), null,
                languagesPanel, NbBundle.getMessage(this.getClass(), "KeywordSearchConfigurationPanel.customizeComponents.stringExtToolTip"), 1);
        tabbedPane.insertTab(NbBundle.getMessage(this.getClass(), "KeywordSearchConfigurationPanel.customizeComponents.genTabTitle"), null,
                generalPanel, NbBundle.getMessage(this.getClass(), "KeywordSearchConfigurationPanel.customizeComponents.genTabToolTip"), 2);
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 675, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Load each of the tabs and reload the XML.
     */
    @Override
    public void load() {
        // Deselect all table rows
        listsPanel.load();
        languagesPanel.load();
        generalPanel.load();
        // Reload the XML to avoid 'ghost' vars
        KeywordSearchListsXML.getCurrent().reload();
    }

    /**
     * Store each panel's settings.
     */
    @Override
    public void store() {
        listsPanel.store();
        languagesPanel.store();
        generalPanel.store();
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
