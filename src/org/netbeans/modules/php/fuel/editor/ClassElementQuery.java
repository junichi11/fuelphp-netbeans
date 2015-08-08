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

import java.util.HashSet;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.fuel.editor.elements.AssetClassElement;
import org.netbeans.modules.php.fuel.editor.elements.ClassElement;
import org.netbeans.modules.php.fuel.editor.elements.PresenterClassElement;
import org.netbeans.modules.php.fuel.editor.elements.ViewClassElement;
import org.netbeans.modules.php.fuel.editor.elements.ViewModelClassElement;
import org.netbeans.modules.php.fuel.util.FuelDocUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author junichi11
 */
public class ClassElementQuery {

    private static final Set<String> assetMethods = new HashSet<>();
    private static final Set<String> viewMethods = new HashSet<>();

    static {
        // Asset
        assetMethods.add("img"); // NOI18N
        assetMethods.add("js"); // NOI18N
        assetMethods.add("css"); // NOI18N
        // View, ViewModel
        viewMethods.add("forge"); // NOI18N
    }

    /**
     * Get ClassElement on current offset.
     *
     * @param doc
     * @param offset
     * @return
     */
    public static ClassElement get(Document doc, int offset) {
        if (doc == null) {
            return null;
        }
        FileObject current = NbEditorUtilities.getFileObject(doc);
        if (current == null) {
            return null;
        }
        // get token sequence
        TokenSequence<PHPTokenId> ts = FuelDocUtils.getTokenSequence(doc);
        ts.move(offset);
        ts.moveNext();

        String method = ""; // NOI18N
        while (ts.movePrevious()) {
            Token<PHPTokenId> token = ts.token();
            CharSequence text = token.text();
            PHPTokenId id = token.id();
            if (TokenUtilities.equals(text, ";") // NOI18N
                    || TokenUtilities.equals(text, "{") // NOI18N
                    || TokenUtilities.equals(text, "=")) { // NOI18N
                break;
            }
            if (id != PHPTokenId.PHP_STRING) {
                continue;
            }
            String textString = text.toString();
            if (assetMethods.contains(textString) || viewMethods.contains(textString)) {
                method = textString;
                continue;
            }

            // View::forge, ViewModel::forge
            if (viewMethods.contains(method)) {
                if (TokenUtilities.equals(text, "View")) { // NOI18N
                    return new ViewClassElement(method, current);
                } else if (TokenUtilities.equals(text, "ViewModel")) { // NOI18N
                    return new ViewModelClassElement(method, current);
                } else if (TokenUtilities.equals(text, "Presenter")) { // NOI18N
                    return new PresenterClassElement(method, current);
                }
            }

            // Asset::js, Asset::css, Asset::img
            if (assetMethods.contains(method)) {
                if (TokenUtilities.equals(text, "Asset")) { // NOI18N
                    return new AssetClassElement(method, current);
                }
            }
        }

        return null;
    }
}
