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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule.FILE_TYPE;
import org.netbeans.modules.php.fuel.ui.actions.gotos.items.GoToItem;
import org.netbeans.modules.php.fuel.ui.actions.gotos.items.GoToItemFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author junichi11
 */
public abstract class FuelPhpGoToStatus {

    private FileObject currentFile;
    private PhpModule phpModule;
    private int currentOffset;
    protected static final int DEFAULT_OFFSET = 0;
    private static final Comparator<GoToItem> FILE_COMPARATOR = new Comparator<GoToItem>() {
        @Override
        public int compare(GoToItem o1, GoToItem o2) {
            return o1.getFileObject().getName().compareToIgnoreCase(o2.getFileObject().getName());
        }
    };

    FuelPhpGoToStatus() {
    }

    public void scan() {
        if (phpModule != null && currentFile != null) {
            scan(phpModule, currentFile, currentOffset);
        }
    }

    protected abstract void scan(PhpModule phpModule, FileObject currentFile, int offset);

    protected void scan(final DefaultVisitor visitor, FileObject targetFile) throws ParseException {
        ParserManager.parse(Collections.singleton(Source.create(targetFile)), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ParserResult parseResult = (ParserResult) resultIterator.getParserResult();
                visitor.scan(Utils.getRoot(parseResult));
            }
        });
    }

    // gettter and setter
    public FileObject getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(FileObject currentFile) {
        this.currentFile = currentFile;
        if (currentFile != null) {
            this.phpModule = PhpModule.Factory.forFileObject(currentFile);
        }
    }

    public PhpModule getPhpModule() {
        return phpModule;
    }

    public void setPhpModule(PhpModule phpModule) {
        this.phpModule = phpModule;
    }

    public int getCurrentOffset() {
        return currentOffset;
    }

    public void setCurrentOffset(int currentOffset) {
        this.currentOffset = currentOffset;
    }

    /**
     * Get all items.
     *
     * @return all GoToItems
     */
    public List<GoToItem> getAll() {
        List<GoToItem> items = new ArrayList<>();
        items.addAll(getControllers());
        items.addAll(getModels());
        items.addAll(getViews());
        items.addAll(getViewModels());
        items.addAll(getPresenters());
        items.addAll(getTests());
        return items;
    }

    /**
     * Get smart go to items.
     *
     * @return
     */
    public List<GoToItem> getSmart() {
        return Collections.emptyList();
    }

    /**
     * Get GoToItems for controller.
     *
     * @return GoToItems if it is not empty, otherwise empty list.
     */
    public List<GoToItem> getControllers() {
        return Collections.emptyList();
    }

    /**
     * Get GoToItems for controller.
     *
     * @return GoToItems if it is not empty, otherwise empty list.
     */
    public List<GoToItem> getModels() {
        return Collections.emptyList();
    }

    /**
     * Get GoToItems for view.
     *
     * @return GoToItems if it is not empty, otherwise empty list.
     */
    public List<GoToItem> getViews() {
        return Collections.emptyList();
    }

    /**
     * Get GoToItems for view model.
     *
     * @return GoToItems if it is not empty, otherwise empty list.
     */
    public List<GoToItem> getViewModels() {
        return Collections.emptyList();
    }

    /**
     * Get GoToItems for presenter.
     *
     * @return GoToItems if it is not empty, otherwise empty list.
     */
    public List<GoToItem> getPresenters() {
        return Collections.emptyList();
    }

    /**
     * Get GoToItems for test.
     *
     * @return GoToItems if it is not empty, otherwise empty list.
     */
    public List<GoToItem> getTests() {
        FuelPhpModule fuelModule = FuelPhpModule.forPhpModule(phpModule);
        FileObject testsDirectory = getDirectory(FILE_TYPE.TESTS);
        if (testsDirectory == null) {
            return Collections.emptyList();
        }
        FILE_TYPE fileType = fuelModule.getFileType(currentFile);
        String path = currentFile.getPath();
        String relativePath = "";
        int index = 0;
        switch (fileType) {
            case CONTROLLER:
                index = path.indexOf("/controller/"); // NOI18N
                break;
            case MODEL:
                index = path.indexOf("/model/"); // NOI18N
                break;
            case VIEW_MODEL:
                index = path.indexOf("/view/"); // NOI18N
                break;
            case PRESENTER:
                index = path.indexOf("/presenter/"); // NOI18N
                break;
            default:
                // do nothing
                break;
        }

        if (index > 0) {
            relativePath = path.substring(index);
        }
        if (StringUtils.isEmpty(relativePath)) {
            return Collections.emptyList();
        }
        // get tartget
        FileObject target = testsDirectory.getFileObject(relativePath);
        if (target == null) {
            return Collections.emptyList();
        }
        GoToItem goToItem = GoToItemFactory.getInstance().create(target, DEFAULT_OFFSET, ""); // NOI18N
        return Collections.singletonList(goToItem);
    }

    /**
     * Get controller items.
     *
     * @return
     */
    public List<GoToItem> getAllControllers() {
        return getGoToItems(FILE_TYPE.CONTROLLER);
    }

    /**
     * Get model items.
     *
     * @return
     */
    public List<GoToItem> getAllModels() {
        return getGoToItems(FILE_TYPE.MODEL);
    }

    /**
     * Get view model items.
     *
     * @return
     */
    public List<GoToItem> getAllViewModels() {
        return getGoToItems(FILE_TYPE.VIEW_MODEL);
    }

    /**
     * Get presenter items.
     *
     * @return
     */
    public List<GoToItem> getAllPresenters() {
        return getGoToItems(FILE_TYPE.PRESENTER);
    }

    /**
     * Get configuration items.
     *
     * @return
     */
    public List<GoToItem> getConfigurations() {
        return getGoToItems(FILE_TYPE.CONFIG);
    }

    /**
     * Get task items.
     *
     * @return
     */
    public List<GoToItem> getTasks() {
        return getGoToItems(FILE_TYPE.TASKS);
    }

    /**
     * Get task items.
     *
     * @return
     */
    public List<GoToItem> getAllTests() {
        return getGoToItems(FILE_TYPE.TESTS);
    }

    /**
     * Get GoToItems for file type.
     *
     * @param fileType file type
     * @return GoToItems
     */
    private List<GoToItem> getGoToItems(FILE_TYPE fileType) {
        FileObject directory = getDirectory(fileType);
        if (directory == null) {
            return Collections.emptyList();
        }

        Enumeration<? extends FileObject> children = directory.getChildren(true);
        List<GoToItem> items = new ArrayList<>();
        GoToItemFactory factory = GoToItemFactory.getInstance();
        while (children.hasMoreElements()) {
            FileObject child = children.nextElement();
            if (FileUtils.isPhpFile(child)) {
                // use EditorCookie if offset is 0 in order to open a file
                items.add(factory.create(child, DEFAULT_OFFSET, null));
            }
        }
        sort(items);
        return items;
    }

    /**
     * Get directory for file type.
     *
     * @param fileType
     * @return directory for file type.
     */
    protected FileObject getDirectory(FILE_TYPE fileType) {
        FuelPhpModule fuelModule = FuelPhpModule.forPhpModule(phpModule);
        return fuelModule.getDirectory(currentFile, fileType);
    }

    /**
     * Get controller items for view and view model. Return empty list if file
     * type is another type.
     *
     * @param fileType
     * @return
     */
    protected List<GoToItem> getControllers(FILE_TYPE fileType) {
        if (fileType != FILE_TYPE.VIEW && fileType != FILE_TYPE.VIEW_MODEL && fileType != FILE_TYPE.PRESENTER) {
            return Collections.emptyList();
        }
        FileObject view = getCurrentFile();

        // get view directory
        FileObject viewDirectory = getDirectory(fileType);
        if (viewDirectory == null) {
            return Collections.emptyList();
        }

        // create relative path to controller file
        String viewDirectoryPath = viewDirectory.getPath();
        String viewPath = view.getPath();
        String contorllerPath = viewPath.replace(viewDirectoryPath, ""); // NOI18N
        contorllerPath = contorllerPath.replace("/" + view.getNameExt(), ""); // NOI18N

        // get controller directory
        FileObject controllerDirectory = getDirectory(FILE_TYPE.CONTROLLER);
        if (controllerDirectory == null) {
            return Collections.emptyList();
        }
        FileObject controller = controllerDirectory.getFileObject(contorllerPath.concat(".php")); // NOI18N

        // get controller
        if (controller == null) {
            return Collections.emptyList();
        }

        // infer method
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        Collection<PhpClass> classes = editorSupport.getClasses(controller);
        int methodOffset = 0;
        String methodName = ""; // NOI18N
        for (PhpClass phpClass : classes) {
            for (PhpClass.Method method : phpClass.getMethods()) {
                if (method.getName().equals("action_" + view.getName())) { // NOI18N
                    methodName = method.getName();
                    methodOffset = method.getOffset();
                    break;
                }
            }
            break;
        }

        GoToItemFactory factory = GoToItemFactory.getInstance();
        List<GoToItem> items = new ArrayList<>(2);
        items.add(factory.create(controller, DEFAULT_OFFSET, "")); // NOI18N
        if (!methodName.isEmpty()) {
            items.add(factory.create(controller, methodOffset, methodName));
        }
        return items;
    }

    /**
     * Sort with file name.
     *
     * @param items
     */
    public void sort(List<GoToItem> items) {
        Collections.sort(items, FILE_COMPARATOR);
    }
}
