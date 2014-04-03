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
 *//*
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

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.ChangeSupport;

/**
 *
 * @author junichi11
 */
public class FuelPhpCustomizerPanel extends JPanel {

    private static final long serialVersionUID = -7256119527757283230L;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    /**
     * Creates new form FuelPhpCustomizerPanel
     */
    public FuelPhpCustomizerPanel() {
        initComponents();
        fuelNameTextField.getDocument().addDocumentListener(new DefaultDocumentListener());
    }

    public boolean isFuelEnabled() {
        return enabledCheckBox.isSelected();
    }

    public void setFuelEnebled(boolean isEnabled) {
        enabledCheckBox.setSelected(isEnabled);
    }

    public String getFuelName() {
        return fuelNameTextField.getText().trim();
    }

    public void setFuelNameTextField(String fuelName) {
        fuelNameTextField.setText(fuelName);
    }

    public boolean useTestCaseMethod() {
        return useTestCaseMethodCheckBox.isSelected();
    }

    public void setUseTestCaseMethod(boolean use) {
        this.useTestCaseMethodCheckBox.setSelected(use);
    }

    public boolean ignoreMVCNode() {
        return ignoreMVCNodeCheckBox.isSelected();
    }

    public void setIgnoreMVCNode(boolean ignore) {
        this.ignoreMVCNodeCheckBox.setSelected(ignore);
    }

    public String getTestCasePrefixTextField() {
        return testCasePrefixTextField.getText();
    }

    public void setTestCasePrefixTextField(String text) {
        testCasePrefixTextField.setText(text);
    }

    public String getTestCaseSuffixTextField() {
        return testCaseSuffixTextField.getText();
    }

    public void setTestCaseSuffixTextField(String tesxt) {
        testCaseSuffixTextField.setText(tesxt);
    }

    public String getTestGroupAnnotation() {
        return testGroupAnnotationTextField.getText();
    }

    public void setTestGroupAnnotation(String name) {
        testGroupAnnotationTextField.setText(name);
    }

    public boolean useAutoCreateFile() {
        return autoCreateFileCheckBox.isSelected();
    }

    public void setAutoCreateFile(boolean use) {
        autoCreateFileCheckBox.setSelected(use);
    }

    /**
     * Set enabled for all components except enabledCheckBox.
     *
     * @param isEnabled
     */
    public void setAllComponentsEnabled(boolean isEnabled) {
        for (Component component : this.getComponents()) {
            if (component == enabledCheckBox) {
                continue;
            }
            component.setEnabled(isEnabled);
        }
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fuelNameLabel = new javax.swing.JLabel();
        fuelNameTextField = new javax.swing.JTextField();
        useTestCaseMethodCheckBox = new javax.swing.JCheckBox();
        ignoreMVCNodeCheckBox = new javax.swing.JCheckBox();
        ignoreMessageLabel = new javax.swing.JLabel();
        testCasePrefixSuffixLabel = new javax.swing.JLabel();
        testCasePrefixLabel = new javax.swing.JLabel();
        testCasePrefixTextField = new javax.swing.JTextField();
        testCaseSuffixLabel = new javax.swing.JLabel();
        testCaseSuffixTextField = new javax.swing.JTextField();
        testGroupAnnotationLabel = new javax.swing.JLabel();
        testGroupAnnotationTextField = new javax.swing.JTextField();
        autoCreateFileCheckBox = new javax.swing.JCheckBox();
        generalLabel = new javax.swing.JLabel();
        testLabel = new javax.swing.JLabel();
        enabledCheckBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(fuelNameLabel, org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.fuelNameLabel.text")); // NOI18N

        fuelNameTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.fuelNameTextField.text")); // NOI18N
        fuelNameTextField.setToolTipText(org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.fuelNameTextField.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(useTestCaseMethodCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.useTestCaseMethodCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ignoreMVCNodeCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.ignoreMVCNodeCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ignoreMessageLabel, org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.ignoreMessageLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(testCasePrefixSuffixLabel, org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.testCasePrefixSuffixLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(testCasePrefixLabel, org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.testCasePrefixLabel.text")); // NOI18N

        testCasePrefixTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.testCasePrefixTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(testCaseSuffixLabel, org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.testCaseSuffixLabel.text")); // NOI18N

        testCaseSuffixTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.testCaseSuffixTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(testGroupAnnotationLabel, org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.testGroupAnnotationLabel.text")); // NOI18N

        testGroupAnnotationTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.testGroupAnnotationTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(autoCreateFileCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.autoCreateFileCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(generalLabel, org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.generalLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(testLabel, org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.testLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(enabledCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.enabledCheckBox.text")); // NOI18N
        enabledCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(FuelPhpCustomizerPanel.class, "FuelPhpCustomizerPanel.enabledCheckBox.toolTipText")); // NOI18N
        enabledCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enabledCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(generalLabel)
                    .addComponent(testLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(testCasePrefixLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(testCasePrefixTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(testCaseSuffixLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(testCaseSuffixTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(testCasePrefixSuffixLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(testGroupAnnotationLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(testGroupAnnotationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(fuelNameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fuelNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(useTestCaseMethodCheckBox)
                            .addComponent(ignoreMVCNodeCheckBox)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addComponent(ignoreMessageLabel))
                            .addComponent(autoCreateFileCheckBox)))
                    .addComponent(enabledCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(enabledCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(generalLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fuelNameLabel)
                    .addComponent(fuelNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useTestCaseMethodCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ignoreMVCNodeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ignoreMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(autoCreateFileCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(testLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testCasePrefixSuffixLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(testCasePrefixLabel)
                    .addComponent(testCasePrefixTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(testCaseSuffixLabel)
                    .addComponent(testCaseSuffixTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(testGroupAnnotationLabel)
                    .addComponent(testGroupAnnotationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void enabledCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enabledCheckBoxActionPerformed
        setAllComponentsEnabled(enabledCheckBox.isSelected());
    }//GEN-LAST:event_enabledCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoCreateFileCheckBox;
    private javax.swing.JCheckBox enabledCheckBox;
    private javax.swing.JLabel fuelNameLabel;
    private javax.swing.JTextField fuelNameTextField;
    private javax.swing.JLabel generalLabel;
    private javax.swing.JCheckBox ignoreMVCNodeCheckBox;
    private javax.swing.JLabel ignoreMessageLabel;
    private javax.swing.JLabel testCasePrefixLabel;
    private javax.swing.JLabel testCasePrefixSuffixLabel;
    private javax.swing.JTextField testCasePrefixTextField;
    private javax.swing.JLabel testCaseSuffixLabel;
    private javax.swing.JTextField testCaseSuffixTextField;
    private javax.swing.JLabel testGroupAnnotationLabel;
    private javax.swing.JTextField testGroupAnnotationTextField;
    private javax.swing.JLabel testLabel;
    private javax.swing.JCheckBox useTestCaseMethodCheckBox;
    // End of variables declaration//GEN-END:variables

    private class DefaultDocumentListener implements DocumentListener {

        public DefaultDocumentListener() {
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            fireChange();
        }
    }
}
