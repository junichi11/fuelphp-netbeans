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
package org.netbeans.modules.php.fuel.preferences;

import java.util.prefs.Preferences;
import org.netbeans.modules.php.api.phpmodule.PhpModule;

/**
 *
 * @author junichi11
 */
public class FuelPhpPreferences {

    private static final String ENABLED = "enabled"; // NOI18N
    private static final String FUEL_NAME = "fuel-name"; // NOI18N
    private static final String USE_TEST_CASE = "use-test-case"; // NOI18N
    private static final String IGNORE_MVC_NODE = "ignore-mvc-node"; // NOI18N
    private static final String DEFAULT_FUEL_NAME = "fuel"; // NOI18N
    private static final String TEST_CASE_PREFIX = "test-case-prefix"; // NOI18N
    private static final String TEST_CASE_SUFFIX = "test-case-suffix"; // NOI18N
    private static final String TEST_GROUP_ANNOTATION = "test-group-annotation"; // NOI18N
    private static final String AUTO_CREATE_FILE = "auto-create-file"; // NOI18N

    public static boolean isEnabled(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(ENABLED, false);
    }

    public static void setEnabled(PhpModule phpModule, boolean isEnabled) {
        getPreferences(phpModule).putBoolean(ENABLED, isEnabled);
    }

    public static String getFuelName(PhpModule phpModule) {
        String fuelName = getPreferences(phpModule).get(FUEL_NAME, ""); // NOI18N
        if (fuelName == null || fuelName.isEmpty()) {
            fuelName = DEFAULT_FUEL_NAME;
        }
        return fuelName;
    }

    public static void setFuelName(PhpModule phpModule, String fuelName) {
        getPreferences(phpModule).put(FUEL_NAME, fuelName);
    }

    public static boolean useTestCaseMethod(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(USE_TEST_CASE, false);
    }

    public static void setUseTestCaseMethod(PhpModule phpModule, boolean use) {
        getPreferences(phpModule).putBoolean(USE_TEST_CASE, use);
    }

    public static boolean ignoreMVCNode(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(IGNORE_MVC_NODE, false);
    }

    public static void setIgnoreMVCNode(PhpModule phpModule, boolean ignore) {
        getPreferences(phpModule).putBoolean(IGNORE_MVC_NODE, ignore);
    }

    public static String getTestCasePrefix(PhpModule phpModule) {
        return getPreferences(phpModule).get(TEST_CASE_PREFIX, "Test_");
    }

    public static void setTestCasePrefix(PhpModule phpModule, String prefix) {
        getPreferences(phpModule).put(TEST_CASE_PREFIX, prefix);
    }

    public static String getTestCaseSuffix(PhpModule phpModule) {
        return getPreferences(phpModule).get(TEST_CASE_SUFFIX, "");
    }

    public static void setTestCaseSuffix(PhpModule phpModule, String suffix) {
        getPreferences(phpModule).put(TEST_CASE_SUFFIX, suffix);
    }

    public static String getTestGroupAnnotation(PhpModule phpModule) {
        return getPreferences(phpModule).get(TEST_GROUP_ANNOTATION, "App"); // NOI18N
    }

    public static void setTestGroupAnnotation(PhpModule phpModule, String name) {
        getPreferences(phpModule).put(TEST_GROUP_ANNOTATION, name);
    }

    public static boolean useAutoCreateFile(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(AUTO_CREATE_FILE, false);
    }

    public static void setAutoCreateFile(PhpModule phpModule, boolean useAutoCreateFile) {
        getPreferences(phpModule).putBoolean(AUTO_CREATE_FILE, useAutoCreateFile);
    }

    private static Preferences getPreferences(PhpModule phpModule) {
        return phpModule.getPreferences(FuelPhpPreferences.class, true);
    }
}
