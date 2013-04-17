/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.fuel.commands.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.fuel.commands.Oil;
import org.netbeans.modules.php.fuel.util.FuelUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author junichi11
 */
public class FuelPhpGeneratePanel extends JPanel {

    private static final long serialVersionUID = 6702434245614578291L;
    private static final String GENERATE_COMMAND = "generate"; // NOI18N
    private static final String ADMIN_COMMAND = "admin"; // NOI18N
    private static final String CONFIG_COMMAND = "config"; // NOI18N
    private static final String CONTROLLER_COMMAND = "controller"; // NOI18N
    private static final String MIGRATION_COMMAND = "migration"; // NOI18N
    private static final String MODEL_COMMAND = "model"; // NOI18N
    private static final String SCAFFOLD_COMMAND = "scaffold"; // NOI18N
    private static final String TASK_COMMAND = "task"; // NOI18N
    private static final String VIEWS_COMMAND = "views"; // NOI18N
    // migragion
    private static final String MIGRATION_CREATE_TABLE_FORMAT = "create_%s"; // NOI18N
    private static final String MIGRATION_RENAME_TABLE_FORMAT = "rename_table_%s_to_%s"; // NOI18N
    private static final String MIGRATION_DROP_TABLE_FORMAT = "drop_%s"; // NOI18N
    private static final String MIGRATION_ADD_FIELD_FORMAT = "add_%s_to_%s"; // NOI18N
    private static final String MIGRATION_RENAME_FIELD_FORMAT = "rename_field_%s_to_%s_in_%s"; // NOI18N
    private static final String MIGRATION_DELETE_FIELD_FORMAT = "delete_%s_from_%s"; // NOI18N
    private static final List<String> DEFAUL_PARAMS = Arrays.asList(GENERATE_COMMAND);
    private static final String[] MODEL_TYPES = {
        "blob",
        "date",
        "datetime",
        "decimal",
        "enum",
        "float",
        "int",
        "string",
        "text",
        "time",
        "timestamp",
        "varchar"};
    // model table
    private static final int MODEL_FIELD_NAME = 0;
    private static final int MODEL_FIELD_TYPE = 1;
    private static final int MODEL_FIELD_SIZE = 2;
    private static final int MODEL_FIELD_DEFAULT = 3;
    private static final int MODEL_FIELD_OTHERS = 4;
    private static final int MODEL_FIELD_NULL = 5;
    // config table
    private static final int CONFIG_KEY = 0;
    private static final int CONFIG_VALUE = 1;
    private final PhpModule phpModule;

    /**
     * Creates new form FuelPhpGeneratePanel
     */
    public FuelPhpGeneratePanel(PhpModule phpModule) {
        this.phpModule = phpModule;
        initComponents();
        JComboBox combo = new JComboBox(MODEL_TYPES);
        DefaultCellEditor cellEditor = new DefaultCellEditor(combo);
        modelTable.getColumn("Type").setCellEditor(cellEditor); // NOI18N
        migrationTable.getColumn("Type").setCellEditor(cellEditor); // NOI18N
        setViewsControllerNameCombobox();
        setControllerExtendsComboBox();
    }

    /**
     * Get parameters.
     *
     * @return parameters
     */
    public List<String> getParameters() {
        // get current tab
        Component selectedComponent = generateTabbedPane.getSelectedComponent();
        List<String> params = new ArrayList<String>();
        params.addAll(DEFAUL_PARAMS);
        if (selectedComponent == null) {
            return params;
        }

        // get params for sub command
        if (selectedComponent.equals(configPanel)) {
            params.addAll(getConfigParameters());
        } else if (selectedComponent.equals(controllerPanel)) {
            params.addAll(getControllerParameters());
        } else if (selectedComponent.equals(modelPanel)) {
            // model, admin, scaffold
            params.addAll(getModelParameters());
        } else if (selectedComponent.equals(viewsPanel)) {
            params.addAll(getViewsParameters());
        } else if (selectedComponent.equals(taskPanel)) {
            params.addAll(getTaskParameters());
        } else if (selectedComponent.equals(migrationPanel)) {
            params.addAll(getMigrationParameters());
        }

        // runtime options
        if (runtimeForceCheckBox.isSelected()) {
            params.add(runtimeForceCheckBox.getText());
        }
        if (runtimeSkipCheckBox.isSelected()) {
            params.add(runtimeSkipCheckBox.getText());
        }
        if (runtimeQuietCheckBox.isSelected()) {
            params.add(runtimeQuietCheckBox.getText());
        }
        if (runtimeSpeakCheckBox.isSelected()) {
            params.add(runtimeSpeakCheckBox.getText());
        }

        params.addAll(getOthers(runtimeOthersTextField));
        return params;
    }

    private List<String> getOthers(JTextField jtf) {
        List<String> params = new ArrayList<String>();
        String others = jtf.getText();
        if (!StringUtils.isEmpty(others)) {
            others = others.trim().replaceAll(" +", " "); // NOI18N
            params.addAll(StringUtils.explode(others, " ")); // NOI18N
        }
        return params;
    }

