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

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.fuel.preferences.FuelPhpPreferences;
import org.netbeans.modules.php.fuel.util.FuelUtils;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleIgnoredFilesExtender;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author junichi11
 */
public class FuelPhpIgnoredFilesExtender extends PhpModuleIgnoredFilesExtender {

    private File docs;
    private File controller;
    private File model;
    private File views;
    private File modules;

    public FuelPhpIgnoredFilesExtender(PhpModule pm) {
        assert pm != null;
        docs = new File(FileUtil.toFile(pm.getProjectDirectory()), "docs"); //NOI18N
        // add settings whether MVC direcotries is ignored
        if (FuelPhpPreferences.ignoreMVCNode(pm)) {
            controller = FileUtil.toFile(FuelUtils.getControllerDirectory(pm));
            model = FileUtil.toFile(FuelUtils.getModelDirectory(pm));
            views = FileUtil.toFile(FuelUtils.getViewsDirectory(pm));
            modules = FileUtil.toFile(FuelUtils.getModulesDirectory(pm));
        }
    }

    @Override
    public Set<File> getIgnoredFiles() {
        Set<File> ignoreFiles = new HashSet<File>();
        ignoreFiles.add(docs);
        if (controller != null) {
            ignoreFiles.add(controller);
        }
        if (model != null) {
            ignoreFiles.add(model);
        }
        if (views != null) {
            ignoreFiles.add(views);
        }
        if (modules != null) {
            ignoreFiles.add(modules);
        }
        return ignoreFiles;
    }
}
