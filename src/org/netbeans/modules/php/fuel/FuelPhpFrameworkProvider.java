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
package org.netbeans.modules.php.fuel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.netbeans.modules.php.api.framework.BadgeIcon;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.fuel.commands.FuelPhpFrameworkCommandSupport;
import org.netbeans.modules.php.fuel.preferences.FuelPhpPreferences;
import org.netbeans.modules.php.spi.editor.EditorExtender;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleIgnoredFilesExtender;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author junichi11
 */
public class FuelPhpFrameworkProvider extends PhpFrameworkProvider {

    protected static final RequestProcessor RP = new RequestProcessor(FuelPhpFrameworkProvider.class);
    private static final FuelPhpFrameworkProvider INSTANCE = new FuelPhpFrameworkProvider();
    private static final String ICON_PATH = "org/netbeans/modules/php/fuel/resources/fuel_badge_8.png"; // NOI18N
    private final BadgeIcon badgeIcon;
    private final Map<PhpModule, FileObject> fuelDirectory = new HashMap<PhpModule, FileObject>();

    @PhpFrameworkProvider.Registration(position = 700)
    public static FuelPhpFrameworkProvider getInstance() {
        return INSTANCE;
    }

    private FuelPhpFrameworkProvider() {
        super("fuelphp", // NOI18N
                NbBundle.getMessage(FuelPhpFrameworkProvider.class, "LBL_FrameworkName"),
                NbBundle.getMessage(FuelPhpFrameworkProvider.class, "LBL_FrameworkDescription"));
        badgeIcon = new BadgeIcon(
                ImageUtilities.loadImage(ICON_PATH),
                FuelPhpFrameworkProvider.class.getResource("/" + ICON_PATH)); // NOI18N
    }

    @Override
    public BadgeIcon getBadgeIcon() {
        return badgeIcon;
    }

    @Override
    public boolean isInPhpModule(PhpModule pm) {
        if (!FuelPhpPreferences.isEnabled(pm)) {
            return false;
        }

        FileObject fuel = fuelDirectory.get(pm);
        if (fuel != null) {
            return true;
        }

        // add file change listener
        FileObject sourceDirectory = pm.getSourceDirectory();
        if (sourceDirectory == null) {
            return false;
        }
        String fuelName = FuelPhpPreferences.getFuelName(pm);
        fuel = sourceDirectory.getFileObject(fuelName);
        if (fuel != null) {
            fuel.addFileChangeListener(new FileChangeAdapter() {
                @Override
                public void fileRenamed(FileRenameEvent fe) {
                    FileObject file = fe.getFile();
                    String newFuelName = file.getName();
                    PhpModule phpModule = PhpModule.Factory.forFileObject(file);
                    String fuelName = FuelPhpPreferences.getFuelName(phpModule);
                    if (newFuelName != null && !newFuelName.equals(fuelName)) {
                        FuelPhpPreferences.setFuelName(phpModule, newFuelName);
                    }
                }
            });
            fuelDirectory.put(pm, fuel);
            return true;
        }

        return false;
    }

    @Override
    public File[] getConfigurationFiles(PhpModule pm) {
        List<File> files = new LinkedList<File>();
        FileObject sourceDirectory = pm.getSourceDirectory();
        FileObject config = null;
        if (sourceDirectory != null) {
            String configPath = FuelPhpPreferences.getFuelName(pm) + "/app/config"; // NOI18N
            config = sourceDirectory.getFileObject(configPath);
        }
        if (config != null) {
            FileObject[] children = config.getChildren();
            for (FileObject child : children) {
                if (!child.isFolder()) {
                    files.add(FileUtil.toFile(child));
                } else {
                    for (FileObject c : child.getChildren()) {
                        if (!c.isFolder()) {
                            files.add(FileUtil.toFile(c));
                        }
                    }
                }
            }
        }

        return files.toArray(new File[files.size()]);
    }

    @Override
    public PhpModuleExtender createPhpModuleExtender(PhpModule pm) {
        return new FuelPhpModuleExtender();
    }

