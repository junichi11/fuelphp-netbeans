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
package org.netbeans.modules.php.fuel.ui.logicalview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule;
import org.netbeans.modules.php.fuel.options.FuelPhpOptions;
import org.netbeans.modules.php.fuel.util.FuelUtils;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;

/**
 *
 * @author junichi11
 */
@NodeFactory.Registration(projectType = "org-netbeans-modules-php-project", position = 500)
public class MVCNodeFactory implements NodeFactory {

    private static final String ASSETS = "assets"; // NOI18N

    public MVCNodeFactory() {
    }

    @Override
    public NodeList<?> createNodes(Project p) {
        PhpModule phpModule = PhpModule.lookupPhpModule(p);
        if (FuelUtils.isFuelPHP(phpModule)) {
            return new MVCNodeList(phpModule);
        }
        return NodeFactorySupport.fixedNodeList();
    }

    private static class MVCNodeList implements NodeList<FileObject> {

        private PhpModule phpModule;
        private static final Logger LOGGER = Logger.getLogger(MVCNodeList.class.getName());

        public MVCNodeList(PhpModule phpModule) {
            this.phpModule = phpModule;
        }

        @Override
        public List<FileObject> keys() {
            if (FuelUtils.isFuelPHP(phpModule)) {
                FuelPhpOptions options = FuelPhpOptions.getInstance();
                List<String> availableNodes = options.getAvailableNodes();
                List<FileObject> list = new ArrayList<FileObject>();

                for (String node : availableNodes) {
                    FileObject rootNode = getRootNode(node);
                    if (rootNode == null) {
                        continue;
                    }
                    list.add(rootNode);
                }
                return list;
            }
            return Collections.emptyList();
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public Node node(FileObject key) {
            Node node = null;
            if (key != null) {
                FileObject rootFolder = key;
                DataFolder folder = getFolder(rootFolder);
                if (folder != null) {
                    node = new MVCNode(folder, null, key.getName());
                }
            }
            return node;
        }

        private DataFolder getFolder(FileObject fileObject) {
            if (fileObject != null && fileObject.isValid()) {
                try {
                    DataFolder dataFolder = DataFolder.findFolder(fileObject);
                    return dataFolder;
                } catch (Exception ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
            return null;
        }

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }

        private FileObject getRootNode(String node) {
            FuelPhpModule fuelModule = FuelPhpModule.forPhpModule(phpModule);

            if (node.equals("controller")) { // NOI18N
                return fuelModule.getDirectory(FuelPhpModule.DIR_TYPE.APP, FuelPhpModule.FILE_TYPE.CONTROLLER, null);
            }

            if (node.equals("model")) { // NOI18N
                return fuelModule.getDirectory(FuelPhpModule.DIR_TYPE.APP, FuelPhpModule.FILE_TYPE.MODEL, null);
            }

            if (node.equals("views")) { // NOI18N
                return fuelModule.getDirectory(FuelPhpModule.DIR_TYPE.APP, FuelPhpModule.FILE_TYPE.VIEW, null);
            }

            if (node.equals("modules")) { // NOI18N
                return fuelModule.getDirectory(FuelPhpModule.DIR_TYPE.MODULES);
            }

            if (node.equals(ASSETS)) {
                FileObject publicDirectory = fuelModule.getDirectory(FuelPhpModule.DIR_TYPE.PUBLIC, FuelPhpModule.FILE_TYPE.NONE, null); // NOI18N
                if (publicDirectory != null) {
                    return publicDirectory.getFileObject(ASSETS);
                }
            }

            if (node.equals("tasks")) { // NOI18N
                return fuelModule.getDirectory(FuelPhpModule.DIR_TYPE.APP, FuelPhpModule.FILE_TYPE.TASKS, null);
            }

            return null;
        }
    }
}
