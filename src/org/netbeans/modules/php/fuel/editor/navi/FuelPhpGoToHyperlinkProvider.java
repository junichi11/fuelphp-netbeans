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
package org.netbeans.modules.php.fuel.editor.navi;

import java.io.File;
import java.io.IOException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.fuel.editor.ClassElementQuery;
import org.netbeans.modules.php.fuel.editor.elements.ClassElement;
import org.netbeans.modules.php.fuel.util.FuelDocUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author junichi11
 */
@MimeRegistration(mimeType = FileUtils.PHP_MIME_TYPE, service = HyperlinkProviderExt.class)
public class FuelPhpGoToHyperlinkProvider extends FuelPhpHyperlinkProviderExt {

    private FileObject targetFile;
    private String targetText;
    private ClassElement element;

    @Override
    public boolean verifyState(Document document, final int offset, HyperlinkType type) {
        TokenSequence<PHPTokenId> ts = FuelDocUtils.getTokenSequence(document);
        ts.move(offset);
        ts.moveNext();
        Token<PHPTokenId> token = ts.token();
        PHPTokenId id = token.id();
        String text = token.text().toString();
        if (id != PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING || text.equals("")) {
            return false;
        }

        // set span
        targetText = text.substring(1, text.length() - 1);
        int start = ts.offset() + 1;
        int end = start + targetText.length();
        setHyperlinkSpan(start, end);

        // get class element
        PhpModule phpModule = PhpModule.forFileObject(NbEditorUtilities.getFileObject(document));
        element = ClassElementQuery.get(document, offset);
        if (element == null || element.getBaseDirectory(phpModule) == null) {
            return false;
        }

        // set target file
        setTargetFile(phpModule);
        return true;
    }

    @Override
    public void performClickAction(Document document, int offset, HyperlinkType type) {
        // create file
        // TODO add project properties option
        createFile();

        // open file
        if (targetFile != null) {
            UiUtils.open(targetFile, 0);
        }
    }

    @Override
    @NbBundle.Messages("LBL_CreateNewFileMessage=Not found file : create a new empty file when you click here")
    public String getTooltipText(Document document, int offset, HyperlinkType type) {
        if (targetFile == null) {
            return Bundle.LBL_CreateNewFileMessage();
        }

        // get source directory
        PhpModule phpModule = PhpModule.inferPhpModule();
        FileObject sourceDirectory = null;
        if (phpModule != null) {
            sourceDirectory = phpModule.getSourceDirectory();
        }

        // get path
        String relativePath = ""; // NOI18N
        if (sourceDirectory != null) {
            relativePath = FileUtil.getRelativePath(sourceDirectory, targetFile);
        } else {
            relativePath = targetFile.getPath();
        }
        return relativePath;
    }

    /**
     * Create file if target file doesn't exist.
     */
    private void createFile() {
        if (targetFile == null) {
            // create new empty file
            PhpModule phpModule = PhpModule.inferPhpModule();
            FileObject baseDirectory = element.getBaseDirectory(phpModule);
            File base = FileUtil.toFile(baseDirectory);
            File newFile = new File(base, targetText + element.getExtension());
            File parentFile = newFile.getParentFile();

            // create sub directories
            if (!parentFile.getName().equals(baseDirectory.getNameExt())) {
                parentFile.mkdirs();
            }

            // create file
            try {
                if (newFile.createNewFile()) {
                    setTargetFile(phpModule);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    /**
     * Set target file.
     *
     * @param phpModule
     */
    private void setTargetFile(PhpModule phpModule) {
        // get base directory
        FileObject baseDirectory = element.getBaseDirectory(phpModule);

        // get target file
        if (baseDirectory != null) {
            targetFile = baseDirectory.getFileObject(targetText + element.getExtension()); // NOI18N
        }
    }
}
