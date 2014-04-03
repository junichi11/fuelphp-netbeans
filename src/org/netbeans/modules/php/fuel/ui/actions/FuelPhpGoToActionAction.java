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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.php.fuel.ui.GoToPopup;
import org.netbeans.modules.php.fuel.ui.PopupUtil;
import org.netbeans.modules.php.fuel.ui.actions.gotos.items.GoToItem;
import org.netbeans.modules.php.fuel.ui.actions.gotos.statuses.FuelPhpGoToStatus;
import org.netbeans.modules.php.fuel.ui.actions.gotos.statuses.FuelPhpGoToStatusFactory;
import org.netbeans.modules.php.spi.framework.actions.GoToActionAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author junichi11
 */
public class FuelPhpGoToActionAction extends GoToActionAction {

    private static final long serialVersionUID = 7088563533898976812L;
    private final FileObject view;
    private final int offset;

    public FuelPhpGoToActionAction(FileObject view, int offset) {
        this.view = view;
        this.offset = offset;
    }

    @Override
    public boolean goToAction() {
        FuelPhpGoToStatusFactory factory = FuelPhpGoToStatusFactory.getInstance();
        FuelPhpGoToStatus status = factory.create(view, offset);
        status.scan();
        final List<GoToItem> items = status.getControllers();
        if (items.isEmpty()) {
            return false;
        }

        // show popup
        JTextComponent editor = EditorRegistry.lastFocusedComponent();
        if (editor == null) {
            return false;
        }
        try {
            Rectangle rectangle = editor.modelToView(editor.getCaretPosition());
            final Point point = new Point(rectangle.x, rectangle.y + rectangle.height);
            SwingUtilities.convertPointToScreen(point, editor);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    String title = "Go To Action"; // NOI18N
                    PopupUtil.showPopup(new GoToPopup(title, items), title, point.x, point.y, true, 0);
                }
            });
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return true;
    }
}
