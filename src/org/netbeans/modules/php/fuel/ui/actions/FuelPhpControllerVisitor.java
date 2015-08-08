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
package org.netbeans.modules.php.fuel.ui.actions;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author junichi11
 */
public final class FuelPhpControllerVisitor extends DefaultVisitor {

    private static final String FORGE_METHOD = "forge"; // NOI18N
    private static final String VIEW_CLASS = "View"; // NOI18N
    private static final String VIEW_MODEL_CLASS = "ViewModel"; // NOI18N
    private static final String PRESENTER_CLASS = "Presenter"; // NOI18N
    private final Set<String> viewPath = new HashSet<>();
    private final Set<String> allViewPath = new HashSet<>();
    private final Set<String> viewModelPath = new HashSet<>();
    private final Set<String> allViewModelPath = new HashSet<>();
    private final Set<String> presenterPath = new HashSet<>();
    private final Set<String> allPresenterPath = new HashSet<>();
    private String actionName = ""; // NOI18N
    private String methodName = null;

    public FuelPhpControllerVisitor(String actionName) {
        this.actionName = actionName;
    }

    public FuelPhpControllerVisitor(FileObject targetFile, int currentCaretPosition) {
        // get PhpBaseElement(Method) for current positon
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        // XXX
        int startClassOffset = 0;
        Collection<PhpClass> classes = editorSupport.getClasses(targetFile);
        for (PhpClass phpClass : classes) {
            startClassOffset = phpClass.getOffset();
            break;
        }
        // FIXME exception might be occurred
        // if user run action at outside php class.
        // e.g. document area.
        //
        if (currentCaretPosition > startClassOffset) {
            PhpBaseElement phpElement = editorSupport.getElement(targetFile, currentCaretPosition);
            if (phpElement != null && phpElement instanceof PhpClass.Method) {
                PhpClass.Method method = (PhpClass.Method) phpElement;
                actionName = method.getName();
            }
        }

    }

    public Set<String> getViewPath() {
        return viewPath;
    }

    public Set<String> getAllViewPath() {
        return allViewPath;
    }

    public Set<String> getViewModelPath() {
        return viewModelPath;
    }

    public Set<String> getAllViewModelPath() {
        return allViewModelPath;
    }

    public Set<String> getPresenterPath() {
        return presenterPath;
    }

    public Set<String> getAllPresenterPath() {
        return allPresenterPath;
    }

    @Override
    public void visit(MethodDeclaration node) {
        methodName = CodeUtils.extractMethodName(node);
        super.visit(node);
    }

    @Override
    public void visit(StaticMethodInvocation node) {
        super.visit(node);
        Expression classNameExpression = node.getClassName();
        String className = CodeUtils.extractQualifiedName(classNameExpression);
        if (!VIEW_CLASS.equals(className) && !VIEW_MODEL_CLASS.equals(className) && !PRESENTER_CLASS.equals(className)) {
            return;
        }
        FunctionInvocation fi = node.getMethod();
        String invokedMethodName = CodeUtils.extractFunctionName(fi);
        if (!FORGE_METHOD.equals(invokedMethodName)) {
            return;
        }
        // get method parameters
        List<Expression> parameters = fi.getParameters();
        Expression e = null;
        if (!parameters.isEmpty()) {
            e = parameters.get(0);
        }
        String path = ""; // NOI18N
        if (e instanceof Scalar) {
            Scalar s = (Scalar) e;
            if (s.getScalarType() == Scalar.Type.STRING) {
                String value = s.getStringValue();
                path = value.substring(1, value.length() - 1);
            }
        }
        if (!path.isEmpty() && actionName != null) {
            if (methodName.equals(actionName)) {
                if (VIEW_CLASS.equals(className)) {
                    viewPath.add(path);
                } else if (VIEW_MODEL_CLASS.equals(className)) {
                    viewModelPath.add(path);
                } else if (PRESENTER_CLASS.equals(className)) {
                    presenterPath.add(path);
                }
            }

            // all
            if (VIEW_CLASS.equals(className)) {
                allViewPath.add(path);
            } else if (VIEW_MODEL_CLASS.equals(className)) {
                allViewModelPath.add(path);
            } else if (PRESENTER_CLASS.equals(className)) {
                allPresenterPath.add(path);
            }
        }
    }
}
