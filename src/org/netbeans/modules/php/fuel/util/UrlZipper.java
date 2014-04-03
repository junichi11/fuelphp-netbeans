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
package org.netbeans.modules.php.fuel.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author junichi11
 */
public class UrlZipper {

    protected String url;
    protected String unzipRootDirName;
    protected FileObject baseDir;
    protected ZipEntryFilter filter;

    public UrlZipper(String url, FileObject baseDir, ZipEntryFilter filter, String unziipRootDirName) {
        this.url = url;
        this.baseDir = baseDir;
        this.unzipRootDirName = unziipRootDirName;
        this.filter = filter;
    }

    public UrlZipper(String url, FileObject baseDir, ZipEntryFilter filter) {
        this.url = url;
        this.baseDir = baseDir;
        this.unzipRootDirName = ""; // NOI18N
        this.filter = filter;
    }

    protected ZipInputStream getZipInputStream() throws MalformedURLException, IOException {
        URL zipUrl = new URL(url);
        ZipInputStream zi = new ZipInputStream(zipUrl.openStream());

        return zi;
    }

    public void unzip() throws MalformedURLException, IOException {
        if (baseDir == null) {
            return;
        }

        ZipInputStream zipInputStream = getZipInputStream();
        ZipEntry zipEntry;

        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (!filter.accept(zipEntry)) {
                continue;
            }
            // change from zipRootDirName to unzipRootDirName
            String unzipFileName = filter.getPath(zipEntry);
            File unzipBaseDir = FileUtil.toFile(baseDir);
            File unzipFile = new File(unzipBaseDir, unzipFileName);

            // if zipentry is directrory, make dir
            if (zipEntry.isDirectory()) {
                unzipFile.mkdir();
                zipInputStream.closeEntry();
                continue;
            } else {
                File parentFile = unzipFile.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
            }

            // write data
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(unzipFile));
            int data;
            while ((data = zipInputStream.read()) != -1) {
                outputStream.write(data);
            }
            zipInputStream.closeEntry();
            outputStream.close();
        }
    }
}
