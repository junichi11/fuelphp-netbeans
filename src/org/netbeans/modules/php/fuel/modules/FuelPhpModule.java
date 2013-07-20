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
import org.openide.filesystems.FileObject;

/**
 *
 * @author junichi11
 */
public class FuelPhpModule {

    private PhpModule phpModule;
    private FuelPhpModuleImpl impl;

    public FuelPhpModule(PhpModule phpModule, FuelPhpModuleImpl impl) {
        this.phpModule = phpModule;
        this.impl = impl;
    }

    public enum DIR_TYPE {

        APP,
        MODULES,
        PACKAGES,
        PUBLIC,
        CORE,
        NONE,
    }

    public enum FILE_TYPE {

        NONE,
        CONTROLLER,
        VIEW,
        VIEW_MODEL,
        MODEL,
        CONFIG,
        TASKS,
        TESTS,
    }

    public FileObject getDirectory(DIR_TYPE dirType) {
        return impl.getDirectory(dirType);
    }

    public FileObject getDirectory(DIR_TYPE dirType, FILE_TYPE fileType, String dirName) {
        return impl.getDirectory(dirType, fileType, dirName);
    }

    public boolean isInApp(FileObject currentFile) {
        return impl.isInApp(currentFile);
    }

    public boolean isInCore(FileObject currentFile) {
        return impl.isInCore(currentFile);
    }

    public boolean isInModule(FileObject currentFile) {
        return impl.isInModules(currentFile);
    }

    public boolean isInPackages(FileObject currentFile) {
        return impl.isInPackages(currentFile);
    }

    public boolean isInPublic(FileObject currentFile) {
        return impl.isInPublic(currentFile);
    }

    public DIR_TYPE getDirType(FileObject current) {
        return impl.getDirType(current);
    }

    public FILE_TYPE getFileType(FileObject current) {
        return impl.getFileType(current);
    }

    public String getModuleName(FileObject fileObject) {
        return impl.getModuelName(fileObject);
    }

    public String getPackageName(FileObject fileObject) {
        return impl.getPackageName(fileObject);
    }

    /**
     * Get source directory path.
     *
     * @return
     */
    public String getSourceDirectoryPath() {
        return impl.getSourceDirectoryPath();
    }

    /**
     * Get file path.
     *
     * @param fileObject
     * @return
     */
    public String getPath(FileObject fileObject) {
        return impl.getPath(fileObject);
    }

    /**
     * Create FuelPhpModule. If php module is empty, Dummy is created.
     *
     * @param phpModule
     * @return FuelPhpModule
     */
    public static FuelPhpModule forPhpModule(PhpModule phpModule) {
        // factory
        FuelPhpModuleFactory factory = FuelPhpModuleFactory.getInstance();
        return factory.create(phpModule);
    }
}
