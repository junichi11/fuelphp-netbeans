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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author junichi11
 */
public final class FuelPhpControllerVisitor extends DefaultVisitor {

    private static final String FORGE_METHOD = "forge"; // NOI18N
    private static final String VIEW_CLASS = "View"; // NOI18N
    private static final String VIEW_MODEL_CLASS = "ViewModel"; // NOI18N
    private final Map<String, String> viewPath = new HashMap<String, String>();
    private String actionName;
    private String methodName = null;

    public FuelPhpControllerVisitor(String actionName) {
        this.actionName = actionName;
    }

    public Map<String, String> getViewPath() {
        Map<String, String> path;
        synchronized (viewPath) {
            path = viewPath;
        }
        return path;
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
        if (!VIEW_CLASS.equals(className) && !VIEW_MODEL_CLASS.equals(className) || !methodName.equals(actionName)) {
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
                path = s.getStringValue().replace("'", ""); // NOI18N
            }
        }
        if (!path.isEmpty() && actionName.equals(methodName)) {
            synchronized (viewPath) {
                viewPath.put(className, path + ".php"); // NOI18N
            }
        }
    }
}
