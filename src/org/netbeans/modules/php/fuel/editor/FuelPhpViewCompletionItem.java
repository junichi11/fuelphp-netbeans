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
package org.netbeans.modules.php.fuel.editor;

import java.io.IOException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule.DIR_TYPE;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule.FILE_TYPE;
import org.netbeans.modules.php.fuel.util.FuelUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author junichi11
 */
public class FuelPhpViewCompletionItem extends FuelPhpCompletionItem {

    private final FILE_TYPE fileType;

    public FuelPhpViewCompletionItem(String text, int startOffset, int removeLength) {
        super(text, startOffset, removeLength);
        this.fileType = FILE_TYPE.NONE;
    }

    public FuelPhpViewCompletionItem(String text, int startOffset, int removeLength, FILE_TYPE fileType) {
        super(text, startOffset, removeLength);
        this.fileType = fileType;
    }

    public FuelPhpViewCompletionItem(String text, int startOffset, int removeLength, boolean isExist, FILE_TYPE fileType) {
        super(text, startOffset, removeLength, isExist);
        this.fileType = fileType;
    }

    @Override
    public void defaultAction(JTextComponent jtc) {
        String text = getText();
        Document doc = jtc.getDocument();

        // create a new file
        // alt key is pushed
        FileObject createdFile = null;
        if (isCreatableNewFile(text)) {
            try {
                createdFile = createNewFile(doc);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        super.defaultAction(jtc);

        // open created file
        if (createdFile != null) {
            UiUtils.open(createdFile, 0);
        }
    }

    /**
     * Check whether create a new file. If user push [alt + Enter], a file is
     * created.
     *
     * @param text
     * @return true alt key is pushed and end of text is not slash "/",
     * otherwise false.
     */
    private boolean isCreatableNewFile(String text) {
        return isIsAltDown() && !text.endsWith("/"); // NOI18N
    }

    /**
     * Create a new file. If text starts with module name, create a module file.
     *
     * @param doc
     * @throws IOException
     */
    private FileObject createNewFile(Document doc) throws IOException {
        String path = getText();

        // check module name
        String[] moduleSplit = FuelUtils.moduleSplit(path);
        String moduleName = ""; // NOI18N
        if (moduleSplit != null && moduleSplit.length == 2) {
            moduleName = moduleSplit[0];
            path = moduleSplit[1];
        }

        // get base directory
        FileObject fileObject = NbEditorUtilities.getFileObject(doc);
        FuelPhpModule fuelModule = FuelPhpModule.forPhpModule(PhpModule.forFileObject(fileObject));
        FileObject baseDirectory;
        if (!moduleName.isEmpty()) {
            baseDirectory = fuelModule.getDirectory(DIR_TYPE.MODULES, fileType, moduleName);
        } else {
            baseDirectory = fuelModule.getDirectory(fileObject, fileType);
        }

        // create a file
        if (baseDirectory != null) {
            path = path.concat(".php"); // NOI18N
            boolean isCreated = fuelModule.createNewFile(baseDirectory, path);
            if (isCreated) {
                return baseDirectory.getFileObject(path);
            }
        }
        return null;
    }
}
