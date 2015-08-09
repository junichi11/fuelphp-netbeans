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
package org.netbeans.modules.php.fuel.editor;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule.DIR_TYPE;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule.FILE_TYPE;
import org.netbeans.modules.php.fuel.util.FuelDocUtils;
import org.netbeans.modules.php.fuel.util.FuelUtils;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;

/**
 *
 * @author junichi11
 */
@MimeRegistration(mimeType = FileUtils.PHP_MIME_TYPE, service = CompletionProvider.class)
public class ViewCompletionProvider extends FuelPhpCompletionProvider {

    private static final String SLASH = "/"; //NOI18N
    private String filter;
    private String directoryPath;
    private String moduleName;
    private int startOffset;
    private int removeLength;
    private boolean isExistSameAsFilter;
    private FILE_TYPE fileType = FILE_TYPE.NONE;

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component, PhpModule phpModule) {
        final PhpModule pm = phpModule;

        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @SuppressWarnings("unchecked")
            @Override
            protected void query(CompletionResultSet completionResultSet, Document doc, int caretOffset) {
                // check View::forge()
                TokenSequence<PHPTokenId> ts = FuelDocUtils.getTokenSequence(doc);
                ts.move(caretOffset);
                ts.moveNext();
                Token<PHPTokenId> token = ts.token();
                try {
                    if (token.id() != PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING) {
                        return;
                    }
                    String inputValue = ts.token().text().toString();

                    // init
                    setStartOffset(ts);
                    setRemoveLength(inputValue);
                    if (!isViewForge(ts)) {
                        return;
                    }
                    initFilter(inputValue, caretOffset);

                    // get current file
                    FileObject fileObject = NbEditorUtilities.getFileObject(doc);
                    if (fileObject == null) {
                        return;
                    }

                    // set module name
                    setModuleName();

                    // get base view directory
                    FileObject viewDirectory = getViewDirectory(pm, fileObject);
                    if (viewDirectory == null) {
                        completionResultSet.addItem(new FuelPhpViewCompletionItem(getInsertPath(filter), startOffset, removeLength, false, getFileType()));
                        return;
                    }

                    // add items
                    FileObject[] views = viewDirectory.getChildren();
                    addItems(views, completionResultSet);
                    if (!isExistSameAsFilter && !filter.isEmpty()) {
                        completionResultSet.addItem(new FuelPhpViewCompletionItem(getInsertPath(filter), startOffset, removeLength, false, getFileType()));
                    }
                } finally {
                    completionResultSet.finish();
                }
            }
        }, component);
    }

    private void setStartOffset(TokenSequence<PHPTokenId> ts) {
        this.startOffset = ts.offset() + 1;
    }

    private void setRemoveLength(String inputValue) {
        this.removeLength = inputValue.length() - 2;
        if (this.removeLength < 0) {
            this.removeLength = 0;
        }
    }

    private void initFilter(String inputValue, int caretOffset) {
        if (inputValue.length() > 2 && caretOffset >= startOffset) {
            filter = inputValue.substring(1, caretOffset - startOffset + 1);
        } else {
            filter = ""; // NOI18N
        }
    }

    private void setModuleName() {
        // module name support
        String[] moduleSplit = FuelUtils.moduleSplit(filter);
        if (moduleSplit == null) {
            moduleName = ""; // NOI18N
            return;
        }

        switch (moduleSplit.length) {
            case 2:
                moduleName = moduleSplit[0];
                filter = moduleSplit[1];
                break;
            case 1:
                if (filter.endsWith("::")) { // NOI18N
                    moduleName = moduleSplit[0];
                    filter = ""; // NOI18N
                } else {
                    moduleName = ""; // NOI18N
                }
                break;
            default:
                moduleName = ""; // NOI18N
                break;
        }
    }

    private FileObject getViewDirectory(PhpModule phpModule, FileObject fileObject) {
        FuelPhpModule fuelModule = FuelPhpModule.forPhpModule(phpModule);
        FileObject viewDirectory;
        if (!moduleName.isEmpty()) {
            viewDirectory = fuelModule.getDirectory(DIR_TYPE.MODULES, getFileType(), moduleName);
        } else {
            viewDirectory = fuelModule.getDirectory(fileObject, getFileType());
        }

        if (viewDirectory == null) {
            return null;
        }

        // exist subdirectory
        int lastIndexOfSlash = filter.lastIndexOf(SLASH);
        directoryPath = ""; // NOI18N
        if (lastIndexOfSlash > 0) {
            directoryPath = filter.substring(0, lastIndexOfSlash + 1);
            filter = filter.substring(lastIndexOfSlash + 1);
            viewDirectory = viewDirectory.getFileObject(directoryPath);
        }

        return viewDirectory;
    }

    private void addItems(FileObject[] views, CompletionResultSet completionResultSet) {
        isExistSameAsFilter = false;
        if (views == null) {
            return;
        }
        for (FileObject view : views) {
            String viewPath = view.getName();
            if (view.isFolder()) {
                viewPath = viewPath + SLASH;
            }
            if (viewPath.equals(filter)) {
                isExistSameAsFilter = true;
            }
            if (!viewPath.isEmpty()
                    && viewPath.startsWith(filter)
                    && !viewPath.equals(".gitkeep")) { //NOI18N
                completionResultSet.addItem(new FuelPhpViewCompletionItem(getInsertPath(viewPath), startOffset, removeLength));
            }
        }
    }

    private String getInsertPath(String viewPath) {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(moduleName)) {
            sb.append(moduleName);
            sb.append("::"); // NOI18N
        }
        if (!StringUtils.isEmpty(directoryPath)) {
            sb.append(directoryPath);
        }
        if (!StringUtils.isEmpty(viewPath)) {
            sb.append(viewPath);
        }
        return sb.toString();
    }

    /**
     * Check View::forge() or ViewModel::forge() or Presenter::forge()
     *
     * @param ts
     * @return true if View or ViewModel or Presenter, otherwise false
     */
    private boolean isViewForge(TokenSequence<PHPTokenId> ts) {
        // brace
        ts.movePrevious();
        // forge
        ts.movePrevious();
        String forgeMethod = ts.token().text().toString();
        if (!forgeMethod.equals("forge") || ts.token().id() != PHPTokenId.PHP_STRING) { //NOI18N
            return false;
        }
        // :: operator
        ts.movePrevious();
        ts.movePrevious();
        String viewClass = ts.token().text().toString();
        if (ts.token().id() != PHPTokenId.PHP_STRING) {
            return false;
        }
        switch (viewClass) {
            case "View": // NOI18N
                fileType = FILE_TYPE.VIEW;
                break;
            case "ViewModel": // NOI18N
                fileType = FILE_TYPE.VIEW_MODEL;
                break;
            case "Presenter": // NOI18N
                fileType = FILE_TYPE.PRESENTER;
                break;
            default:
                fileType = FILE_TYPE.NONE;
                return false;
        }
        return true;
    }

    private FILE_TYPE getFileType() {
        return fileType;
    }
}
