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

import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
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
@MimeRegistration(mimeType = "text/x-php5", service = CompletionProvider.class)
public class ViewCompletionProvider extends FuelPhpCompletionProvider {

    private boolean isView;
    private boolean isViewModel;
    private static final String SLASH = "/"; //NOI18N

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component, PhpModule phpModule) {
        final PhpModule pm = phpModule;

        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @SuppressWarnings("unchecked")
            @Override
            protected void query(CompletionResultSet completionResultSet, Document doc, int caretOffset) {
                AbstractDocument ad = (AbstractDocument) doc;
                ad.readLock();
                try {
                    // check View::forge()
                    TokenHierarchy hierarchy = TokenHierarchy.get(doc);
                    TokenSequence<PHPTokenId> ts = hierarchy.tokenSequence(PHPTokenId.language());
                    ts.move(caretOffset);
                    ts.moveNext();
                    Token<PHPTokenId> token = ts.token();
                    if (token.id() != PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING) {
                        completionResultSet.finish();
                        return;
                    }
                    String caretInput = ts.token().text().toString();

                    int startOffset = ts.offset() + 1;
                    int removeLength = caretInput.length() - 2;
                    if (removeLength < 0) {
                        removeLength = 0;
                    }
                    if (!isViewForge(ts)) {
                        completionResultSet.finish();
                        return;
                    }

                    String filter = caretInput.substring(1, caretOffset - startOffset + 1);

                    // get fuel/app/views or fuel/app/classes/view
                    // to a CompletionResultSet
                    FileObject viewDirectory = null;
                    if (isView) {
                        viewDirectory = FuelUtils.getViewsDirectory(pm);
                    } else if (isViewModel) {
                        viewDirectory = FuelUtils.getViewModelDirectory(pm);
                    }
                    if (viewDirectory == null) {
                        completionResultSet.finish();
                        return;
                    }
                    // exist subdirectory
                    int lastIndexOfSlash = filter.lastIndexOf(SLASH);
                    String directory = ""; // NOI18N
                    if (lastIndexOfSlash > 0) {
                        directory = filter.substring(0, lastIndexOfSlash + 1);
                        filter = filter.substring(lastIndexOfSlash + 1);
                        viewDirectory = viewDirectory.getFileObject(directory);
                        if (viewDirectory == null) {
                            completionResultSet.finish();
                            return;
                        }
                    }

                    FileObject[] views = viewDirectory.getChildren();
                    for (int i = 0; i < views.length; i++) {
                        final FileObject view = views[i];
                        String viewPath = view.getName();
                        if (view.isFolder()) {
                            viewPath = viewPath + SLASH;
                        }
                        if (!viewPath.isEmpty()
                            && viewPath.startsWith(filter)
                            && !viewPath.equals(".gitkeep")) { //NOI18N
                            completionResultSet.addItem(new FuelPhpCompletionItem(directory + viewPath, startOffset, removeLength));
                        }
                    }
                } finally {
                    ad.readUnlock();
                }

                completionResultSet.finish();
            }
        }, component);

    }

    /**
     * Check View::forge() or ViewModel::forge()
     *
     * @param ts
     * @return true if View or ViewModel, otherwise false
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
        isView = false;
        isViewModel = false;
        if (viewClass.equals("View")) { //NOI18N
            isView = true;
        } else if (viewClass.equals("ViewModel")) { //NOI18N
            isViewModel = true;
        } else {
            return false;
        }
        return true;
    }
}
