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
 *//*
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
package org.netbeans.modules.php.fuel.options;

import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.fuel.FuelPhp;
import org.openide.util.NbPreferences;

/**
 *
 * @author junichi11
 */
public class FuelPhpOptions {

    private static final FuelPhpOptions INSTANCE = new FuelPhpOptions();
    private static final String NOTIFY_AUTODETECTION = "notify.autodetection"; // NOI18N
    private static final String PREFERENCES_PATH = "fuelphp"; // NOI18N
    private static final String GIT_BRANCH_NAME = "git.branch.name"; // NOI18N
    private static final String DEFAULT_CONFIG = "default.config"; // NOI18N
    private static final String USE_DEFAULT_CONFIG = "use.default.config"; // NOI18N
    private static final String AVAILABLE_NODES = "available.nodes"; // NOI18N

    private FuelPhpOptions() {
    }

    public static FuelPhpOptions getInstance() {
        return INSTANCE;
    }

    public boolean isNotifyAutodetection() {
        return getPreferences().getBoolean(NOTIFY_AUTODETECTION, true);
    }

    public void setNotifyAutodetection(boolean notify) {
        getPreferences().putBoolean(NOTIFY_AUTODETECTION, notify);
    }

    public String getGitBranchName() {
        return getPreferences().get(GIT_BRANCH_NAME, ""); // NOI18N
    }

    public void setGitBranchName(String name) {
        getPreferences().put(GIT_BRANCH_NAME, name);
    }

    public boolean isDefaultConfig() {
        return getPreferences().getBoolean(USE_DEFAULT_CONFIG, false);
    }

    public void setDefaultConfig(boolean isDefault) {
        getPreferences().putBoolean(USE_DEFAULT_CONFIG, isDefault);
    }

    public String getDefaultConfig() {
        return getPreferences().get(DEFAULT_CONFIG, ""); // NOI18N
    }

    public void setDefaultConfig(String config) {
        getPreferences().put(DEFAULT_CONFIG, config);
    }

    public List<String> getAvailableNodes() {
        String nodes = getPreferences().get(AVAILABLE_NODES, null);
        if (nodes == null) {
            return FuelPhp.CUSTOM_NODES;
        }
        return StringUtils.explode(nodes, "|"); // NOI18N
    }

    public void setAvailableNodes(List<String> nodes) {
        getPreferences().put(AVAILABLE_NODES, StringUtils.implode(nodes, "|")); // NOI18N
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(FuelPhpOptions.class).node(PREFERENCES_PATH);
    }
}
