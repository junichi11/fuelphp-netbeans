/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.fuel.ui;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.netbeans.modules.php.fuel.util.FuelUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author junichi11
 */
public class NewProjectConfigurationPanel extends javax.swing.JPanel {

    private static final String GITHUB_API_REPOS_DOWNLOADS = "https://api.github.com/repos/fuel/fuel/downloads"; // NOI18N
    private static final String GITHUB_API_REPOS_DOWNLOADS_HTML_URL = "html_url";
    private static final String GITHUB_API_REPOS_DOWNLOADS_NAME = "name";
    private static final long serialVersionUID = 7874450246517944114L;
    private Map<String, String> downloadsMap = new HashMap<String, String>();
    private String errorMessage = null; // NOI18N

    /**
     * Creates new form NewProjectConfigurationPanel
     */
    public NewProjectConfigurationPanel() {
        initComponents();
        this.unzipRadioButton.setSelected(true);
        try {
            // Get JSON
            URL githubApi = new URL(GITHUB_API_REPOS_DOWNLOADS);
            JSONArray jsonArray = FuelUtils.getJsonArray(githubApi);

            String[] downloadsArray = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jObject = (JSONObject) jsonArray.get(i);
                downloadsArray[i] = jObject.getString(GITHUB_API_REPOS_DOWNLOADS_NAME); // NOI18N
                downloadsMap.put(jObject.getString(GITHUB_API_REPOS_DOWNLOADS_NAME), jObject.getString(GITHUB_API_REPOS_DOWNLOADS_HTML_URL)); // NOI18N
            }

            Arrays.sort(downloadsArray, new Comparator<String>() {
                public static final String COMPARE_SPLIT_PATTERN = "[., -]"; // NOI18N

                @Override
                public int compare(String a, String b) {
                    String[] aArray = a.split(COMPARE_SPLIT_PATTERN);
                    String[] bArray = b.split(COMPARE_SPLIT_PATTERN);
                    for (int i = 0; i < aArray.length; i++) {
                        try {
                            Integer aInt = Integer.parseInt(aArray[i]);
                            Integer bInt = Integer.parseInt(bArray[i]);
                            if (aInt == bInt) {
                                continue;
                            } else {
                                return bInt - aInt;
                            }
                        } catch (NumberFormatException ex) {
                            return 1;
                        }
                    }
                    return 1;
                }
            });
            versionList.setListData(downloadsArray);
            versionList.setSelectedIndex(0);
        } catch (JSONException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            errorMessage = "Is not connected to the network.";
        }
        gettingFileInfoLabel.setText(errorMessage);
    }

    public Map<String, String> getDownloadsMap() {
        return downloadsMap;
    }

    public JLabel getGettingFileInfoLabel() {
        return gettingFileInfoLabel;
    }

    public JList getVersionList() {
        return versionList;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ButtonGroup getButtonGroup() {
        return buttonGroup;
    }

    public JRadioButton getGitCloneRadioButton() {
        return gitCloneRadioButton;
    }

    public JRadioButton getUnzipRadioButton() {
        return unzipRadioButton;
    }

    public void setGitCommandLabel(String command) {
        gitCommandLabel.setText(command);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        selectVersionLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        versionList = new javax.swing.JList();
        gettingFileInfoLabel = new javax.swing.JLabel();
        gitCloneRadioButton = new javax.swing.JRadioButton();
        unzipRadioButton = new javax.swing.JRadioButton();
        gitCommandLabel = new javax.swing.JLabel();

        selectVersionLabel.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.selectVersionLabel.text")); // NOI18N

        versionList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(versionList);

        gettingFileInfoLabel.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.gettingFileInfoLabel.text")); // NOI18N

        buttonGroup.add(gitCloneRadioButton);
        gitCloneRadioButton.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.gitCloneRadioButton.text")); // NOI18N
        gitCloneRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gitCloneRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup.add(unzipRadioButton);
        unzipRadioButton.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.unzipRadioButton.text")); // NOI18N

        gitCommandLabel.setText(org.openide.util.NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.gitCommandLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(gitCloneRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(unzipRadioButton)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gettingFileInfoLabel)
                    .addComponent(selectVersionLabel)
                    .addComponent(gitCommandLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(139, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(unzipRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectVersionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gettingFileInfoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gitCloneRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gitCommandLabel)
                .addContainerGap(45, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void gitCloneRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gitCloneRadioButtonActionPerformed
        try {
            Process process = Runtime.getRuntime().exec("git"); // NOI18N
            process.waitFor();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            gitCloneRadioButton.setSelected(false);
            unzipRadioButton.setSelected(true);
            gitCloneRadioButton.setEnabled(false);
        }
    }//GEN-LAST:event_gitCloneRadioButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JLabel gettingFileInfoLabel;
    private javax.swing.JRadioButton gitCloneRadioButton;
    private javax.swing.JLabel gitCommandLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel selectVersionLabel;
    private javax.swing.JRadioButton unzipRadioButton;
    private javax.swing.JList versionList;
    // End of variables declaration//GEN-END:variables
}
