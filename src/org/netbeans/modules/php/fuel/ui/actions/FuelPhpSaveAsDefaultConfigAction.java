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

import java.io.IOException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule.DIR_TYPE;
import org.netbeans.modules.php.fuel.modules.FuelPhpModule.FILE_TYPE;
import org.netbeans.modules.php.fuel.options.FuelPhpOptions;
import org.netbeans.modules.php.fuel.util.FuelUtils;
import org.netbeans.modules.php.spi.framework.actions.BaseAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author junichi11
 */
@NbBundle.Messages({
    "FuelPhpSaveAsDefaultConfigAction.name=Save config as default",
    "FuelPhpSaveAsDefaultConfigAction.success=Success : Set source code of config.php to Options"
})
public final class FuelPhpSaveAsDefaultConfigAction extends BaseAction {

    private static final FuelPhpSaveAsDefaultConfigAction INSTANCE = new FuelPhpSaveAsDefaultConfigAction();
    private static final long serialVersionUID = -4791298716997294066L;

    private FuelPhpSaveAsDefaultConfigAction() {
    }

    public static FuelPhpSaveAsDefaultConfigAction getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getFullName() {
        return Bundle.LBL_FuelPhpAction(getPureName());
    }

    @Override
    protected String getPureName() {
        return Bundle.FuelPhpSaveAsDefaultConfigAction_name();
    }

    @Override
    protected void actionPerformed(PhpModule phpModule) {
        // called via shortcut
        if (!FuelUtils.isFuelPHP(phpModule)) {
            // do nothing
            return;
        }

        // get config file
        FuelPhpModule fuelModule = FuelPhpModule.forPhpModule(phpModule);
        FileObject configDirectory = fuelModule.getDirectory(DIR_TYPE.APP, FILE_TYPE.CONFIG, ""); // NOI18N
        if (configDirectory == null) {
            return;
        }
        FileObject config = configDirectory.getFileObject("config.php"); // NOI18N
        if (config == null) {
            return;
        }

        // set text to options
        FuelPhpOptions options = FuelPhpOptions.getInstance();
        try {
            options.setDefaultConfig(config.asText("UTF-8")); // NOI18N

            // open dialog
            NotifyDescriptor descriptor = new NotifyDescriptor(
                    "Success : Set source code of config.php to Options",
                    getPureName(),
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.INFORMATION_MESSAGE,
                    null,
                    null);
            Object response = DialogDisplayer.getDefault().notify(descriptor);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
