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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.fuel.ui.GoToViewPanel;
import org.netbeans.modules.php.fuel.util.FuelUtils;
import org.netbeans.modules.php.spi.framework.actions.GoToViewAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author junichi11
 */
public class FuelPhpGoToViewAction extends GoToViewAction {

    private static final long serialVersionUID = -3029428763923922512L;
    private final FileObject controller;
    private final int offset;
    private static final String VIEW_CLASS = "View"; // NOI18N
    private static final String VIEW_MODEL_CLASS = "ViewModel"; // NOI18N

    public FuelPhpGoToViewAction(FileObject controller, int offset) {
        assert FuelUtils.isController(controller);
        this.controller = controller;
        this.offset = offset;
    }

    /**
     * Find the view file, then open the view file.
     *
     * @return Find the view file, then return true.
     */
    @Override
    public boolean goToView() {
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        PhpBaseElement element = editorSupport.getElement(controller, offset);
        if (!(element instanceof PhpClass.Method)) {
            return false;
        }

        String actionName = element.getName();
        if (!FuelUtils.isActionName(actionName)) {
            return false;
        }

        Map<String, String> viewMap = getView(actionName);
        if (viewMap.isEmpty()) {
            return false;
        }
        // check modules
        boolean isModules = FuelUtils.isInModules(controller);

        // get view or view model, infer view from view model
        FileObject viewsDirectory;
        FileObject viewModelDirectory;
        if (isModules) {
            PhpModule phpModule = PhpModule.forFileObject(controller);
            String moduleName = FuelUtils.getModuleName(controller);
            viewsDirectory = FuelUtils.getModuleDirectory(phpModule, moduleName + "/views"); // NOI18N
            viewModelDirectory = FuelUtils.getModuleDirectory(phpModule, moduleName + "/classes/view"); // NOI18N
        } else {
            viewsDirectory = FuelUtils.getViewsDirectory(controller);
            viewModelDirectory = FuelUtils.getViewModelDirectory(controller);
        }

        FileObject views = null;
        FileObject viewModel = null;
        FileObject openFile = null;

        if (viewMap.containsKey(VIEW_MODEL_CLASS)) {
            viewModel = viewModelDirectory.getFileObject(viewMap.get(VIEW_MODEL_CLASS));
            views = viewsDirectory.getFileObject(viewMap.get(VIEW_MODEL_CLASS));
        } else if (viewMap.containsKey(VIEW_CLASS)) {
            openFile = viewsDirectory.getFileObject(viewMap.get(VIEW_CLASS));
        } else {
            return false;
        }

        // select view or view model
        if ((viewModel != null) && (views != null)) {
            // open dialog
            GoToViewPanel panel = new GoToViewPanel();
            DialogDescriptor d = new DialogDescriptor(panel, "Select View"); // NOI18N
            if (DialogDisplayer.getDefault().notify(d) == DialogDescriptor.CANCEL_OPTION) {
                // don't open file
                return true;
            }

            String selected = panel.getViewList().getSelectedValue().toString();
            if (selected.equals(VIEW_CLASS)) {
                openFile = views;
            } else {
                openFile = viewModel;
            }
        }

        // open view file
        if (openFile != null) {
            UiUtils.open(openFile, DEFAULT_OFFSET);
            return true;
        }

        return false;
    }

    /**
     * Get view
     *
     * @param actionName controller action name
     * @return view file Map. If don't exist view file, return null.
     */
    public Map<String, String> getView(String actionName) {
        return parseAction(actionName);
    }

    /**
     * Parse Controller file
     *
     * @param name controller action name
     * @return views file path from views directory
     */
    private Map<String, String> parseAction(final String name) {

        final Map<String, String> viewPath = new HashMap<String, String>();
        try {
            ParserManager.parse(Collections.singleton(Source.create(controller)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ParserResult parseResult = (ParserResult) resultIterator.getParserResult();
                    final FuelPhpControllerVisitor controllerVisitor = new FuelPhpControllerVisitor(name);
                    controllerVisitor.scan(Utils.getRoot(parseResult));
                    viewPath.putAll(controllerVisitor.getViewPath());
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        return viewPath;
    }
}
