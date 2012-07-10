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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.eclipse.jgit.api.Git;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.fuel.ui.NewProjectConfigurationPanel;
import org.netbeans.modules.php.fuel.util.GithubUrlZipper;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

/**
 *
 * @author junichi11
 */
public class FuelPhpModuleExtender extends PhpModuleExtender {
    private static final String CONFIG_PHP = "fuel/app/config/config.php"; // NOI18N
    private static final String GIT_GITHUB_COM_FUEL_FUEL_GIT = "git://github.com/fuel/fuel.git";
	private NewProjectConfigurationPanel panel = null;

    @Override
    public void addChangeListener(ChangeListener cl) {
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
    }

    @Override
    public JComponent getComponent() {
	    return getPanel();
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public boolean isValid() {
	    return getPanel().getErrorMessage() == null;
    }

    @Override
    public String getErrorMessage() {
	    return getPanel().getErrorMessage();
    }

    @Override
    public String getWarningMessage() {
	    return null;
    }

    @Override
    public Set<FileObject> extend(PhpModule pm) throws ExtendingException {		            
	    FileObject localPath = pm.getSourceDirectory();
        if (getPanel().getUnzipRadioButton().isSelected()) {

            Map<String, String> downloadsMap = getPanel().getDownloadsMap();
            String url = downloadsMap.get(getPanel().getVersionList().getSelectedValue().toString());
            GithubUrlZipper zipper = new GithubUrlZipper(url, localPath);
            try {
                zipper.unzip();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            // TODO submodule update >> NetBeans 7.2++
            String remotePath = GIT_GITHUB_COM_FUEL_FUEL_GIT;
            Git.cloneRepository()
                .setURI(remotePath)
                .setDirectory(FileUtil.toFile(localPath))
                .call();
        }

		// set open file
		Set<FileObject> files = new HashSet<FileObject>();
		FileObject config;
		config = pm.getSourceDirectory().getFileObject(CONFIG_PHP);
		if(config != null){
			files.add(config);
		}

		return files;
    }
    
    public NewProjectConfigurationPanel getPanel(){
		if(panel == null){
			panel = new NewProjectConfigurationPanel();
		}
		return panel;
    }
}
