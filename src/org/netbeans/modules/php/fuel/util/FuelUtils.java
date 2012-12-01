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
package org.netbeans.modules.php.fuel.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONException;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.fuel.preferences.FuelPhpPreferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author junichi11
 */
public final class FuelUtils {

    private static final String ACTION_PREFIX = "action_"; // NOI18N
    private static final String CONTROLLER_PREFIX = "Controller_"; // NOI18N
    private static final int DEFAULT_OFFSET = 0;
    private static final String EXT_PHP = "php"; // NOI18N
    private static final String FUEL_APP_CLASSES_CONTROLLER_DIR = "%s/app/classes/controller"; // NOI18N
    private static final String FUEL_APP_CLASSES_VIEW_DIR = "%s/app/classes/view";
    private static final String FUEL_APP_VIEWS_DIR = "%s/app/views"; // NOI18N
    private static final String FUEL_AUTOCOMPLETION_PHP = "org-netbeans-modules-php-fuel/fuel_autocompletion.php"; // NOI18N
    private static final String NBPROJECT_DIR_NAME = "nbproject"; // NOI18N
    private static final String UTF8 = "UTF-8"; // NOI18N

    public static JSONArray getJsonArray(URL url) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), UTF8)); // NOI18N
            StringBuilder contents = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                contents.append(str);
            }

            JSONArray jsonArray = new JSONArray(contents.toString());
            return jsonArray;
        } catch (JSONException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static void getAutoCompletionFile(FileObject projectDirectory) {
        if (projectDirectory == null) {
            return;
        }
        FileObject nbprojectDirectory = projectDirectory.getFileObject(NBPROJECT_DIR_NAME);
        FileObject autoCompletionFile = FileUtil.getConfigFile(FUEL_AUTOCOMPLETION_PHP);

        if (nbprojectDirectory.getFileObject(autoCompletionFile.getNameExt()) != null) {
            // already exists
            return;
        }

        if (nbprojectDirectory != null && autoCompletionFile != null) {
            try {
                autoCompletionFile.copy(nbprojectDirectory, autoCompletionFile.getName(), autoCompletionFile.getExt());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Check controller file
     *
     * @param controller controller FileObject
     * @return if prefix is "Controller_", return true.
     */
    public static boolean isController(FileObject controller) {
        if (controller == null) {
            return false;
        }
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        Collection<PhpClass> phpClasses = editorSupport.getClasses(controller);
        if (phpClasses.size() != 1) {
            return false;
        }

        PhpClass phpClass = null;
        for (PhpClass pc : phpClasses) {
            phpClass = pc;
        }

        if (phpClass == null) {
            return false;
        }

        return phpClass.getName().startsWith(CONTROLLER_PREFIX);
    }

    /**
     * Check view file
     *
     * @param view view FileObject
     * @return if the view file is in views directory, return true.
     */
    public static boolean isView(FileObject view) {
        if (view == null || !EXT_PHP.equals(view.getExt())) {
            return false;
        }
        FileObject viewsDirectory = getViewsDirectory(view);

        String viewsDirectoryPath = viewsDirectory.getPath();
        String viewPath = view.getPath();

        return viewPath.startsWith(viewsDirectoryPath);
    }

    /**
     * Check view model file
     *
     * @param viewModel view model FileObject
     * @return if the view model file is in view model directory, return true.
     */
    public static boolean isViewModel(FileObject viewModel) {
        if (viewModel == null || !EXT_PHP.equals(viewModel.getExt())) {
            return false;
        }
        FileObject viewModelDirectory = getViewModelDirectory(viewModel);

        String viewModelDirectoryPath = viewModelDirectory.getPath();
        String viewModelPath = viewModel.getPath();

        return viewModelPath.startsWith(viewModelDirectoryPath);
    }

    /**
     * Get views directory (fuel/app/views)
     *
     * @param fo FileObject
     * @return views directory FileObject
     */
    public static FileObject getViewsDirectory(FileObject fo) {
        PhpModule pm = PhpModule.forFileObject(fo);
        if (pm == null) {
            return null;
        }
        String viewsPath = String.format(FUEL_APP_VIEWS_DIR, FuelPhpPreferences.getFuelName(pm));
        return pm.getSourceDirectory().getFileObject(viewsPath);
    }

    /**
     * Get controller directory (fuel/app/classes/controller)
     *
     * @param fo FileObject
     * @return controller directory FileObject
     */
    public static FileObject getControllerDirectory(FileObject fo) {
        PhpModule pm = PhpModule.forFileObject(fo);
        if (pm == null) {
            return null;
        }
        String controllerPath = String.format(FUEL_APP_CLASSES_CONTROLLER_DIR, FuelPhpPreferences.getFuelName(pm));
        return pm.getSourceDirectory().getFileObject(controllerPath);
    }

    /**
     * Get controller directory (fuel/app/classes/controller)
     *
     * @param fo FileObject
     * @return controller directory FileObject
     */
    public static FileObject getViewModelDirectory(FileObject fo) {
        PhpModule pm = PhpModule.forFileObject(fo);
        if (pm == null) {
            return null;
        }
        String viewPath = String.format(FUEL_APP_CLASSES_VIEW_DIR, FuelPhpPreferences.getFuelName(pm));
        return pm.getSourceDirectory().getFileObject(viewPath);
    }

    /**
     * Check action name
     *
     * @param name action name
     * @return name starts with "action_", then return true.
     */
    public static boolean isActionName(String name) {
        return name.startsWith(ACTION_PREFIX) && !name.equals(ACTION_PREFIX);
    }

    /**
     * Get infered controller
     *
     * @param view view file
     * @return controller FileObject
     */
    public static FileObject getInferedController(FileObject view) {
        if (!isView(view) && !isViewModel(view)) {
            return null;
        }
        // view file path from views directory
        String viewPath = getViewPath(view);
        String controllerPath = viewPath.replace("/" + view.getNameExt(), "") + ".php"; // NOI18N

        // get controller
        FileObject controller = getControllerDirectory(view).getFileObject(controllerPath);
        if (!isController(controller)) {
            return null;
        }
        return controller;
    }

    /**
     * Get view file path from views / view model directory
     *
     * @param view
     * @return view file relative path. contain extension.
     */
    private static String getViewPath(FileObject view) {
        String viewDirectoryPath = null;
        String viewPath = view.getPath();;
        if (isView(view)) {
            viewDirectoryPath = getViewsDirectory(view).getPath();
        } else if (isViewModel(view)) {
            viewDirectoryPath = getViewModelDirectory(view).getPath();
        } else {
            return null;
        }
        String replacePath = viewPath.replace(viewDirectoryPath, ""); // NOI18N
        if (replacePath.startsWith("/")) { // NOI18N
            replacePath = replacePath.replaceFirst("/", ""); // NOI18N
        }
        return replacePath;
    }

    /**
     * Get infered controller action name
     *
     * @param view view FileObject
     * @return action name.
     */
    public static String getInferedActionName(FileObject view) {
        if (!isView(view) && !isViewModel(view)) {
            return null;
        }
        return ACTION_PREFIX + view.getName();
    }
}
