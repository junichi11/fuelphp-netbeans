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
package org.netbeans.modules.php.fuel;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.composer.api.Composer;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule;
import org.netbeans.modules.php.fuel.modules.FuelPhpVersion;
import org.netbeans.modules.php.fuel.options.FuelPhpOptions;
import org.netbeans.modules.php.fuel.preferences.FuelPhpPreferences;
import org.netbeans.modules.php.fuel.ui.NewProjectConfigurationPanel;
import org.netbeans.modules.php.fuel.util.FuelUtils;
import org.netbeans.modules.php.fuel.util.FuelZipEntryFilter;
import org.netbeans.modules.php.fuel.util.UrlZipper;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender.ExtendingException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author junichi11
 */
public class FuelPhpModuleExtender extends PhpModuleExtender {

    private static final String ADD_COMMAND = "add";
    private static final String BRANCH_MASTER_MERGE = "branch.master.merge";
    private static final String BRANCH_MASTER_REMOTE = "branch.master.remote";
    private static final String CONFIG_COMMAND = "config";
    private static final String CONFIG_PHP = "fuel/app/config/config.php"; // NOI18N
    private static final String GIT = "git";
    private static final String GIT_DIR = "--git-dir=";
    private static final String GIT_GITHUB_COM_FUEL_FUEL_GIT = "git://github.com/fuel/fuel.git";
    private static final String GIT_REPO = "/.git";
    private static final String INIT_COMMAND = "init";
    private static final String ORIGIN = "origin";
    private static final String PULL_COMMAND = "pull";
    private static final String REFS_HEADS = "refs/heads/";
    private static final String REMOTE_COMMAND = "remote";
    private static final String WORK_TREE = "--work-tree=";
    private NewProjectConfigurationPanel panel = null;
    private static final Logger LOGGER = Logger.getLogger(FuelPhpModuleExtender.class.getName());

    @Override
    public void addChangeListener(ChangeListener cl) {
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
    }

    @Override
    public JComponent getComponent() {
        return getPanel();
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public boolean isValid() {
        return getPanel().getErrorMessage() == null;
    }

    @Override
    public String getErrorMessage() {
        return getPanel().getErrorMessage();
    }

    @Override
    public String getWarningMessage() {
        return null;
    }

    @Override
    public Set<FileObject> extend(PhpModule pm) throws ExtendingException {
        FileObject sourceDirectory = pm.getSourceDirectory();
        if (sourceDirectory == null) {
            return Collections.emptySet();
        }
        if (getPanel().getUnzipRadioButton().isSelected()) {

            Map<String, String> downloadsMap = getPanel().getDownloadsMap();
            String url = downloadsMap.get(getPanel().getVersionList().getSelectedValue().toString());
            UrlZipper zipper = new UrlZipper(url, sourceDirectory, new FuelZipEntryFilter());
            try {
                zipper.unzip();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            try {
                String repoPath = sourceDirectory.getPath();
                String gitDir = GIT_DIR + repoPath + GIT_REPO;
                String workTree = WORK_TREE + repoPath;
                String branchName = FuelPhpOptions.getInstance().getGitBranchName();

                String[] initCommand = {GIT, INIT_COMMAND, repoPath};
                String[] remoteAddCommand = {GIT, gitDir, workTree, REMOTE_COMMAND, ADD_COMMAND, ORIGIN, GIT_GITHUB_COM_FUEL_FUEL_GIT};
                String[] configMergeCommand = {GIT, gitDir, workTree, CONFIG_COMMAND, BRANCH_MASTER_MERGE, REFS_HEADS + branchName};
                String[] configRemoteCommand = {GIT, gitDir, workTree, CONFIG_COMMAND, BRANCH_MASTER_REMOTE, ORIGIN};
                String[] pullCommand = {GIT, gitDir, workTree, PULL_COMMAND};
                String[] submodulesCommand = {"/bin/bash", "-c", "cd " + repoPath + ";git submodule update --init --recursive"}; // NOI18N

                // Run git Command
                getPanel().setGitCommandLabel(INIT_COMMAND);
                Process initProcess = Runtime.getRuntime().exec(initCommand);
                initProcess.waitFor();
                getPanel().setGitCommandLabel(REMOTE_COMMAND + " " + ADD_COMMAND);
                Process remoteProcess = Runtime.getRuntime().exec(remoteAddCommand);
                remoteProcess.waitFor();
                getPanel().setGitCommandLabel(CONFIG_COMMAND + " " + BRANCH_MASTER_MERGE);
                Process configMergeProcess = Runtime.getRuntime().exec(configMergeCommand);
                configMergeProcess.waitFor();
                getPanel().setGitCommandLabel(CONFIG_COMMAND + " " + BRANCH_MASTER_REMOTE);
                Process configRemoteProcess = Runtime.getRuntime().exec(configRemoteCommand);
                configRemoteProcess.waitFor();
                getPanel().setGitCommandLabel(PULL_COMMAND);
                Process pullProcess = Runtime.getRuntime().exec(pullCommand);
                pullProcess.waitFor();
                getPanel().setGitCommandLabel("submodule update --init --recursive"); // NOI18N
                Process submoduleProcess = Runtime.getRuntime().exec(submodulesCommand);
                submoduleProcess.waitFor();
                getPanel().setGitCommandLabel("Complete"); // NOI18N

            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        FuelPhpPreferences.setEnabled(pm, true);

        // composer update
        update(pm);

        // set open file
        Set<FileObject> files = new HashSet<>();
        FileObject config = sourceDirectory.getFileObject(CONFIG_PHP);

        // use default config
        setDefaultConfig(config);

        if (config != null) {
            files.add(config);
        }
        try {
            // add a file to nbproject directory for auto completion
            FuelUtils.createAutoCompletionFile(pm, true);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Can't create a file for auto completion", ex);
        }

        return files;
    }

    public NewProjectConfigurationPanel getPanel() {
        if (panel == null) {
            panel = new NewProjectConfigurationPanel();
        }
        return panel;
    }

    /**
     * Set default config to config.php.
     *
     * @param config config.php
     */
    private void setDefaultConfig(FileObject config) {
        FuelPhpOptions options = FuelPhpOptions.getInstance();

        if (options.isDefaultConfig()) {
            try {
                // write
                OutputStream outputStream = config.getOutputStream();
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true); // NOI18N
                try {
                    pw.write(options.getDefaultConfig());
                } finally {
                    pw.close();
                }
            } catch (FileAlreadyLockedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Update with Composer.
     *
     * @param phpModule
     */
    @NbBundle.Messages("FuelPhpModuleExtender.not.found.composer=Can't use composer. Please set composer path to the Options and run composer update manually.")
    private void update(PhpModule phpModule) {
        FuelPhpModule fuelModule = FuelPhpModule.forPhpModule(phpModule);
        FuelPhpVersion version = fuelModule.getVersion();

        // version >= 1.6
        if (version.getMajor() >= 1 && version.getMinor() >= 6) {
            try {
                Composer composer = Composer.getDefault();
                composer.update(phpModule);
            } catch (InvalidPhpExecutableException ex) {
                NotifyDescriptor.Message message = new NotifyDescriptor.Message(Bundle.FuelPhpModuleExtender_not_found_composer(), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(message);
                LOGGER.log(Level.WARNING, Bundle.FuelPhpModuleExtender_not_found_composer());
            }
        }
    }

}
