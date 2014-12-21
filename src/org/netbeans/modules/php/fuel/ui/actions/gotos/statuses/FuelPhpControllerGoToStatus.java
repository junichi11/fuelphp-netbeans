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
package org.netbeans.modules.php.fuel.ui.actions.gotos.statuses;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule.DIR_TYPE;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule.FILE_TYPE;
import org.netbeans.modules.php.fuel.ui.actions.FuelPhpControllerVisitor;
import org.netbeans.modules.php.fuel.ui.actions.gotos.items.GoToItem;
import org.netbeans.modules.php.fuel.ui.actions.gotos.items.GoToItemFactory;
import org.netbeans.modules.php.fuel.util.FuelUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

public class FuelPhpControllerGoToStatus extends FuelPhpGoToStatus {

    private static final FuelPhpControllerGoToStatus INSTANCE = new FuelPhpControllerGoToStatus();
    private final List<GoToItem> viewItems = new ArrayList<GoToItem>();
    private final List<GoToItem> allViewItems = new ArrayList<GoToItem>();
    private final List<GoToItem> viewModelItems = new ArrayList<GoToItem>();
    private final List<GoToItem> allViewModelItems = new ArrayList<GoToItem>();
    private final List<GoToItem> presenterItems = new ArrayList<GoToItem>();
    private final List<GoToItem> allPresenterItems = new ArrayList<GoToItem>();

    private FuelPhpControllerGoToStatus() {
    }

    public static FuelPhpControllerGoToStatus getInstance() {
        return INSTANCE;
    }

    private void reset() {
        viewItems.clear();
        viewModelItems.clear();
        presenterItems.clear();
        allViewItems.clear();
        allViewModelItems.clear();
        allPresenterItems.clear();
    }

    @Override
    protected void scan(PhpModule phpModule, FileObject currentFile, int offset) {
        reset();

        // scan
        final FuelPhpControllerVisitor visitor = new FuelPhpControllerVisitor(currentFile, offset);
        try {
            scanController(visitor, currentFile);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        // set view items
        Set<String> viewModelPath = visitor.getViewModelPath();
        Set<String> presenterPath = visitor.getPresenterPath();
        Set<String> viewPath = visitor.getViewPath();
        if (!viewModelPath.isEmpty()) {
            viewPath.addAll(viewModelPath);
        }
        if (!presenterPath.isEmpty()) {
            viewPath.addAll(presenterPath);
        }
        setGoToItems(viewPath, FILE_TYPE.VIEW, false);
        setGoToItems(visitor.getAllViewPath(), FILE_TYPE.VIEW, true);
        setGoToItems(viewModelPath, FILE_TYPE.VIEW_MODEL, false);
        setGoToItems(visitor.getAllViewModelPath(), FILE_TYPE.VIEW_MODEL, true);
        // since fuel 1.7.2
        setGoToItems(presenterPath, FILE_TYPE.PRESENTER, false);
        setGoToItems(visitor.getAllPresenterPath(), FILE_TYPE.PRESENTER, true);

        // sort
        sort(viewItems);
        sort(viewModelItems);
        sort(presenterItems);
        sort(allViewItems);
        sort(allViewModelItems);
        sort(allPresenterItems);
    }

    private void scanController(FuelPhpControllerVisitor visitor, FileObject targetFile) throws ParseException {
        scan(visitor, targetFile);
    }

    private void setGoToItems(Set<String> viewPath, FILE_TYPE fileType, boolean isAll) {
        FileObject targetDirectory = getDirectory(fileType);
        GoToItemFactory itemFactory = GoToItemFactory.getInstance();
        for (String path : viewPath) {
            if (StringUtils.isEmpty(path)) {
                continue;
            }

            // check moodule
            String[] moduleSplite = FuelUtils.moduleSplit(path);
            FileObject moduleDirectory = null;
            if (moduleSplite != null && moduleSplite.length == 2) {
                String moduleName = moduleSplite[0];
                path = moduleSplite[1];
                FuelPhpModule fuelModule = FuelPhpModule.forPhpModule(getPhpModule());
                moduleDirectory = fuelModule.getDirectory(DIR_TYPE.MODULES, fileType, moduleName);
            }

            // get file
            FileObject view;
            if (moduleDirectory != null) {
                view = moduleDirectory.getFileObject(path.concat(".php")); // NOI18N
            } else {
                view = targetDirectory.getFileObject(path.concat(".php")); // NOI18N
            }
            if (view == null) {
                continue;
            }

            // add to items
            GoToItem item = itemFactory.create(view, DEFAULT_OFFSET, ""); // NOI18N
            if (item == null) {
                continue;
            }
            switch (fileType) {
                case VIEW:
                    if (isAll) {
                        allViewItems.add(item);
                    } else {
                        viewItems.add(item);
                    }
                    break;
                case VIEW_MODEL:
                    if (isAll) {
                        allViewModelItems.add(item);
                    } else {
                        viewModelItems.add(item);
                    }
                    break;
                case PRESENTER:
                    if (isAll) {
                        allPresenterItems.add(item);
                    } else {
                        presenterItems.add(item);
                    }
                    break;
                default:
                    throw new AssertionError();
            }
        }
    }

    @Override
    public List<GoToItem> getSmart() {
        // add relevant files : Test, View, ViewModel
        List<GoToItem> items = getViewAndViewModel();
        items.addAll(getTests());
        return items;
    }

    @Override
    public List<GoToItem> getViews() {
        return allViewItems;
    }

    @Override
    public List<GoToItem> getViewModels() {
        return allViewModelItems;
    }

    @Override
    public List<GoToItem> getPresenters() {
        return allPresenterItems;
    }

    public List<GoToItem> getViewAndViewModel() {
        List<GoToItem> items = getView();
        items.addAll(getViewModel());
        items.addAll(getPresenter());
        return items;
    }

    public List<GoToItem> getView() {
        return viewItems;
    }

    public List<GoToItem> getViewModel() {
        return viewModelItems;
    }

    public List<GoToItem> getPresenter() {
        return presenterItems;
    }
}