    @Override
    public PhpModuleProperties getPhpModuleProperties(PhpModule pm) {
        PhpModuleProperties properties = new PhpModuleProperties();
        FileObject sourceDirectory = pm.getSourceDirectory();
        if (sourceDirectory == null) {
            return properties;
        }
        // webroot directory
        FileObject webroot = sourceDirectory.getFileObject("public"); // NOI18N
        if (webroot != null) {
            properties = properties.setWebRoot(webroot);
        }
        // test directory
        // Since this method is only called when create new project, fuel name is fixed.
        FileObject testDirectory = sourceDirectory.getFileObject("fuel/app/tests"); // NOI18N
        if (testDirectory != null) {
            properties = properties.setTests(testDirectory);
        }
        return properties;
    }

    @Override
    public PhpModuleActionsExtender getActionsExtender(PhpModule pm) {
        return new FuelPhpActionsExtender();
    }

    @Override
    public PhpModuleIgnoredFilesExtender getIgnoredFilesExtender(PhpModule pm) {
        return new FuelPhpIgnoredFilesExtender(pm);
    }

    @Override
    public FrameworkCommandSupport getFrameworkCommandSupport(PhpModule pm) {
        return new FuelPhpFrameworkCommandSupport(pm);
    }

    @Override
    public EditorExtender getEditorExtender(PhpModule pm) {
        return null;
    }

    @Override
    public PhpModuleCustomizerExtender createPhpModuleCustomizerExtender(PhpModule phpModule) {
        return new FuelPhpModuleCustomizerExtender(phpModule);
    }

    @NbBundle.Messages({
        "# {0} - name",
        "FuelPhpFrameworkProvider.autoditection=FuelPHP autoditection : {0}",
        "FuelPhpFrameworkProvider.autoditection.action=If you want to enable as FuelPHP project, please click here."
    })
    @Override
    public void phpModuleOpened(final PhpModule phpModule) {
        if (!isInPhpModule(phpModule)) {
            // wait 1 minute since after projects is opened, scanning and VCS tasks are running
            RP.schedule(new FuelPhpAutoDetectionTask(phpModule), 1, TimeUnit.MINUTES);
        }
    }

    //~ inner class
    private class FuelPhpAutoDetectionTask implements Runnable {

        private final PhpModule phpModule;
        private Notification notification;

        public FuelPhpAutoDetectionTask(PhpModule phpModule) {
            this.phpModule = phpModule;
        }

        @Override
        public void run() {
            // auto detection
            FileObject sourceDirectory = phpModule.getSourceDirectory();
            String fuelName = FuelPhpPreferences.getFuelName(phpModule);
            if (sourceDirectory != null) {
                // TODO add validator class
                FileObject fuelDir = sourceDirectory.getFileObject(fuelName);
                FileObject oil = sourceDirectory.getFileObject("oil"); // NOI18N
                if (fuelDir == null || oil == null) {
                    return;
                }

                // show notification displayer
                if (!FuelPhpPreferences.isEnabled(phpModule)) {
                    NotificationDisplayer notificationDisplayer = NotificationDisplayer.getDefault();
                    notification = notificationDisplayer.notify(
                            Bundle.FuelPhpFrameworkProvider_autoditection(phpModule.getDisplayName()), // title
                            NotificationDisplayer.Priority.LOW.getIcon(), // icon
                            Bundle.FuelPhpFrameworkProvider_autoditection_action(), // detail
                            new FuelPhpAutoDetectionActionListener(), // action
                            NotificationDisplayer.Priority.LOW); // priority
                }
            }
        }

        private class FuelPhpAutoDetectionActionListener implements ActionListener {

            public FuelPhpAutoDetectionActionListener() {
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                FuelPhpPreferences.setEnabled(phpModule, true);
                phpModule.notifyPropertyChanged(new PropertyChangeEvent(this, PhpModule.PROPERTY_FRAMEWORKS, null, null));
                notification.clear();
            }
        }
    }

}
