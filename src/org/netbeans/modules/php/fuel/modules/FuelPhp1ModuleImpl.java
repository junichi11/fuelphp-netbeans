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
package org.netbeans.modules.php.fuel.modules;

import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule.DIR_TYPE;
import static org.netbeans.modules.php.fuel.modules.FuelPhpModule.DIR_TYPE.MODULES;
import static org.netbeans.modules.php.fuel.modules.FuelPhpModule.DIR_TYPE.PACKAGES;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule.FILE_TYPE;
import org.openide.filesystems.FileObject;

public class FuelPhp1ModuleImpl extends FuelPhpModuleImpl {

    public FuelPhp1ModuleImpl(PhpModule phpModule) {
        super(phpModule);
    }

    @Override
    public FileObject getDirectory(DIR_TYPE dirType) {
        if (phpModule == null || dirType == null) {
            return null;
        }
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            return null;
        }

        String fuelName = getFuelDirectoryName();
        String path = ""; // NOI18N
        switch (dirType) {
            case APP:
                path = fuelName + "/app"; // NOI18N
                break;
            case MODULES:
                path = fuelName + "/app/modules"; // NOI18N
                break;
            case CORE:
                path = fuelName + "/core"; // NOI18N
                break;
            case PACKAGES:
                path = fuelName + "/packages"; // NOI18N
                break;
            case PUBLIC:
                path = "public"; // NOI18N
                break;
            case NONE:
                // do nothing
                break;
            default:
                // do nothing
                break;
        }
        if (path.isEmpty()) {
            return null;
        }
        return sourceDirectory.getFileObject(path);
    }

    @Override
    public FileObject getDirectory(DIR_TYPE dirType, FILE_TYPE fileType, String dirName) {
        FileObject baseDirectory = getDirectory(dirType);
        if (baseDirectory == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        switch (dirType) {
            case MODULES: // no break
            case PACKAGES:
                if (StringUtils.isEmpty(dirName)) {
                    return null;
                }
                sb.append(dirName);
                sb.append("/"); // NOI18N
                break;
            default:
                // do nothing
                break;
        }

        if (fileType == null) {
            return baseDirectory.getFileObject(sb.toString());
        }

        switch (fileType) {
            case CONTROLLER:
                sb.append("classes/controller"); // NOI18N
                break;
            case MODEL:
                sb.append("classes/model"); // NOI18N
                break;
            case VIEW:
                sb.append("views"); // NOI18N
                break;
            case VIEW_MODEL:
                sb.append("classes/view"); // NOI18N
                break;
            case CONFIG:
                sb.append("config"); // NOI18N
                break;
            case TASKS:
                sb.append("tasks"); // NOI18N
                break;
            case TESTS:
                sb.append("tests"); // NOI18N
                break;
            default:
                // do nothinsg
                break;
        }

        return baseDirectory.getFileObject(sb.toString());
    }

    @Override
    public FileObject getDirectory(FileObject currentFile, FILE_TYPE fileType) {
        DIR_TYPE dirType = getDirType(currentFile);
        String dirName = ""; // NOI18N
        switch (dirType) {
            case MODULES:
                dirName = getModuleName(currentFile);
                break;
            case PACKAGES:
                dirName = getPackageName(currentFile);
                break;
            default:
                // do nothing
                break;
        }
        return getDirectory(dirType, fileType, dirName);
    }

    @Override
    public boolean isInApp(FileObject currentFile) {
        return isIn(DIR_TYPE.APP, currentFile);
    }

    @Override
    public boolean isInCore(FileObject currentFile) {
        return isIn(DIR_TYPE.CORE, currentFile);
    }

    @Override
    public boolean isInModules(FileObject currentFile) {
        return isIn(DIR_TYPE.MODULES, currentFile);
    }

    @Override
    public boolean isInPackages(FileObject currentFile) {
        return isIn(DIR_TYPE.PACKAGES, currentFile);
    }

    @Override
    public boolean isInPublic(FileObject currentFile) {
        return isIn(DIR_TYPE.PUBLIC, currentFile);
    }

    private boolean isIn(DIR_TYPE dirType, FileObject current) {
        DIR_TYPE currentDirType = getDirType(current);
        return dirType == currentDirType;
    }

    @Override
    public DIR_TYPE getDirType(FileObject currentFile) {
        String path = getPath(currentFile);
        String sourcePath = getSourceDirectoryPath();
        if (StringUtils.isEmpty(path) || StringUtils.isEmpty(sourcePath)) {
            return DIR_TYPE.NONE;
        }
        String fuelName = getFuelDirectoryName();
        path = path.replace(sourcePath, ""); // NOI18N
        if (path.contains(fuelName + "/app/modules/")) { // NOI18N
            return DIR_TYPE.MODULES;
        } else if (path.contains(fuelName + "/app/")) { // NOI18N
            return DIR_TYPE.APP;
        } else if (path.contains(fuelName + "/core/")) { // NOI18N
            return DIR_TYPE.CORE;
        } else if (path.contains(fuelName + "/packages/")) { // NOI18N
            return DIR_TYPE.PACKAGES;
        } else if (path.contains("/public/")) { // NOI18N
            return DIR_TYPE.PUBLIC;
        }

        return DIR_TYPE.NONE;
    }

    @Override
    public FILE_TYPE getFileType(FileObject currentFile) {
        String path = getPath(currentFile);
        String sourcePath = getSourceDirectoryPath();
        if (StringUtils.isEmpty(path) || StringUtils.isEmpty(sourcePath) || !FileUtils.isPhpFile(currentFile)) {
            return FILE_TYPE.NONE;
        }
        path = path.replace(sourcePath, ""); // NOI18N
        if (path.contains("classes/controller/")) { // NOI18N
            return FILE_TYPE.CONTROLLER;
        } else if (path.contains("classes/view/")) { // NOI18N
            return FILE_TYPE.VIEW_MODEL;
        } else if (path.contains("classes/model/")) { // NOI18N
            return FILE_TYPE.MODEL;
        } else if (path.contains("/views/")) { // NOI18N
            return FILE_TYPE.VIEW;
        } else if (path.contains("/config/")) { // NOI18N
            return FILE_TYPE.CONFIG;
        } else if (path.contains("/tasks/")) { // NOI18N
            return FILE_TYPE.TASKS;
        } else if (path.contains("/tests/")) { // NOI18N
            return FILE_TYPE.TESTS;
        }

        return FILE_TYPE.NONE;
    }
}
