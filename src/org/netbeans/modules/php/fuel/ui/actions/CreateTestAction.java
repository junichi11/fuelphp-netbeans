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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.editor.PhpClass.Method;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.fuel.preferences.FuelPhpPreferences;
import org.netbeans.modules.php.fuel.util.FuelUtils;
import org.netbeans.modules.php.spi.framework.actions.BaseAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "UnitTests",
        id = "org.netbeans.modules.php.fuel.ui.actions.CreateTestAction")
@ActionRegistration(
        displayName = "#CTL_CreateTestAction")
@Messages("CTL_CreateTestAction=Create Test for FuelPHP")
public final class CreateTestAction extends BaseAction {

    private static final long serialVersionUID = 1432991188919830734L;
    private final DataObject context;
    private final FileObject targetFile;
    private FileObject targetTestDirectory;
    private Collection<Method> methods;
    private String className;

    public CreateTestAction(DataObject context) {
        this.context = context;
        targetFile = getFileObject();
    }

    @Override
    protected String getFullName() {
        return getPureName();
    }

    @Override
    protected String getPureName() {
        return Bundle.CTL_CreateTestAction();
    }

    @Override
    public void actionPerformed(PhpModule phpModule) {
        if (!FuelUtils.isFuelPHP(phpModule)) {
            return;
        }
        if (targetFile.isFolder()) {
            return;
        }
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        Collection<PhpClass> classes = editorSupport.getClasses(targetFile);
        for (PhpClass phpClass : classes) {
            className = phpClass.getName();
            methods = phpClass.getMethods();
            break;
        }
        if (className == null || className.isEmpty()) {
            return;
        }
        // create Test File
        FileObject testDirectory = getTestDirectory();
        if (testDirectory == null) {
            return;
        }
        // create directories
        createDirectories(testDirectory);
        if (targetTestDirectory == null) {
            return;
        }

        // if file exists, open it
        String testFileName = targetFile.getNameExt();
        FileObject testFile = targetTestDirectory.getFileObject(testFileName);
        if (testFile != null) {
            UiUtils.open(testFile, 0);
            return;
        }

        // get encoding
        String encoding = phpModule.getProperties().getEncoding();

        // get prefix, suffix
        String prefix = FuelPhpPreferences.getTestCasePrefix(phpModule);
        String suffix = FuelPhpPreferences.getTestCaseSuffix(phpModule);
        try {
            OutputStream outuptStream = targetTestDirectory.createAndOpen(testFileName);
            try {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(outuptStream, encoding));
                try {
                    pw.println("<?php"); //NOI18N
                    pw.println("/**"); //NOI18N
                    String group = " * @group " + FuelPhpPreferences.getTestGroupAnnotation(phpModule); //NOI18N
                    pw.println(group);
                    pw.println(" */"); //NOI18N
                    pw.format("class %s%s%s extends TestCase", prefix, className, suffix); //NOI18N
                    pw.flush();
                    pw.println();
                    pw.println("{"); //NOI18N
                    for (Method method : methods) {
                        pw.format("\tpublic function test_%s()\n\t{\n\t}\n\n", method.getName()); //NOI18N
                    }
                    pw.flush();
                    pw.println("}"); //NOI18N
                } finally {
                    pw.close();
                }
            } finally {
                outuptStream.close();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        testFile = targetTestDirectory.getFileObject(testFileName);
        UiUtils.open(testFile, 0);
    }

    private FileObject getFileObject() {
        return context.getPrimaryFile();
    }

    private FileObject getTestDirectory() {
        String[] splits = className.split("_"); //NOI18N
        int length = splits.length;
        if (length < 1) {
            return null;
        }
        if (length == 1 && !isParentClasses()) {
            return null;
        }
        StringBuilder relativePath = new StringBuilder();
        for (int i = 0; i < length + 1; i++) {
            relativePath.append("../"); //NOI18N
        }
        relativePath.append("tests/"); //NOI18N
        return targetFile.getFileObject(relativePath.toString());
    }

    private void createDirectories(FileObject testDirectory) {
        String[] splits = className.split("_"); //NOI18N
        StringBuilder sb = new StringBuilder();
        int length = splits.length;
        for (int i = 0; i < length - 1; i++) {
            sb.append(splits[i].toLowerCase());
            sb.append("/"); //NOI18N
        }
        File tests = FileUtil.toFile(testDirectory);
        File file = new File(tests, sb.toString());
        if (!file.exists()) {
            file.mkdirs();
        }
        targetTestDirectory = FileUtil.toFileObject(file);
    }

    /**
     * Check whether parent is classes directory.
     *
     * @return true if parent is classes, otherwise false.
     */
    private boolean isParentClasses() {
        if (!targetFile.isFolder()) {
            FileObject parent = targetFile.getParent();
            if (parent.getNameExt().equals("classes")) { //NOI18N
                return true;
            }
        }
        return false;
    }
}
