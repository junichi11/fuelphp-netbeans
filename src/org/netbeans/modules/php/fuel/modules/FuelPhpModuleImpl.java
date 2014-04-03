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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule.DIR_TYPE;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule.FILE_TYPE;
import org.netbeans.modules.php.fuel.preferences.FuelPhpPreferences;
import org.netbeans.modules.php.fuel.ui.FuelPhpStatusLineElement;
import org.openide.awt.StatusLineElementProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author junichi11
 */
public abstract class FuelPhpModuleImpl {

    protected PhpModule phpModule;
    private FuelPhpVersion version;
    private final FileObject fuelClass;
    private static final Logger LOGGER = Logger.getLogger(FuelPhpModuleImpl.class.getName());

    @NbBundle.Messages("FuelPhpModuleImpl.fuelclass.notFound=Not found: core/classes/fuel.php")
    public FuelPhpModuleImpl(PhpModule phpModule) {
        this.phpModule = phpModule;

        // version
        fuelClass = getFuelClass();
        String versionNumber = null;
        if (fuelClass != null) {
            // add change listener #26
            FuelFileChangeAdapter fuelFileChangeAdapter = new FuelFileChangeAdapter();
            fuelClass.addFileChangeListener(fuelFileChangeAdapter);
            versionNumber = getVersionNumber(fuelClass);
        } else {
            LOGGER.log(Level.WARNING, Bundle.FuelPhpModuleImpl_fuelclass_notFound());
        }
        this.version = new FuelPhpVersion(versionNumber);
    }

    public FuelPhpVersion getVersion() {
        return version;
    }

    /**
     * Get specific directory for directory type.
     *
     * @param dirType
     * @return directory
     */
    public abstract FileObject getDirectory(FuelPhpModule.DIR_TYPE dirType);

    /**
     * Get directory for directory type, file type, and module, package name.
     *
     * @param dirType directory type
     * @param fileType file type
     * @param dirName module or package name
     * @return directory
     */
    public abstract FileObject getDirectory(FuelPhpModule.DIR_TYPE dirType, FuelPhpModule.FILE_TYPE fileType, String dirName);

    /**
     * Get specific directory for current file.
     *
     * @param currentFile target file
     * @param fileType file type
     * @return directory
     */
    public abstract FileObject getDirectory(FileObject currentFile, FuelPhpModule.FILE_TYPE fileType);

    /**
     * Check whether target file is in app directory.
     *
     * @param currentFile target file
     * @return true if the file is in app, otherwise false.
     */
    public abstract boolean isInApp(FileObject currentFile);

    /**
     * Check whether target file is in core directory.
     *
     * @param currentFile target file
     * @return true if the file is in core, otherwise false.
     */
    public abstract boolean isInCore(FileObject currentFile);

    /**
     * Check whether target file is in moodules directory.
     *
     * @param currentFile target file
     * @return true if the file is in modules, otherwise false.
     */
    public abstract boolean isInModules(FileObject currentFile);

    /**
     * Check whether target file is in packages directory.
     *
     * @param currentFile target file
     * @return true if the file is in packages, otherwise false.
     */
    public abstract boolean isInPackages(FileObject currentFile);

    /**
     * Check whether target file is in public directory.
     *
     * @param currentFile target file
     * @return true if the file is in public, otherwise false.
     */
    public abstract boolean isInPublic(FileObject currentFile);

    /**
     * Get DIR_TYPE for target file.
     *
     * @param currentFile target file
     * @return DIR_TYPE
     */
    public abstract DIR_TYPE getDirType(FileObject currentFile);

    /**
     * Get FILE_TYPE for target file.
     *
     * @param currentFile target file
     * @return FILE_TYPE
     */
    public abstract FILE_TYPE getFileType(FileObject currentFile);

    /**
     * Get fuel directory name (default is fuel).
     *
     * @return fuel directory name.
     */
    public String getFuelDirectoryName() {
        if (phpModule == null) {
            return ""; // NOI18N
        }
        return FuelPhpPreferences.getFuelName(phpModule);
    }

