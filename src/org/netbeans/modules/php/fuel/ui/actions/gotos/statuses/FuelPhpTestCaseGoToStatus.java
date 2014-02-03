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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule.DIR_TYPE;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule.FILE_TYPE;
import org.netbeans.modules.php.fuel.ui.actions.gotos.items.GoToItem;
import org.netbeans.modules.php.fuel.ui.actions.gotos.items.GoToItemFactory;
import org.openide.filesystems.FileObject;

public class FuelPhpTestCaseGoToStatus extends FuelPhpGoToStatus {

    private static final FuelPhpTestCaseGoToStatus INSTANCE = new FuelPhpTestCaseGoToStatus();

    private FuelPhpTestCaseGoToStatus() {
    }

    public static FuelPhpTestCaseGoToStatus getInstance() {
        return INSTANCE;
    }

    @Override
    protected void scan(PhpModule phpModule, FileObject currentFile, int offset) {
    }

    @Override
    public List<GoToItem> getSmart() {
        FileObject currentFile = getCurrentFile();
        FuelPhpModule fuelModule = FuelPhpModule.forPhpModule(getPhpModule());
        DIR_TYPE dirType = fuelModule.getDirType(currentFile);
        String dirName = ""; // NOI18N
        if (dirType == DIR_TYPE.MODULES) {
            dirName = fuelModule.getModuleName(currentFile);
        } else if (dirType == DIR_TYPE.PACKAGES) {
            dirName = fuelModule.getPackageName(currentFile);
        }
        String path = currentFile.getPath();
        String relativePath = ""; // NOI18N
        int index;
        List<String> dirNames = Arrays.asList("/controller/", "/model/", "/view/"); // NOI18N
        for (String name : dirNames) {
            index = path.indexOf(name); // NOI18N
            if (index > 0) {
                relativePath = "classes/" + path.substring(index); // NOI18N
                break;
            }
        }

        FileObject baseDirectory = fuelModule.getDirectory(dirType, FILE_TYPE.NONE, dirName);
        if (baseDirectory == null) {
            return Collections.emptyList();
        }
        FileObject target = baseDirectory.getFileObject(relativePath);
        if (target == null) {
            return Collections.emptyList();
        }
        GoToItem item = GoToItemFactory.getInstance().create(target, DEFAULT_OFFSET, ""); // NOI18N

        return Collections.singletonList(item);
    }
}
