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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.fuel.FuelPhp;
import org.netbeans.modules.php.fuel.util.FuelUtils;
import org.netbeans.modules.php.spi.framework.actions.BaseAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 *
 * @author junichi11
 */
@ActionID(
        category = "PHP",
        id = "org.netbeans.moudles.php.fuel.FuelPhpBaseActionMenu")
@ActionRegistration(
        lazy = false,
        menuText = "#LBL_FuelPHP",
        displayName = "#LBL_FuelPHP")
@ActionReferences({
    @ActionReference(path = "Editors/text/x-php5/Toolbars/Default", position = 220000),
    @ActionReference(path = "Loaders/text/x-php5/Actions", position = 150),
    @ActionReference(path = "Editors/text/x-php5/Popup", position = 550)
})
@NbBundle.Messages("LBL_FuelPHP=FuelPHP")
public class FuelPhpBaseActionMenu extends BaseAction implements Presenter.Popup, Presenter.Toolbar {

    private static final long serialVersionUID = -2457214705573766624L;
    private static final FuelPhpBaseActionMenu INSTANCE = new FuelPhpBaseActionMenu();

    private FuelPhpBaseActionMenu() {
    }

    public static FuelPhpBaseActionMenu getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getFullName() {
        return getPureName();
    }

    @Override
    protected String getPureName() {
        return Bundle.LBL_FuelPHP();
    }

    @Override
    protected void actionPerformed(PhpModule phpModule) {
        // do nothing
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = new JMenu(Bundle.LBL_FuelPHP());
        if (FuelUtils.isFuelPHP(PhpModule.inferPhpModule())) {
            // create test
            DataObject dataObject = getDataObject();
            if (dataObject != null) {
                JMenuItem createTest = new JMenuItem(new CreateTestAction(dataObject));
                menu.add(createTest);
            }
        }
        if (menu.getItemCount() == 0) {
            menu.setVisible(false);
        }
        return menu;
    }

    @Override
    public Component getToolbarPresenter() {
        FuelToolbarPresenter fuelToolbarPresenter = new FuelToolbarPresenter();
        if (FuelUtils.isFuelPHP(PhpModule.inferPhpModule())) {
            fuelToolbarPresenter.setVisible(true);
        } else {
            fuelToolbarPresenter.setVisible(false);
        }
        return fuelToolbarPresenter;
    }

    /**
     * Get DataObject.
     *
     * @return
     */
    private DataObject getDataObject() {
        Lookup context = Utilities.actionsGlobalContext();
        return context.lookup(DataObject.class);
    }

    /**
     * Check whether action is available with editor.
     *
     * @return
     */
    private boolean isAvailableWithEditor() {
        Lookup context = Utilities.actionsGlobalContext();
        EditorCookie ec = context.lookup(EditorCookie.class);
        StyledDocument document = ec.getDocument();
        if (document == null) {
            return false;
        }
        return true;
    }

    //~ inner class
    private class FuelToolbarPresenter extends JButton {

        private static final long serialVersionUID = 209143166483474370L;
        private JPopupMenu popup;

        FuelToolbarPresenter() {
            this.setIcon(ImageUtilities.loadImageIcon(FuelPhp.FUEL_ICON_16, true));
            this.popup = new JPopupMenu();

            // add actions
            // create test
            DataObject dataObject = getDataObject();
            if (dataObject != null) {
                popup.add(new CreateTestAction(dataObject));
            }

            // add listener
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    FuelToolbarPresenter.this.popup.show(FuelToolbarPresenter.this, 0, FuelToolbarPresenter.this.getHeight());
                }
            });
        }
    }
}
