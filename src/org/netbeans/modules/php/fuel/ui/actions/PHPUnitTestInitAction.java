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
package org.netbeans.modules.php.fuel.ui.actions;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.fuel.FuelPhp;
import org.netbeans.modules.php.fuel.preferences.FuelPhpPreferences;
import org.netbeans.modules.php.fuel.support.ProjectPropertiesSupport;
import org.netbeans.modules.php.fuel.util.FuelUtils;
import org.netbeans.modules.php.spi.framework.actions.BaseAction;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author junichi11
 */
public class PHPUnitTestInitAction extends BaseAction {

    private static final long serialVersionUID = 4707270182888174096L;
    private static final PHPUnitTestInitAction INSTANCE = new PHPUnitTestInitAction();
    private static final Logger LOGGER = Logger.getLogger(PHPUnitTestInitAction.class.getName());
    private static final String BOOTSTRAP_PHPUNIT = "bootstrap_phpunit.php"; // NOI18N
    private static final String BOOTSTRAP_MAKEGOOD = "bootstrap_makegood.php"; // NOI18N
    private Set<String> FOR_MAKEGOOD;
    private static final String NET_BEANS_SUITE = "NetBeansSuite"; // NOI18N
    private static final String NET_BEANS_SUITE_PHP = NET_BEANS_SUITE + ".php"; // NOI18N
    private static final String CONFIG_PATH = "org-netbeans-modules-php-fuel/"; // NOI18N
    private static final String CONFIG_NET_BEANS_SUITE_PHP = CONFIG_PATH + NET_BEANS_SUITE_PHP;
    private static final String PHPUNIT = "phpunit"; // NOI18N
    private static final String PHPUNIT_PHP = PHPUNIT + ".php"; // NOI18N
    private static final Map<String, String> messages = new HashMap<String, String>();
    private static final String SUCCESS_MSG = "success";
    private static final String FAIL_MSG = "fail";
    private static final String BOOTSTRAP = "bootstrap"; // NOI18N
    private static final String UTF8 = "UTF-8"; // NOI18N
    private FileObject coreDirectory;

    static {
    }

    private PHPUnitTestInitAction() {
    }

    public static PHPUnitTestInitAction getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getFullName() {
        return NbBundle.getMessage(PHPUnitTestInitAction.class, "LBL_FuelPhpAction", getPureName()); // NOI18N
    }

    @Override
    protected String getPureName() {
        return NbBundle.getMessage(PHPUnitTestInitAction.class, "LBL_PHPUnitTestInit"); // NOI18N
    }

    @Override
    protected void actionPerformed(PhpModule phpModule) {
        // via shortcut
        if (!FuelUtils.isFuelPHP(phpModule)) {
            // do nothing
            return;
        }
        coreDirectory = FuelUtils.getCoreDirectory(phpModule);
        if (coreDirectory == null) {
            return;
        }
        String fuel = FuelPhpPreferences.getFuelName(phpModule);
        FOR_MAKEGOOD = new HashSet<String>();
        FOR_MAKEGOOD.add("$_SERVER['doc_root'] = '../../';"); // NOI18N
        FOR_MAKEGOOD.add("$_SERVER['app_path'] = '" + fuel + "/app';"); // NOI18N
        FOR_MAKEGOOD.add("$_SERVER['vendor_path'] = '" + fuel + "/vendor';"); // NOI18N
        FOR_MAKEGOOD.add("$_SERVER['core_path'] = '" + fuel + "/core';"); // NOI18N
        FOR_MAKEGOOD.add("$_SERVER['package_path'] = '" + fuel + "/packages';"); // NOI18N

        // create files
        createBootstrap();
        createNetBeansSuite(phpModule);
        createScript(phpModule);

        setPhpProjectProperties(phpModule);
        StringBuilder notifyMessage = new StringBuilder();
        for (String key : messages.keySet()) {
            notifyMessage.append(key);
            notifyMessage.append(":"); // NOI18N
            notifyMessage.append(messages.get(key));
            notifyMessage.append(" \n"); // NOI18N
        }
        NotificationDisplayer.getDefault().notify(getFullName(), ImageUtilities.loadImageIcon(FuelPhp.FUEL_ICON_16, true), notifyMessage.toString(), null);
    }