    /**
     * Get module name.
     *
     * @param fileObject
     * @return module name if module exists, otherwise empty string.
     */
    public String getModuleName(FileObject fileObject) {
        if (!isInModules(fileObject)) {
            return ""; // NOI18N
        }
        String path = getPath(fileObject);
        if (path.isEmpty()) {
            return ""; // NOI18N
        }
        path = path.replaceAll(".+/modules/", ""); // NOI18N
        int indexOfSlash = path.indexOf("/"); // NOI18N
        if (indexOfSlash == -1) {
            return ""; // NOI18N
        }
        return path.substring(0, indexOfSlash); // NOI18N
    }

    /**
     * Get package name.
     *
     * @param fileObject
     * @return module name if package exists, otherwise empty string.
     */
    public String getPackageName(FileObject fileObject) {
        if (!isInPackages(fileObject)) {
            return ""; // NOI18N
        }
        String path = getPath(fileObject);
        if (path.isEmpty()) {
            return ""; // NOI18N
        }
        path = path.replaceAll(".+/packages/", ""); // NOI18N
        int indexOfSlash = path.indexOf("/"); // NOI18N
        if (indexOfSlash == -1) {
            return ""; // NOI18N
        }
        return path.substring(0, indexOfSlash); // NOI18N
    }

    /**
     * Get source directory path.
     *
     * @return
     */
    public String getSourceDirectoryPath() {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            return ""; // NOI18N
        }
        return sourceDirectory.getPath();
    }

    /**
     * Get file path.
     *
     * @param currentFile
     * @return
     */
    public String getPath(FileObject currentFile) {
        if (currentFile == null) {
            return ""; // NOI18N
        }
        return currentFile.getPath();
    }

    /**
     * Create a new file.
     *
     * @param baseDirectory base directory
     * @param path path from base directory
     * @return true if a file is created, otherwise false.
     */
    public boolean createNewFile(FileObject baseDirectory, String path) throws IOException {
        if (baseDirectory == null || StringUtils.isEmpty(path)) {
            return false;
        }
        File baseDir = FileUtil.toFile(baseDirectory);
        File targetFile = new File(baseDir, path);
        if (targetFile.exists()) {
            return false;
        }
        File parentFile = targetFile.getParentFile();
        if (!parentFile.exists()) {
            // mkdirs
            parentFile.mkdirs();
        }
        return targetFile.createNewFile();
    }

    private FileObject getFuelClass() {
        // get core
        FileObject core = getDirectory(DIR_TYPE.CORE);
        if (core == null) {
            return null;
        }

        // get fuel class
        return core.getFileObject("classes/fuel.php"); // NOI18N
    }

    /**
     * Get version number.
     *
     * @param fuelClass fuel.php file
     * @return version number, null if it doesn't exit.
     */
    private String getVersionNumber(FileObject fuelClass) {
        try {
            List<String> lines = fuelClass.asLines("UTF-8"); // NOI18N
            Pattern pattern = Pattern.compile("\tconst VERSION = '([a-zA-Z0-9.-]+)';"); // NOI18N
            for (String line : lines) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private class FuelFileChangeAdapter extends FileChangeAdapter {

        public FuelFileChangeAdapter() {
        }

        @Override
        public void fileChanged(FileEvent fe) {
            FileObject fuel = fe.getFile();
            fireChange(fuel);
        }

        private void fireChange(FileObject fuel) {
            if (fuel == null) {
                return;
            }
            String version = getVersionNumber(fuel);
            FuelPhpModuleImpl.this.version = new FuelPhpVersion(version);
            Collection<? extends StatusLineElementProvider> elements = Lookup.getDefault().lookupAll(StatusLineElementProvider.class);
            for (StatusLineElementProvider element : elements) {
                if (element instanceof FuelPhpStatusLineElement) {
                    FuelPhpStatusLineElement statusbar = (FuelPhpStatusLineElement) element;
                    statusbar.reset();
                    break;
                }
            }
        }

    }
}
