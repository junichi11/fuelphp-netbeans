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

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.*;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.fuel.util.FuelUtils;
import org.netbeans.modules.php.spi.actions.GoToViewAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author junichi11
 */
public class FuelPhpGoToViewAction extends GoToViewAction{
    private static final long serialVersionUID = -3029428763923922512L;
    private final FileObject controller;
    private final int offset;

    public FuelPhpGoToViewAction(FileObject controller, int offset) {
        assert FuelUtils.isController(controller);
        this.controller = controller;
        this.offset = offset;
    }
    
    /**
     * Find the view file, then open the view file.
     * @return Find the view file, then return true.  
     */
    @Override
    public boolean goToView() {
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        PhpBaseElement element = editorSupport.getElement(controller, offset);
        if(!(element instanceof PhpClass.Method)){
            return false;
        }
        
        String actionName = element.getName();
        if(!FuelUtils.isActionName(actionName)){
            return false;
        }
        
        FileObject view = getView(actionName);
        if(view != null){
            UiUtils.open(view, DEFAULT_OFFSET);
            return true;
        }
        
        return false;
    }
    
    /**
     * Get view 
     * @param actionName controller action name
     * @return view file FileObject. If don't exist view file, return null.
     */
    public FileObject getView(String actionName){
        String viewPath = parseAction(actionName);
        
        FileObject viewsDirectory = FuelUtils.getViewsDirectory(controller);
        if(viewsDirectory == null || viewPath.isEmpty()){
            return null;
        }
        
        return viewsDirectory.getFileObject(viewPath);
    }
    
    /**
     * Parse Controller file
     * @param name controller action name
     * @return views file path from views directory
     */
    private String parseAction(final String name){
        
        final StringBuilder viewPath = new StringBuilder();
        try {
            ParserManager.parse(Collections.singleton(Source.create(controller)), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ParserResult parseResult = (ParserResult) resultIterator.getParserResult();
                    final FuelPhpControllerVisitor controllerVisitor = new FuelPhpControllerVisitor(name);
                    controllerVisitor.scan(Utils.getRoot(parseResult));
                    viewPath.append(controllerVisitor.getViewPath());
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return viewPath.toString();
    }
    
    private static final class FuelPhpControllerVisitor extends DefaultVisitor{
        private static final String FORGE_METHOD = "forge"; // NOI18N
        private static final String VIEW_CLASS = "View"; // NOI18N
        
        private final StringBuilder viewPath = new StringBuilder();
        private String actionName;
        private String methodName = null;

        public FuelPhpControllerVisitor(String actionName) {
            this.actionName = actionName;
        }

        public String getViewPath(){
            String path = ""; // NOI18N
            synchronized(viewPath){
                path = viewPath.toString();
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
            Expression className = node.getClassName();
            if(!VIEW_CLASS.equals(CodeUtils.extractQualifiedName(className)) || !methodName.equals(actionName)){
                return;
            }
            FunctionInvocation fi = node.getMethod();
            String invokedMethodName = CodeUtils.extractFunctionName(fi);
            if(!FORGE_METHOD.equals(invokedMethodName)){
                return;
            }
            List<Expression> parameters = fi.getParameters();
            Expression e = null;
            
            if(!parameters.isEmpty()){
                e = parameters.get(0);
            }
            
            String path = ""; // NOI18N
            if(e instanceof Scalar){
                Scalar s = (Scalar)e;
                if(s.getScalarType() == Scalar.Type.STRING){
                    path = s.getStringValue().replace("'", ""); // NOI18N
                }
            }
            
            if(!path.isEmpty() && actionName.equals(methodName)){
                synchronized(viewPath){
                    viewPath.append(path).append(".php"); // NOI18N
                }
            }
        }
        
    }
}