    /**
     * Create bootstrap_makegood.php file
     *
     * @return void
     */
    private void createBootstrap() {
        FileObject bootstrapPhpunit = coreDirectory.getFileObject(BOOTSTRAP_PHPUNIT);
        FileObject bootstrapMakegood = coreDirectory.getFileObject(BOOTSTRAP_MAKEGOOD);
        if (bootstrapPhpunit != null) {
            OutputStream outputStream = null;
            try {
                if (bootstrapMakegood == null) {
                    outputStream = coreDirectory.createAndOpen(BOOTSTRAP_MAKEGOOD);
                } else {
                    outputStream = bootstrapMakegood.getOutputStream();
                }
                List<String> lines = bootstrapPhpunit.asLines();
                PrintWriter pw = new PrintWriter(outputStream);
                for (String line : lines) {
                    if (line.startsWith("$app_path")) { // NOI18N
                        for (String str : FOR_MAKEGOOD) {
                            pw.println(str);
                        }
                    }
                    pw.println(line);
                }
                pw.close();
                messages.put(BOOTSTRAP, SUCCESS_MSG);
            } catch (IOException ex) {
                messages.put(BOOTSTRAP, FAIL_MSG);
            }
        }
    }

    /**
     * Create NetBeansSuite.php
     *
     * @param phpModule
     */
    private void createNetBeansSuite(PhpModule phpModule) {
        FileObject nbproject = FuelUtils.getNbproject(phpModule);
        FileObject nbSuite = nbproject.getFileObject(NET_BEANS_SUITE_PHP);
        if (nbSuite != null) {
            try {
                nbSuite.delete();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        FileObject suite = FileUtil.getConfigFile(CONFIG_NET_BEANS_SUITE_PHP);
        try {
            suite.copy(nbproject, NET_BEANS_SUITE, "php"); // NOI18N
            messages.put(NET_BEANS_SUITE, SUCCESS_MSG);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            messages.put(NET_BEANS_SUITE, FAIL_MSG);
        }
    }

    /**
     * Create Script file
     *
     * @param phpModule
     */
    private void createScript(PhpModule phpModule) {
        FileObject nbproject = FuelUtils.getNbproject(phpModule);
        String scriptFileName = getScriptFileName();
        String phpUnit = getPHPUnitPath();
        if (phpUnit == null || phpUnit.isEmpty()) {
            messages.put(PHPUNIT, FAIL_MSG + "(isn't set phpunit option)");
            return;
        }
        FileObject phpUnitScript = nbproject.getFileObject(scriptFileName);
        FileObject script = FileUtil.getConfigFile(CONFIG_PATH + scriptFileName);

        // over write phpunit script (phpunit.php)
        try {
            String format = String.format(script.asText(UTF8), phpUnit);
            PrintWriter pw;
            if (phpUnitScript == null) {
                pw = new PrintWriter(new OutputStreamWriter(nbproject.createAndOpen(scriptFileName), UTF8));
            } else {
                pw = new PrintWriter(new OutputStreamWriter(phpUnitScript.getOutputStream(), UTF8));
            }
            try {
                pw.print(format);
            } finally {
                pw.close();
            }
            messages.put(PHPUNIT, SUCCESS_MSG);
        } catch (IOException ex) {
            messages.put(PHPUNIT, FAIL_MSG);
        }
        FileObject createdFile = nbproject.getFileObject(scriptFileName);
        FileUtil.toFile(createdFile).setExecutable(true);
    }

    private String getScriptFileName() {
        return PHPUNIT_PHP;
    }

    private String getPHPUnitPath() {
        Preferences preference = NbPreferences.root().node("/org/netbeans/modules/php/phpunit/phpunit"); // NOI18N
        return preference.get("phpUnit.path", null); // NOI18N
    }

    /**
     * Set PHP Project Properties. Set bootstrap, script
     *
     * @param phpModule
     */
    private void setPhpProjectProperties(PhpModule phpModule) {
        // set bootstrap and script
        String bootstrapPath = coreDirectory.getPath() + "/bootstrap_makegood.php"; // NOI18N
        String scriptPath = FuelUtils.getNbproject(phpModule).getPath() + "/" + PHPUNIT_PHP; // NOI18N
        ProjectPropertiesSupport.setPHPUnit(phpModule, bootstrapPath, scriptPath);
    }
}