    /**
     * Reset table.
     *
     * @param table
     */
    private void resetTable(JTable table) {
        int rowCount = table.getRowCount();
        int columnCount = table.getColumnCount();
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                table.setValueAt(null, i, j);
            }
        }
    }

    /**
     * Add row to table.
     *
     * @param table
     */
    private void addRow(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addRow(new String[]{});
    }

    /**
     * Delete rows.
     *
     * @param table
     */
    private void deleteRows(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int[] selectedRows = table.getSelectedRows();
        Arrays.sort(selectedRows);
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            model.removeRow(selectedRows[i]);
        }
    }

    public final void setViewsControllerNameCombobox() {
        FileObject controllerDirectory = FuelUtils.getControllerDirectory(phpModule);
        DefaultComboBoxModel model = (DefaultComboBoxModel) viewsControllerNameComboBox.getModel();
        model.removeAllElements();
        if (controllerDirectory != null) {
            String path = controllerDirectory.getPath();
            addElement(model, controllerDirectory, path);
        }
    }

    public final void setControllerExtendsComboBox() {
        Object currentItem = controllerExtendsComboBox.getSelectedItem();
        controllerExtendsComboBox.removeAllItems();
        controllerExtendsComboBox.addItem(""); // NOI18N
        controllerExtendsComboBox.setEditable(true);
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        FileObject controllerDirectory = FuelUtils.getControllerDirectory(phpModule);
        FileObject coreDirectory = FuelUtils.getCoreDirectory(phpModule);
        FileObject coreControllerDirectory = null;
        if (coreDirectory != null) {
            coreControllerDirectory = coreDirectory.getFileObject("classes/controller"); // NOI18N
        }

        List<FileObject> controllers = new LinkedList<FileObject>();

        // core
        if (coreControllerDirectory != null) {
            Enumeration<? extends FileObject> children = coreControllerDirectory.getChildren(true);
            controllers.addAll(Collections.list(children));
        }

        // app
        if (coreControllerDirectory != null) {
            Enumeration<? extends FileObject> children = controllerDirectory.getChildren(true);
            controllers.addAll(Collections.list(children));
        }

        // add items
        controllerExtendsComboBox.addItem("Controller"); // NOI18N
        FuelUtils.sortFileObject(controllers);
        for (FileObject controller : controllers) {
            for (PhpClass phpClass : editorSupport.getClasses(controller)) {
                String className = phpClass.getName();
                if (className.startsWith(FuelUtils.CONTROLLER_PREFIX)) {
                    controllerExtendsComboBox.addItem(className);
                }
            }
        }
        controllerExtendsComboBox.setSelectedItem(currentItem);
    }

    /**
     * Add element to ComboBoxModel.
     *
     * @param model
     * @param folder
     * @param rootPath
     */
    private void addElement(DefaultComboBoxModel model, FileObject folder, String rootPath) {
        FileObject[] children = folder.getChildren();
        FuelUtils.sortFileObject(children);
        for (FileObject child : children) {
            if (child.isFolder()) {
                addElement(model, child, rootPath);
            }
            if (!FileUtils.isPhpFile(child)) {
                continue;
            }
            String path = child.getPath();
            path = path.replace(rootPath, ""); // NOI18N
            if (path.startsWith("/")) { // NOI18N
                path = path.replaceFirst("/", ""); // NOI18N
            }
            path = path.replace(".php", ""); // NOI18N
            model.addElement(path);
        }
    }

    public void setAdminAndScaffoldComboBox() {
        FileObject fuelDirectory = FuelUtils.getFuelDirectory(phpModule);
        FileObject targetDirectory = null;
        adminAndScaffoldComboBox.removeAllItems();
        if (adminRadioButton.isSelected()) {
            targetDirectory = fuelDirectory.getFileObject("packages/oil/views/admin"); // NOI18N
        } else if (scaffoldRadioButton.isSelected()) {
            targetDirectory = fuelDirectory.getFileObject("packages/oil/views/scaffolding"); // NOI18N
        }
        if (targetDirectory == null) {
            return;
        }
        FileObject[] children = targetDirectory.getChildren();
        FuelUtils.sortFileObject(children);
        adminAndScaffoldComboBox.addItem(""); // NOI18N
        for (FileObject child : children) {
            if (child.isFolder()) {
                adminAndScaffoldComboBox.addItem(child.getName());
            }
        }
    }

    private void setEnabledMigrationTable(boolean isEnabled) {
        migrationScrollPane.setEnabled(isEnabled);
        migrationTable.clearSelection();
        migrationTable.setEnabled(isEnabled);
        migrationAddRowButton.setEnabled(isEnabled);
        migrationDeleteRowsButton.setEnabled(isEnabled);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        modelAdminScaffoldButtonGroup = new javax.swing.ButtonGroup();
        migrationButtonGroup = new javax.swing.ButtonGroup();
        generateTabbedPane = new javax.swing.JTabbedPane();
        configPanel = new javax.swing.JPanel();
        configNameLabel = new javax.swing.JLabel();
        configNameTextField = new javax.swing.JTextField();
        configOverwriteCheckBox = new javax.swing.JCheckBox();
        configModuleLabel = new javax.swing.JLabel();
        configModuleTextField = new javax.swing.JTextField();
        configScrollPane = new javax.swing.JScrollPane();
        configTable = new javax.swing.JTable();
        configAddRowButton = new javax.swing.JButton();
        configDeleteRowsButton = new javax.swing.JButton();
        configResetTableButton = new javax.swing.JButton();
        configOthersLabel = new javax.swing.JLabel();
        configOthersTextField = new javax.swing.JTextField();
        controllerPanel = new javax.swing.JPanel();
        controllerNameLabel = new javax.swing.JLabel();
        controllerNameTextField = new javax.swing.JTextField();
        controllerExtendsLabel = new javax.swing.JLabel();
        controllerWithViewmodelCheckBox = new javax.swing.JCheckBox();
        controllerCrudCheckBox = new javax.swing.JCheckBox();
        controllerOthersLabel = new javax.swing.JLabel();
        controllerOthersTextField = new javax.swing.JTextField();
        controllerResetTableButton = new javax.swing.JButton();
        controllerScrollPane = new javax.swing.JScrollPane();
        controllerTable = new javax.swing.JTable();
        controllerAddRowButton = new javax.swing.JButton();
        controllerDeleteRowsButton = new javax.swing.JButton();
        controllerExtendsComboBox = new javax.swing.JComboBox();
        viewsPanel = new javax.swing.JPanel();
        viewsControllerNameLabel = new javax.swing.JLabel();
        viewsScrollPane = new javax.swing.JScrollPane();
        viewsTable = new javax.swing.JTable();
        viewsWithViewmodelCheckBox = new javax.swing.JCheckBox();
        viewsAddRowButton = new javax.swing.JButton();
        viewsDeleteRowsButton = new javax.swing.JButton();
        viewsResetTableButton = new javax.swing.JButton();
        viewsControllerNameComboBox = new javax.swing.JComboBox();
        modelPanel = new javax.swing.JPanel();
        modelNameLabel = new javax.swing.JLabel();
        modelNameTextField = new javax.swing.JTextField();
        modelScrollPane1 = new javax.swing.JScrollPane();
        modelTable = new javax.swing.JTable();
        modelCreatedAtLabel = new javax.swing.JLabel();
        modelUpdatedAtLabel = new javax.swing.JLabel();
        modelCreatedAtTextField = new javax.swing.JTextField();
        modelUpdatedAtTextField = new javax.swing.JTextField();
        modelNoMigrationCheckBox = new javax.swing.JCheckBox();
        modelCrudCheckBox = new javax.swing.JCheckBox();
        modelNoTimestampCheckBox = new javax.swing.JCheckBox();
        modelNoPropertiesCheckBox = new javax.swing.JCheckBox();
        modelMysqlTimestampCheckBox = new javax.swing.JCheckBox();
        modelSingularCheckBox = new javax.swing.JCheckBox();
        modelResetTableButton = new javax.swing.JButton();
        modelResetAllButton = new javax.swing.JButton();
        modelAddRowButton = new javax.swing.JButton();
        modelDeleteRowsButton = new javax.swing.JButton();
        modelOthersLabel = new javax.swing.JLabel();
        modelOthersTextField = new javax.swing.JTextField();
        adminAndScaffoldComboBox = new javax.swing.JComboBox();
        adminAndScaffoldLabel = new javax.swing.JLabel();
        modelRadioButton = new javax.swing.JRadioButton();
        adminRadioButton = new javax.swing.JRadioButton();
        scaffoldRadioButton = new javax.swing.JRadioButton();
        taskPanel = new javax.swing.JPanel();
        taskNameLabel = new javax.swing.JLabel();
        taskNameTextField = new javax.swing.JTextField();
        taskScrollPane = new javax.swing.JScrollPane();
        taskTable = new javax.swing.JTable();
        taskAddRowButton = new javax.swing.JButton();
        taskDeleteRowsButton = new javax.swing.JButton();
        taskResetTableButton = new javax.swing.JButton();
        migrationPanel = new javax.swing.JPanel();
        migrationCreateTableRadioButton = new javax.swing.JRadioButton();
        migrationRenameTableRadioButton = new javax.swing.JRadioButton();
        migrationDropTableRadioButton = new javax.swing.JRadioButton();
        migrationAddFieldRadioButton = new javax.swing.JRadioButton();
        migrationRenameFieldRadioButton = new javax.swing.JRadioButton();
        migrationDeleteFieldRadioButton = new javax.swing.JRadioButton();
        migrationScrollPane = new javax.swing.JScrollPane();
        migrationTable = new javax.swing.JTable();
        migrationDeleteRowsButton = new javax.swing.JButton();
        migrationAddRowButton = new javax.swing.JButton();
        migrationResetAllButton = new javax.swing.JButton();
        migrationResetTableButton = new javax.swing.JButton();
        migrationTableNameLabel = new javax.swing.JLabel();
        migrationFromLabel = new javax.swing.JLabel();
        migrationToLabel = new javax.swing.JLabel();
        migrationTableNameTextField = new javax.swing.JTextField();
        migrationFromTextField = new javax.swing.JTextField();
        migrationToTextField = new javax.swing.JTextField();
        migrationNameLabel = new javax.swing.JLabel();
        migrationNameTextField = new javax.swing.JTextField();
        runtimeForceCheckBox = new javax.swing.JCheckBox();
        runtimeSkipCheckBox = new javax.swing.JCheckBox();
        runtimeQuietCheckBox = new javax.swing.JCheckBox();
        runtimeSpeakCheckBox = new javax.swing.JCheckBox();
        runWithoutClosingButton = new javax.swing.JButton();
        runtimeOthersLabel = new javax.swing.JLabel();
        runtimeOthersTextField = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(configNameLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.configNameLabel.text")); // NOI18N

        configNameTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.configNameTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(configOverwriteCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.configOverwriteCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(configModuleLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.configModuleLabel.text")); // NOI18N

        configModuleTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.configModuleTextField.text")); // NOI18N

        configTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Key", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        configTable.getTableHeader().setReorderingAllowed(false);
        configScrollPane.setViewportView(configTable);
        configTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.configTable.columnModel.title0")); // NOI18N
        configTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.configTable.columnModel.title1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(configAddRowButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.configAddRowButton.text")); // NOI18N
        configAddRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configAddRowButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(configDeleteRowsButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.configDeleteRowsButton.text")); // NOI18N
        configDeleteRowsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configDeleteRowsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(configResetTableButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.configResetTableButton.text")); // NOI18N
        configResetTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configResetTableButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(configOthersLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.configOthersLabel.text")); // NOI18N

        configOthersTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.configOthersTextField.text")); // NOI18N

        javax.swing.GroupLayout configPanelLayout = new javax.swing.GroupLayout(configPanel);
        configPanel.setLayout(configPanelLayout);
        configPanelLayout.setHorizontalGroup(
            configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(configPanelLayout.createSequentialGroup()
                        .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(configPanelLayout.createSequentialGroup()
                                .addGap(305, 305, 305)
                                .addComponent(configModuleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(configPanelLayout.createSequentialGroup()
                                .addComponent(configNameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(configNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(configModuleLabel)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(configOthersLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(configOthersTextField))
                    .addGroup(configPanelLayout.createSequentialGroup()
                        .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(configPanelLayout.createSequentialGroup()
                                .addComponent(configScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 463, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(configAddRowButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(configDeleteRowsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(configResetTableButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(configOverwriteCheckBox))
                        .addGap(0, 224, Short.MAX_VALUE)))
                .addContainerGap())
        );
        configPanelLayout.setVerticalGroup(
            configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configNameLabel)
                    .addComponent(configNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configModuleLabel)
                    .addComponent(configModuleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configOthersLabel)
                    .addComponent(configOthersTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(configOverwriteCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(configScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(configPanelLayout.createSequentialGroup()
                        .addComponent(configAddRowButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(configDeleteRowsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(configResetTableButton)))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        generateTabbedPane.addTab(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.configPanel.TabConstraints.tabTitle"), configPanel); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(controllerNameLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.controllerNameLabel.text")); // NOI18N

        controllerNameTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.controllerNameTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(controllerExtendsLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.controllerExtendsLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(controllerWithViewmodelCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.controllerWithViewmodelCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(controllerCrudCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.controllerCrudCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(controllerOthersLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.controllerOthersLabel.text")); // NOI18N

        controllerOthersTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.controllerOthersTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(controllerResetTableButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.controllerResetTableButton.text")); // NOI18N
        controllerResetTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                controllerResetTableButtonActionPerformed(evt);
            }
        });

        controllerTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"index"},
                {"create"},
                {"edit"},
                {"view"}
            },
            new String [] {
                "View"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        controllerTable.getTableHeader().setReorderingAllowed(false);
        controllerScrollPane.setViewportView(controllerTable);
        controllerTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.viewsTable.columnModel.title0")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(controllerAddRowButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.controllerAddRowButton.text")); // NOI18N
        controllerAddRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                controllerAddRowButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(controllerDeleteRowsButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.controllerDeleteRowsButton.text")); // NOI18N
        controllerDeleteRowsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                controllerDeleteRowsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout controllerPanelLayout = new javax.swing.GroupLayout(controllerPanel);
        controllerPanel.setLayout(controllerPanelLayout);
        controllerPanelLayout.setHorizontalGroup(
            controllerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controllerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controllerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(controllerPanelLayout.createSequentialGroup()
                        .addGroup(controllerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(controllerWithViewmodelCheckBox)
                            .addComponent(controllerCrudCheckBox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 234, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controllerPanelLayout.createSequentialGroup()
                        .addGroup(controllerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(controllerPanelLayout.createSequentialGroup()
                                .addComponent(controllerOthersLabel)
                                .addGap(39, 39, 39)
                                .addComponent(controllerOthersTextField))
                            .addGroup(controllerPanelLayout.createSequentialGroup()
                                .addGroup(controllerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(controllerExtendsLabel)
                                    .addComponent(controllerNameLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(controllerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(controllerExtendsComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(controllerNameTextField))))
                        .addGap(6, 6, 6)))
                .addComponent(controllerScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controllerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(controllerResetTableButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(controllerAddRowButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(controllerDeleteRowsButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        controllerPanelLayout.setVerticalGroup(
            controllerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controllerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controllerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(controllerScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                    .addGroup(controllerPanelLayout.createSequentialGroup()
                        .addGroup(controllerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(controllerPanelLayout.createSequentialGroup()
                                .addGroup(controllerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(controllerNameLabel)
                                    .addComponent(controllerNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(controllerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(controllerExtendsLabel)
                                    .addComponent(controllerExtendsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(controllerWithViewmodelCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(controllerCrudCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(controllerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(controllerOthersLabel)
                                    .addComponent(controllerOthersTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(controllerPanelLayout.createSequentialGroup()
                                .addComponent(controllerAddRowButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(controllerDeleteRowsButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(controllerResetTableButton)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        generateTabbedPane.addTab(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.controllerPanel.TabConstraints.tabTitle"), controllerPanel); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(viewsControllerNameLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.viewsControllerNameLabel.text")); // NOI18N

        viewsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"index"},
                {"create"},
                {"edit"},
                {"view"}
            },
            new String [] {
                "View"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        viewsTable.getTableHeader().setReorderingAllowed(false);
        viewsScrollPane.setViewportView(viewsTable);
        viewsTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.viewsTable.columnModel.title0")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(viewsWithViewmodelCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.viewsWithViewmodelCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(viewsAddRowButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.viewsAddRowButton.text")); // NOI18N
        viewsAddRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewsAddRowButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(viewsDeleteRowsButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.viewsDeleteRowsButton.text")); // NOI18N
        viewsDeleteRowsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewsDeleteRowsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(viewsResetTableButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.viewsResetTableButton.text")); // NOI18N
        viewsResetTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewsResetTableButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout viewsPanelLayout = new javax.swing.GroupLayout(viewsPanel);
        viewsPanel.setLayout(viewsPanelLayout);
        viewsPanelLayout.setHorizontalGroup(
            viewsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(viewsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(viewsWithViewmodelCheckBox)
                    .addGroup(viewsPanelLayout.createSequentialGroup()
                        .addComponent(viewsControllerNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(viewsControllerNameComboBox, 0, 251, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(viewsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(viewsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(viewsAddRowButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(viewsDeleteRowsButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(viewsResetTableButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        viewsPanelLayout.setVerticalGroup(
            viewsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(viewsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(viewsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                    .addGroup(viewsPanelLayout.createSequentialGroup()
                        .addGroup(viewsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(viewsControllerNameLabel)
                            .addComponent(viewsAddRowButton)
                            .addComponent(viewsControllerNameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(viewsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(viewsPanelLayout.createSequentialGroup()
                                .addComponent(viewsDeleteRowsButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(viewsResetTableButton))
                            .addComponent(viewsWithViewmodelCheckBox))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        generateTabbedPane.addTab(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.viewsPanel.TabConstraints.tabTitle"), viewsPanel); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(modelNameLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelNameLabel.text")); // NOI18N

        modelNameTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelNameTextField.text")); // NOI18N

        modelTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Name", "Type", "Size", "Default", "Others", "NULL"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        modelTable.getTableHeader().setReorderingAllowed(false);
        modelScrollPane1.setViewportView(modelTable);
        modelTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelTable.columnModel.title0")); // NOI18N
        modelTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelTable.columnModel.title1")); // NOI18N
        modelTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        modelTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelTable.columnModel.title2")); // NOI18N
        modelTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelTable.columnModel.title3")); // NOI18N
        modelTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelTable.columnModel.title4")); // NOI18N
        modelTable.getColumnModel().getColumn(5).setPreferredWidth(45);
        modelTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelTable.columnModel.title5")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(modelCreatedAtLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelCreatedAtLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(modelUpdatedAtLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelUpdatedAtLabel.text")); // NOI18N

        modelCreatedAtTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelCreatedAtTextField.text")); // NOI18N

        modelUpdatedAtTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelUpdatedAtTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(modelNoMigrationCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelNoMigrationCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(modelCrudCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelCrudCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(modelNoTimestampCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelNoTimestampCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(modelNoPropertiesCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelNoPropertiesCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(modelMysqlTimestampCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelMysqlTimestampCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(modelSingularCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelSingularCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(modelResetTableButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelResetTableButton.text")); // NOI18N
        modelResetTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modelResetTableButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(modelResetAllButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelResetAllButton.text")); // NOI18N
        modelResetAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modelResetAllButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(modelAddRowButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelAddRowButton.text")); // NOI18N
        modelAddRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modelAddRowButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(modelDeleteRowsButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelDeleteRowsButton.text")); // NOI18N
        modelDeleteRowsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modelDeleteRowsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(modelOthersLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelOthersLabel.text")); // NOI18N

        modelOthersTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelOthersTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(adminAndScaffoldLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.adminAndScaffoldLabel.text")); // NOI18N

        modelAdminScaffoldButtonGroup.add(modelRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(modelRadioButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelRadioButton.text")); // NOI18N
        modelRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modelRadioButtonActionPerformed(evt);
            }
        });

        modelAdminScaffoldButtonGroup.add(adminRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(adminRadioButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.adminRadioButton.text")); // NOI18N
        adminRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminRadioButtonActionPerformed(evt);
            }
        });

        modelAdminScaffoldButtonGroup.add(scaffoldRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(scaffoldRadioButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.scaffoldRadioButton.text")); // NOI18N
        scaffoldRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaffoldRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout modelPanelLayout = new javax.swing.GroupLayout(modelPanel);
        modelPanel.setLayout(modelPanelLayout);
        modelPanelLayout.setHorizontalGroup(
            modelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modelPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(modelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(modelPanelLayout.createSequentialGroup()
                        .addComponent(modelResetTableButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modelResetAllButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(modelDeleteRowsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modelAddRowButton))
                    .addComponent(modelScrollPane1)
                    .addGroup(modelPanelLayout.createSequentialGroup()
                        .addGroup(modelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(modelPanelLayout.createSequentialGroup()
                                .addComponent(modelRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(adminRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scaffoldRadioButton))
                            .addGroup(modelPanelLayout.createSequentialGroup()
                                .addComponent(modelNameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(modelNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(modelOthersLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(modelOthersTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 23, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(modelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(modelSingularCheckBox)
                        .addGroup(modelPanelLayout.createSequentialGroup()
                            .addGroup(modelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(modelCreatedAtLabel)
                                .addComponent(modelUpdatedAtLabel)
                                .addComponent(modelCrudCheckBox))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(modelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(modelCreatedAtTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                .addComponent(modelUpdatedAtTextField)))
                        .addComponent(modelNoMigrationCheckBox)
                        .addComponent(modelNoTimestampCheckBox)
                        .addComponent(modelNoPropertiesCheckBox)
                        .addComponent(modelMysqlTimestampCheckBox)
                        .addComponent(adminAndScaffoldComboBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(adminAndScaffoldLabel))
                .addContainerGap())
        );
        modelPanelLayout.setVerticalGroup(
            modelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modelPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(modelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modelCreatedAtLabel)
                    .addComponent(modelCreatedAtTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modelRadioButton)
                    .addComponent(adminRadioButton)
                    .addComponent(scaffoldRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modelUpdatedAtLabel)
                    .addComponent(modelUpdatedAtTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modelNameLabel)
                    .addComponent(modelNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modelOthersLabel)
                    .addComponent(modelOthersTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(modelPanelLayout.createSequentialGroup()
                        .addComponent(modelCrudCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modelNoMigrationCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modelNoTimestampCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modelNoPropertiesCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modelMysqlTimestampCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modelSingularCheckBox)
                        .addGap(27, 27, 27)
                        .addComponent(adminAndScaffoldLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(adminAndScaffoldComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(modelScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modelResetTableButton)
                    .addComponent(modelResetAllButton)
                    .addComponent(modelAddRowButton)
                    .addComponent(modelDeleteRowsButton))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        generateTabbedPane.addTab(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelPanel.TabConstraints.tabTitle"), modelPanel); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(taskNameLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.taskNameLabel.text")); // NOI18N

        taskNameTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.taskNameTextField.text")); // NOI18N

        taskTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "command"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        taskTable.getTableHeader().setReorderingAllowed(false);
        taskScrollPane.setViewportView(taskTable);
        taskTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.taskTable.columnModel.title0")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(taskAddRowButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.taskAddRowButton.text")); // NOI18N
        taskAddRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                taskAddRowButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(taskDeleteRowsButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.taskDeleteRowsButton.text")); // NOI18N
        taskDeleteRowsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                taskDeleteRowsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(taskResetTableButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.taskResetTableButton.text")); // NOI18N
        taskResetTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                taskResetTableButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout taskPanelLayout = new javax.swing.GroupLayout(taskPanel);
        taskPanel.setLayout(taskPanelLayout);
        taskPanelLayout.setHorizontalGroup(
            taskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(taskPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(taskNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(taskNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(taskScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(taskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(taskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(taskResetTableButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(taskAddRowButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(taskDeleteRowsButton))
                .addContainerGap())
        );
        taskPanelLayout.setVerticalGroup(
            taskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(taskPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(taskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(taskScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                    .addGroup(taskPanelLayout.createSequentialGroup()
                        .addGroup(taskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(taskPanelLayout.createSequentialGroup()
                                .addComponent(taskAddRowButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(taskDeleteRowsButton))
                            .addGroup(taskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(taskNameLabel)
                                .addComponent(taskNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(taskResetTableButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        generateTabbedPane.addTab(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.taskPanel.TabConstraints.tabTitle"), taskPanel); // NOI18N

        migrationButtonGroup.add(migrationCreateTableRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(migrationCreateTableRadioButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationCreateTableRadioButton.text")); // NOI18N
        migrationCreateTableRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                migrationCreateTableRadioButtonActionPerformed(evt);
            }
        });

        migrationButtonGroup.add(migrationRenameTableRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(migrationRenameTableRadioButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationRenameTableRadioButton.text")); // NOI18N
        migrationRenameTableRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                migrationRenameTableRadioButtonActionPerformed(evt);
            }
        });

        migrationButtonGroup.add(migrationDropTableRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(migrationDropTableRadioButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationDropTableRadioButton.text")); // NOI18N
        migrationDropTableRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                migrationDropTableRadioButtonActionPerformed(evt);
            }
        });

        migrationButtonGroup.add(migrationAddFieldRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(migrationAddFieldRadioButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationAddFieldRadioButton.text")); // NOI18N
        migrationAddFieldRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                migrationAddFieldRadioButtonActionPerformed(evt);
            }
        });

        migrationButtonGroup.add(migrationRenameFieldRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(migrationRenameFieldRadioButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationRenameFieldRadioButton.text")); // NOI18N
        migrationRenameFieldRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                migrationRenameFieldRadioButtonActionPerformed(evt);
            }
        });

        migrationButtonGroup.add(migrationDeleteFieldRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(migrationDeleteFieldRadioButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationDeleteFieldRadioButton.text")); // NOI18N
        migrationDeleteFieldRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                migrationDeleteFieldRadioButtonActionPerformed(evt);
            }
        });

        migrationTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Field Name", "Type", "Size", "Default", "Others", "NULL"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        migrationTable.getTableHeader().setReorderingAllowed(false);
        migrationScrollPane.setViewportView(migrationTable);
        migrationTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelTable.columnModel.title0")); // NOI18N
        migrationTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelTable.columnModel.title1")); // NOI18N
        migrationTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        migrationTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelTable.columnModel.title2")); // NOI18N
        migrationTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelTable.columnModel.title3")); // NOI18N
        migrationTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelTable.columnModel.title4")); // NOI18N
        migrationTable.getColumnModel().getColumn(5).setPreferredWidth(45);
        migrationTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.modelTable.columnModel.title5")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(migrationDeleteRowsButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationDeleteRowsButton.text")); // NOI18N
        migrationDeleteRowsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                migrationDeleteRowsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(migrationAddRowButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationAddRowButton.text")); // NOI18N
        migrationAddRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                migrationAddRowButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(migrationResetAllButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationResetAllButton.text")); // NOI18N
        migrationResetAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                migrationResetAllButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(migrationResetTableButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationResetTableButton.text")); // NOI18N
        migrationResetTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                migrationResetTableButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(migrationTableNameLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationTableNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(migrationFromLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationFromLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(migrationToLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationToLabel.text")); // NOI18N

        migrationTableNameTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationTableNameTextField.text")); // NOI18N
        migrationTableNameTextField.setNextFocusableComponent(migrationFromTextField);

        migrationFromTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationFromTextField.text")); // NOI18N
        migrationFromTextField.setNextFocusableComponent(migrationToTextField);

        migrationToTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationToTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(migrationNameLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationNameLabel.text")); // NOI18N

        migrationNameTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationNameTextField.text")); // NOI18N
        migrationNameTextField.setNextFocusableComponent(migrationTableNameTextField);

        javax.swing.GroupLayout migrationPanelLayout = new javax.swing.GroupLayout(migrationPanel);
        migrationPanel.setLayout(migrationPanelLayout);
        migrationPanelLayout.setHorizontalGroup(
            migrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(migrationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(migrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(migrationCreateTableRadioButton)
                    .addComponent(migrationAddFieldRadioButton)
                    .addComponent(migrationRenameFieldRadioButton)
                    .addComponent(migrationDeleteFieldRadioButton)
                    .addComponent(migrationRenameTableRadioButton)
                    .addComponent(migrationDropTableRadioButton))
                .addGap(62, 62, 62)
                .addGroup(migrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(migrationScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                    .addGroup(migrationPanelLayout.createSequentialGroup()
                        .addComponent(migrationResetTableButton, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(migrationResetAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(migrationDeleteRowsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(migrationAddRowButton, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, migrationPanelLayout.createSequentialGroup()
                        .addGroup(migrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(migrationNameLabel)
                            .addComponent(migrationFromLabel)
                            .addComponent(migrationTableNameLabel)
                            .addComponent(migrationToLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(migrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(migrationFromTextField)
                            .addComponent(migrationToTextField)
                            .addComponent(migrationNameTextField)
                            .addComponent(migrationTableNameTextField))))
                .addContainerGap())
        );
        migrationPanelLayout.setVerticalGroup(
            migrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(migrationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(migrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(migrationCreateTableRadioButton)
                    .addComponent(migrationNameLabel)
                    .addComponent(migrationNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(migrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(migrationRenameTableRadioButton)
                    .addComponent(migrationTableNameLabel)
                    .addComponent(migrationTableNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(migrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(migrationDropTableRadioButton)
                    .addComponent(migrationFromLabel)
                    .addComponent(migrationFromTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(migrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(migrationAddFieldRadioButton)
                    .addComponent(migrationToLabel)
                    .addComponent(migrationToTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(migrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(migrationPanelLayout.createSequentialGroup()
                        .addComponent(migrationRenameFieldRadioButton)
                        .addGap(6, 6, 6)
                        .addComponent(migrationDeleteFieldRadioButton)
                        .addGap(0, 183, Short.MAX_VALUE))
                    .addGroup(migrationPanelLayout.createSequentialGroup()
                        .addComponent(migrationScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(migrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(migrationResetTableButton)
                            .addComponent(migrationResetAllButton)
                            .addComponent(migrationAddRowButton)
                            .addComponent(migrationDeleteRowsButton))))
                .addContainerGap())
        );

        generateTabbedPane.addTab(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.migrationPanel.TabConstraints.tabTitle"), migrationPanel); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(runtimeForceCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.runtimeForceCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(runtimeSkipCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.runtimeSkipCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(runtimeQuietCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.runtimeQuietCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(runtimeSpeakCheckBox, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.runtimeSpeakCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(runWithoutClosingButton, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.runWithoutClosingButton.text")); // NOI18N
        runWithoutClosingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runWithoutClosingButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(runtimeOthersLabel, org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.runtimeOthersLabel.text")); // NOI18N

        runtimeOthersTextField.setText(org.openide.util.NbBundle.getMessage(FuelPhpGeneratePanel.class, "FuelPhpGeneratePanel.runtimeOthersTextField.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(generateTabbedPane)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(runtimeOthersLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(runtimeOthersTextField))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(runtimeForceCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(runtimeSkipCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(runtimeQuietCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(runtimeSpeakCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(runWithoutClosingButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(generateTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 435, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runtimeOthersLabel)
                    .addComponent(runtimeOthersTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runtimeForceCheckBox)
                    .addComponent(runtimeSkipCheckBox)
                    .addComponent(runtimeQuietCheckBox)
                    .addComponent(runtimeSpeakCheckBox)
                    .addComponent(runWithoutClosingButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void modelResetTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelResetTableButtonActionPerformed
        resetTable(modelTable);
    }//GEN-LAST:event_modelResetTableButtonActionPerformed

    private void modelResetAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelResetAllButtonActionPerformed
        modelNameTextField.setText(""); // NOI18N
        modelCreatedAtTextField.setText(""); // NOI18N
        modelUpdatedAtTextField.setText(""); // NOI18N
        modelCrudCheckBox.setSelected(false);
        modelNoMigrationCheckBox.setSelected(false);
        modelNoTimestampCheckBox.setSelected(false);
        modelNoPropertiesCheckBox.setSelected(false);
        modelMysqlTimestampCheckBox.setSelected(false);
        modelSingularCheckBox.setSelected(false);
        modelResetTableButtonActionPerformed(evt);
    }//GEN-LAST:event_modelResetAllButtonActionPerformed

    private void runWithoutClosingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runWithoutClosingButtonActionPerformed
        try {
            // run command
            Oil.forPhpModule(phpModule, true).runCommand(phpModule, getParameters(), new RefreshPhpModuleRunnable(phpModule));
        } catch (InvalidPhpExecutableException ex) {
            Exceptions.printStackTrace(ex);
        }
        Component selectedComponent = generateTabbedPane.getSelectedComponent();
        if (selectedComponent.equals(controllerPanel)) {
            setViewsControllerNameCombobox();
        }
    }//GEN-LAST:event_runWithoutClosingButtonActionPerformed

    private void configAddRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configAddRowButtonActionPerformed
        addRow(configTable);
    }//GEN-LAST:event_configAddRowButtonActionPerformed

    private void modelAddRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelAddRowButtonActionPerformed
        addRow(modelTable);
    }//GEN-LAST:event_modelAddRowButtonActionPerformed

    private void modelDeleteRowsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelDeleteRowsButtonActionPerformed
        deleteRows(modelTable);
    }//GEN-LAST:event_modelDeleteRowsButtonActionPerformed

    private void configDeleteRowsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configDeleteRowsButtonActionPerformed
        deleteRows(configTable);
    }//GEN-LAST:event_configDeleteRowsButtonActionPerformed

    private void configResetTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configResetTableButtonActionPerformed
        resetTable(configTable);
    }//GEN-LAST:event_configResetTableButtonActionPerformed

    private void viewsAddRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewsAddRowButtonActionPerformed
        addRow(viewsTable);
    }//GEN-LAST:event_viewsAddRowButtonActionPerformed

    private void viewsResetTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewsResetTableButtonActionPerformed
        resetTable(viewsTable);
    }//GEN-LAST:event_viewsResetTableButtonActionPerformed

    private void viewsDeleteRowsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewsDeleteRowsButtonActionPerformed
        deleteRows(viewsTable);
    }//GEN-LAST:event_viewsDeleteRowsButtonActionPerformed

    private void controllerResetTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_controllerResetTableButtonActionPerformed
        resetTable(controllerTable);
    }//GEN-LAST:event_controllerResetTableButtonActionPerformed

    private void controllerAddRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_controllerAddRowButtonActionPerformed
        addRow(controllerTable);
    }//GEN-LAST:event_controllerAddRowButtonActionPerformed

    private void controllerDeleteRowsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_controllerDeleteRowsButtonActionPerformed
        deleteRows(controllerTable);
    }//GEN-LAST:event_controllerDeleteRowsButtonActionPerformed

    private void taskAddRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_taskAddRowButtonActionPerformed
        addRow(taskTable);
    }//GEN-LAST:event_taskAddRowButtonActionPerformed

    private void taskDeleteRowsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_taskDeleteRowsButtonActionPerformed
        deleteRows(taskTable);
    }//GEN-LAST:event_taskDeleteRowsButtonActionPerformed

    private void taskResetTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_taskResetTableButtonActionPerformed
        resetTable(taskTable);
    }//GEN-LAST:event_taskResetTableButtonActionPerformed

    private void adminRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminRadioButtonActionPerformed
        setAdminAndScaffoldComboBox();
    }//GEN-LAST:event_adminRadioButtonActionPerformed

    private void scaffoldRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaffoldRadioButtonActionPerformed
        setAdminAndScaffoldComboBox();
    }//GEN-LAST:event_scaffoldRadioButtonActionPerformed

    private void modelRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelRadioButtonActionPerformed
        setAdminAndScaffoldComboBox();
    }//GEN-LAST:event_modelRadioButtonActionPerformed

    private void migrationDeleteRowsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_migrationDeleteRowsButtonActionPerformed
        deleteRows(migrationTable);
    }//GEN-LAST:event_migrationDeleteRowsButtonActionPerformed

    private void migrationAddRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_migrationAddRowButtonActionPerformed
        addRow(migrationTable);
    }//GEN-LAST:event_migrationAddRowButtonActionPerformed

    private void migrationResetAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_migrationResetAllButtonActionPerformed
        migrationNameTextField.setText(""); // NOI18N
        migrationTableNameTextField.setText(""); // NOI18N
        migrationFromTextField.setText(""); // NOI18N
        migrationToTextField.setText(""); // NOI18N
        migrationResetTableButtonActionPerformed(evt);
    }//GEN-LAST:event_migrationResetAllButtonActionPerformed

    private void migrationResetTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_migrationResetTableButtonActionPerformed
        resetTable(migrationTable);
    }//GEN-LAST:event_migrationResetTableButtonActionPerformed

    private void migrationCreateTableRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_migrationCreateTableRadioButtonActionPerformed
        migrationNameTextField.setEnabled(false);
        migrationTableNameTextField.setEnabled(true);
        migrationFromTextField.setEnabled(false);
        migrationToTextField.setEnabled(false);
        setEnabledMigrationTable(true);
    }//GEN-LAST:event_migrationCreateTableRadioButtonActionPerformed

    private void migrationRenameTableRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_migrationRenameTableRadioButtonActionPerformed
        migrationNameTextField.setEnabled(false);
        migrationTableNameTextField.setEnabled(false);
        migrationFromTextField.setEnabled(true);
        migrationToTextField.setEnabled(true);
        setEnabledMigrationTable(false);
    }//GEN-LAST:event_migrationRenameTableRadioButtonActionPerformed

    private void migrationDropTableRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_migrationDropTableRadioButtonActionPerformed
        migrationNameTextField.setEnabled(false);
        migrationTableNameTextField.setEnabled(true);
        migrationFromTextField.setEnabled(false);
        migrationToTextField.setEnabled(false);
        setEnabledMigrationTable(false);
    }//GEN-LAST:event_migrationDropTableRadioButtonActionPerformed

    private void migrationAddFieldRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_migrationAddFieldRadioButtonActionPerformed
        migrationNameTextField.setEnabled(true);
        migrationTableNameTextField.setEnabled(true);
        migrationFromTextField.setEnabled(false);
        migrationToTextField.setEnabled(false);
        setEnabledMigrationTable(true);
    }//GEN-LAST:event_migrationAddFieldRadioButtonActionPerformed

    private void migrationRenameFieldRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_migrationRenameFieldRadioButtonActionPerformed
        migrationNameTextField.setEnabled(false);
        migrationTableNameTextField.setEnabled(true);
        migrationFromTextField.setEnabled(true);
        migrationToTextField.setEnabled(true);
        setEnabledMigrationTable(false);
    }//GEN-LAST:event_migrationRenameFieldRadioButtonActionPerformed

    private void migrationDeleteFieldRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_migrationDeleteFieldRadioButtonActionPerformed
        migrationNameTextField.setEnabled(true);
        migrationTableNameTextField.setEnabled(true);
        migrationFromTextField.setEnabled(false);
        migrationToTextField.setEnabled(false);
        setEnabledMigrationTable(true);
    }//GEN-LAST:event_migrationDeleteFieldRadioButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox adminAndScaffoldComboBox;
    private javax.swing.JLabel adminAndScaffoldLabel;
    private javax.swing.JRadioButton adminRadioButton;
    private javax.swing.JButton configAddRowButton;
    private javax.swing.JButton configDeleteRowsButton;
    private javax.swing.JLabel configModuleLabel;
    private javax.swing.JTextField configModuleTextField;
    private javax.swing.JLabel configNameLabel;
    private javax.swing.JTextField configNameTextField;
    private javax.swing.JLabel configOthersLabel;
    private javax.swing.JTextField configOthersTextField;
    private javax.swing.JCheckBox configOverwriteCheckBox;
    private javax.swing.JPanel configPanel;
    private javax.swing.JButton configResetTableButton;
    private javax.swing.JScrollPane configScrollPane;
    private javax.swing.JTable configTable;
    private javax.swing.JButton controllerAddRowButton;
    private javax.swing.JCheckBox controllerCrudCheckBox;
    private javax.swing.JButton controllerDeleteRowsButton;
    private javax.swing.JComboBox controllerExtendsComboBox;
    private javax.swing.JLabel controllerExtendsLabel;
    private javax.swing.JLabel controllerNameLabel;
    private javax.swing.JTextField controllerNameTextField;
    private javax.swing.JLabel controllerOthersLabel;
    private javax.swing.JTextField controllerOthersTextField;
    private javax.swing.JPanel controllerPanel;
    private javax.swing.JButton controllerResetTableButton;
    private javax.swing.JScrollPane controllerScrollPane;
    private javax.swing.JTable controllerTable;
    private javax.swing.JCheckBox controllerWithViewmodelCheckBox;
    private javax.swing.JTabbedPane generateTabbedPane;
    private javax.swing.JRadioButton migrationAddFieldRadioButton;
    private javax.swing.JButton migrationAddRowButton;
    private javax.swing.ButtonGroup migrationButtonGroup;
    private javax.swing.JRadioButton migrationCreateTableRadioButton;
    private javax.swing.JRadioButton migrationDeleteFieldRadioButton;
    private javax.swing.JButton migrationDeleteRowsButton;
    private javax.swing.JRadioButton migrationDropTableRadioButton;
    private javax.swing.JLabel migrationFromLabel;
    private javax.swing.JTextField migrationFromTextField;
    private javax.swing.JLabel migrationNameLabel;
    private javax.swing.JTextField migrationNameTextField;
    private javax.swing.JPanel migrationPanel;
    private javax.swing.JRadioButton migrationRenameFieldRadioButton;
    private javax.swing.JRadioButton migrationRenameTableRadioButton;
    private javax.swing.JButton migrationResetAllButton;
    private javax.swing.JButton migrationResetTableButton;
    private javax.swing.JScrollPane migrationScrollPane;
    private javax.swing.JTable migrationTable;
    private javax.swing.JLabel migrationTableNameLabel;
    private javax.swing.JTextField migrationTableNameTextField;
    private javax.swing.JLabel migrationToLabel;
    private javax.swing.JTextField migrationToTextField;
    private javax.swing.JButton modelAddRowButton;
    private javax.swing.ButtonGroup modelAdminScaffoldButtonGroup;
    private javax.swing.JLabel modelCreatedAtLabel;
    private javax.swing.JTextField modelCreatedAtTextField;
    private javax.swing.JCheckBox modelCrudCheckBox;
    private javax.swing.JButton modelDeleteRowsButton;
    private javax.swing.JCheckBox modelMysqlTimestampCheckBox;
    private javax.swing.JLabel modelNameLabel;
    private javax.swing.JTextField modelNameTextField;
    private javax.swing.JCheckBox modelNoMigrationCheckBox;
    private javax.swing.JCheckBox modelNoPropertiesCheckBox;
    private javax.swing.JCheckBox modelNoTimestampCheckBox;
    private javax.swing.JLabel modelOthersLabel;
    private javax.swing.JTextField modelOthersTextField;
    private javax.swing.JPanel modelPanel;
    private javax.swing.JRadioButton modelRadioButton;
    private javax.swing.JButton modelResetAllButton;
    private javax.swing.JButton modelResetTableButton;
    private javax.swing.JScrollPane modelScrollPane1;
    private javax.swing.JCheckBox modelSingularCheckBox;
    private javax.swing.JTable modelTable;
    private javax.swing.JLabel modelUpdatedAtLabel;
    private javax.swing.JTextField modelUpdatedAtTextField;
    private javax.swing.JButton runWithoutClosingButton;
    private javax.swing.JCheckBox runtimeForceCheckBox;
    private javax.swing.JLabel runtimeOthersLabel;
    private javax.swing.JTextField runtimeOthersTextField;
    private javax.swing.JCheckBox runtimeQuietCheckBox;
    private javax.swing.JCheckBox runtimeSkipCheckBox;
    private javax.swing.JCheckBox runtimeSpeakCheckBox;
    private javax.swing.JRadioButton scaffoldRadioButton;
    private javax.swing.JButton taskAddRowButton;
    private javax.swing.JButton taskDeleteRowsButton;
    private javax.swing.JLabel taskNameLabel;
    private javax.swing.JTextField taskNameTextField;
    private javax.swing.JPanel taskPanel;
    private javax.swing.JButton taskResetTableButton;
    private javax.swing.JScrollPane taskScrollPane;
    private javax.swing.JTable taskTable;
    private javax.swing.JButton viewsAddRowButton;
    private javax.swing.JComboBox viewsControllerNameComboBox;
    private javax.swing.JLabel viewsControllerNameLabel;
    private javax.swing.JButton viewsDeleteRowsButton;
    private javax.swing.JPanel viewsPanel;
    private javax.swing.JButton viewsResetTableButton;
    private javax.swing.JScrollPane viewsScrollPane;
    private javax.swing.JTable viewsTable;
    private javax.swing.JCheckBox viewsWithViewmodelCheckBox;
    // End of variables declaration//GEN-END:variables

    /**
     * Get controller parameters.
     *
     * @return parameters
     */
    private List<String> getControllerParameters() {
        List<String> params = new ArrayList<String>();
        // name
        String name = controllerNameTextField.getText().trim();
        if (StringUtils.isEmpty(name)) {
            return params;
        }
        params.add(CONTROLLER_COMMAND);
        params.add(name);

        // actions
        int rowCount = controllerTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String viewName = (String) controllerTable.getValueAt(i, 0);
            if (StringUtils.isEmpty(viewName)) {
                continue;
            }
            viewName = viewName.trim();
            if (!StringUtils.isEmpty(viewName)) {
                params.add(viewName);
            }
        }
        // --extends
        String extendsName = (String) controllerExtendsComboBox.getSelectedItem();
        extendsName = extendsName.trim();
        if (!StringUtils.isEmpty(extendsName)) {
            params.add(controllerExtendsLabel.getText() + extendsName);
        }

        // --with-viewmodel
        if (controllerWithViewmodelCheckBox.isSelected()) {
            params.add(controllerWithViewmodelCheckBox.getText());
        }

        // --crud
        if (controllerCrudCheckBox.isSelected()) {
            params.add(controllerCrudCheckBox.getText());
        }

        // others
        params.addAll(getOthers(controllerOthersTextField));

        return params;
    }

    /**
     * Get model parameters.
     *
     * @return
     */
    private List<String> getModelParameters() {
        List<String> params = new ArrayList<String>();
        // name
        String name = modelNameTextField.getText().trim();
        if (StringUtils.isEmpty(name)) {
            return params;
        }
        String subfolder = (String) adminAndScaffoldComboBox.getSelectedItem();
        if (StringUtils.isEmpty(subfolder)) {
            subfolder = ""; // NOI18N
        } else {
            subfolder = "/" + subfolder; // NOI18N
        }
        if (modelRadioButton.isSelected()) {
            params.add(MODEL_COMMAND + subfolder);
        } else if (adminRadioButton.isSelected()) {
            params.add(ADMIN_COMMAND + subfolder);
        } else if (scaffoldRadioButton.isSelected()) {
            params.add(SCAFFOLD_COMMAND + subfolder);
        }

        params.add(name);
        // table info
        params.addAll(getModelTableParameters(modelTable));

        // --created-at
        String createdAt = modelCreatedAtTextField.getText().trim();
        if (!StringUtils.isEmpty(createdAt)) {
            params.add(modelCreatedAtLabel.getText() + createdAt);
        }

        // --updated-at
        String updatedAt = modelUpdatedAtTextField.getText().trim();
        if (!StringUtils.isEmpty(updatedAt)) {
            params.add(modelUpdatedAtLabel.getText() + updatedAt);
        }

        // --crud
        if (modelCrudCheckBox.isSelected()) {
            params.add(modelCrudCheckBox.getText());
        }

        // --no-migration
        if (modelNoMigrationCheckBox.isSelected()) {
            params.add(modelNoMigrationCheckBox.getText());
        }

        // --no-timestamp
        if (modelNoTimestampCheckBox.isSelected()) {
            params.add(modelNoTimestampCheckBox.getText());
        }

        // --no-properties
        if (modelNoPropertiesCheckBox.isSelected()) {
            params.add(modelNoPropertiesCheckBox.getText());
        }

        // --mysql-timestamp
        if (modelMysqlTimestampCheckBox.isSelected()) {
            params.add(modelMysqlTimestampCheckBox.getText());
        }

        // --singular
        if (modelSingularCheckBox.isSelected()) {
            params.add(modelSingularCheckBox.getText());
        }

        // othres
        params.addAll(getOthers(modelOthersTextField));

        return params;
    }

    /**
     * Get config parameters.
     *
     * @return config parameters
     */
    private List<String> getConfigParameters() {
        List<String> params = new ArrayList<String>();
        String name = configNameTextField.getText().trim();
        if (StringUtils.isEmpty(name)) {
            return params;
        }
        params.add(CONFIG_COMMAND);
        params.add(name);

        // --modules
        String modulesName = configModuleTextField.getText().trim();
        if (!StringUtils.isEmpty(modulesName)) {
            params.add(configModuleLabel.getText() + modulesName);
        }

        // get key:value
        int rowCount = configTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String key = (String) configTable.getValueAt(i, CONFIG_KEY);
            String value = (String) configTable.getValueAt(i, CONFIG_VALUE);
            if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(value)) {
                params.add(key + ":" + value); // NOI18N
            }
        }

        // --overwrite
        if (configOverwriteCheckBox.isSelected()) {
            params.add(configOverwriteCheckBox.getText());
        }

        // others
        params.addAll(getOthers(configOthersTextField));

        return params;
    }

    /**
     * Get views parameters.
     *
     * @return views parameters
     */
    private List<String> getViewsParameters() {
        List<String> params = new ArrayList<String>();
        // controller name
        String name = (String) viewsControllerNameComboBox.getSelectedItem();
        if (StringUtils.isEmpty(name)) {
            return params;
        }
        params.add(VIEWS_COMMAND);
        params.add(name);

        // views
        int rowCount = viewsTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String view = (String) viewsTable.getValueAt(i, 0);
            if (!StringUtils.isEmpty(view)) {
                params.add(view);
            }
        }

        // --with-viewmodel
        if (viewsWithViewmodelCheckBox.isSelected()) {
            params.add(viewsWithViewmodelCheckBox.getText());
        }

        return params;
    }

    /**
     * Get task parameters.
     *
     * @return task parameters
     */
    private List<String> getTaskParameters() {
        List<String> params = new ArrayList<String>();
        // name
        String name = taskNameTextField.getText().trim();
        if (StringUtils.isEmpty(name)) {
            return params;
        }
        params.add(TASK_COMMAND);
        params.add(name);

        // commands
        int rowCount = taskTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String command = (String) taskTable.getValueAt(i, 0);
            if (!StringUtils.isEmpty(command)) {
                params.add(command);
            }
        }

        return params;
    }

    /**
     * Get migration parameters.
     *
     * @return
     */
    private List<String> getMigrationParameters() {
        List<String> params = new ArrayList<String>();
        String subCommand = ""; // NOI18N
        String migrationName = migrationNameTextField.getText().trim();
        String tableName = migrationTableNameTextField.getText().trim();
        String fromName = migrationFromTextField.getText().trim();
        String toName = migrationToTextField.getText().trim();
        if (migrationCreateTableRadioButton.isSelected()) {
            if (!StringUtils.isEmpty(tableName)) {
                subCommand = String.format(MIGRATION_CREATE_TABLE_FORMAT, tableName);
            }
        } else if (migrationRenameTableRadioButton.isSelected()) {
            if (!StringUtils.isEmpty(fromName) && !StringUtils.isEmpty(toName)) {
                subCommand = String.format(MIGRATION_RENAME_TABLE_FORMAT, fromName, toName);
            }
        } else if (migrationDropTableRadioButton.isSelected()) {
            if (!StringUtils.isEmpty(tableName)) {
                subCommand = String.format(MIGRATION_DROP_TABLE_FORMAT, tableName);
            }
        } else if (migrationAddFieldRadioButton.isSelected()) {
            if (!StringUtils.isEmpty(migrationName) && !StringUtils.isEmpty(tableName)) {
                subCommand = String.format(MIGRATION_ADD_FIELD_FORMAT, migrationName, tableName);
            }
        } else if (migrationRenameFieldRadioButton.isSelected()) {
            if (!StringUtils.isEmpty(fromName) && !StringUtils.isEmpty(toName) && !StringUtils.isEmpty(tableName)) {
                subCommand = String.format(MIGRATION_RENAME_FIELD_FORMAT, fromName, toName, tableName);
            }
        } else if (migrationDeleteFieldRadioButton.isSelected()) {
            if (!StringUtils.isEmpty(migrationName) && !StringUtils.isEmpty(tableName)) {
                subCommand = String.format(MIGRATION_DELETE_FIELD_FORMAT, migrationName, tableName);
            }
        } else {
            return params;
        }

        if (StringUtils.isEmpty(subCommand)) {
            return params;
        }

        // add command
        params.add(MIGRATION_COMMAND);

        if (migrationCreateTableRadioButton.isSelected()
                || migrationAddFieldRadioButton.isSelected()
                || migrationDeleteFieldRadioButton.isSelected()) {
            List<String> modelTableParameters = getModelTableParameters(migrationTable);
            if (!modelTableParameters.isEmpty()) {
                // add sub command
                params.add(subCommand);
                // add table info
                params.addAll(modelTableParameters);
            }
        } else {
            // add sub command
            params.add(subCommand);
        }

        return params;
    }

    /**
     * Get model table parameters.
     *
     * @param table
     * @return
     */
    private List<String> getModelTableParameters(JTable table) {
        List<String> params = new ArrayList<String>();
        int rowCount = table.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            StringBuilder sb = new StringBuilder();
            // field name
            String fieldName = (String) table.getValueAt(i, MODEL_FIELD_NAME);
            if (fieldName == null) {
                continue;
            }
            fieldName = fieldName.trim();
            if (StringUtils.isEmpty(fieldName)) {
                continue;
            }
            // field type
            String fieldType = (String) table.getValueAt(i, MODEL_FIELD_TYPE);
            if (StringUtils.isEmpty(fieldType)) {
                continue;
            }
            sb.append(fieldName);
            sb.append(":"); // NOI18N
            sb.append(fieldType);

            // field size
            String fieldSize = (String) table.getValueAt(i, MODEL_FIELD_SIZE);
            if (!StringUtils.isEmpty(fieldSize)) {
                fieldSize = fieldSize.trim();
                if (!StringUtils.isEmpty(fieldSize)) {
                    sb.append("["); // NOI18N
                    sb.append(fieldSize);
                    sb.append("]"); // NOI18N
                }
            }

            // NULL
            Boolean isNULL = (Boolean) table.getValueAt(i, MODEL_FIELD_NULL);
            if (isNULL != null && isNULL.booleanValue()) {
                sb.append(":null"); // NOI18N
            }

            // field default
            String fieldDefault = (String) table.getValueAt(i, MODEL_FIELD_DEFAULT);
            if (!StringUtils.isEmpty(fieldDefault)) {
                fieldDefault = fieldDefault.trim();
                if (!StringUtils.isEmpty(fieldDefault)) {
                    sb.append("["); // NOI18N
                    sb.append(fieldDefault);
                    sb.append("]"); // NOI18N
                }
            }

            // field others
            String fieldOthers = (String) table.getValueAt(i, MODEL_FIELD_OTHERS);
            if (!StringUtils.isEmpty(fieldOthers)) {
                fieldOthers = fieldOthers.trim();
                if (!StringUtils.isEmpty(fieldOthers)) {
                    sb.append(fieldOthers);
                }
            }
            params.add(sb.toString());
        }
        return params;
    }
}
