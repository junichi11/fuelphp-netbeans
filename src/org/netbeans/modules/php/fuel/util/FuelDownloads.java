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
package org.netbeans.modules.php.fuel.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author junichi11
 */
public class FuelDownloads {

    private static final String DL_NAME_PREFIX = "fuelphp-"; // NOI18N
    private static final String DL_URL_PREFIX = "http://fuelphp.com/files/download/"; // NOI18N
    private static final Map<String, String> DOWNLOADS_MAP = new HashMap<String, String>();
    private static final String[] DOWNLOAD_VERSIONS = {
        DL_NAME_PREFIX + "1.5",
        DL_NAME_PREFIX + "1.4",
        DL_NAME_PREFIX + "1.3",
        DL_NAME_PREFIX + "1.2.1",
        DL_NAME_PREFIX + "1.2",
        DL_NAME_PREFIX + "1.2-rc1",
        DL_NAME_PREFIX + "1.1",
        DL_NAME_PREFIX + "1.1-rc1",
        DL_NAME_PREFIX + "1.0.1",
        DL_NAME_PREFIX + "1.0",
        DL_NAME_PREFIX + "1.0-rc3",
        DL_NAME_PREFIX + "1.0-rc2",
        DL_NAME_PREFIX + "1.0-rc1",
    };
    static {
        DOWNLOADS_MAP.put(DL_NAME_PREFIX + "1.5",       DL_URL_PREFIX + 6);
        DOWNLOADS_MAP.put(DL_NAME_PREFIX + "1.4",       DL_URL_PREFIX + 4);
        DOWNLOADS_MAP.put(DL_NAME_PREFIX + "1.3",       DL_URL_PREFIX + 8);
        DOWNLOADS_MAP.put(DL_NAME_PREFIX + "1.2.1",     DL_URL_PREFIX + 9);
        DOWNLOADS_MAP.put(DL_NAME_PREFIX + "1.2",       DL_URL_PREFIX + 10);
        DOWNLOADS_MAP.put(DL_NAME_PREFIX + "1.2-rc1",   DL_URL_PREFIX + 7);
        DOWNLOADS_MAP.put(DL_NAME_PREFIX + "1.1",       DL_URL_PREFIX + 5);
        DOWNLOADS_MAP.put(DL_NAME_PREFIX + "1.1-rc1",   DL_URL_PREFIX + 15);
        DOWNLOADS_MAP.put(DL_NAME_PREFIX + "1.0.1",     DL_URL_PREFIX + 11);
        DOWNLOADS_MAP.put(DL_NAME_PREFIX + "1.0",       DL_URL_PREFIX + 16);
        DOWNLOADS_MAP.put(DL_NAME_PREFIX + "1.0-rc3",   DL_URL_PREFIX + 3);
        DOWNLOADS_MAP.put(DL_NAME_PREFIX + "1.0-rc2",   DL_URL_PREFIX + 13);
        DOWNLOADS_MAP.put(DL_NAME_PREFIX + "1.0-rc1",   DL_URL_PREFIX + 12);
    }

    public static Map<String, String> getDownloadsMap() {
        return DOWNLOADS_MAP;
    }

    public static String[] getDownloadVersions(){
        return DOWNLOAD_VERSIONS;
    }

    public static boolean isInternetReachable() {
        Collection<String> values = FuelDownloads.getDownloadsMap().values();
        for (String value : values) {
            try {
                URL url = new URL(value);
                URLConnection openConnection = url.openConnection();
                openConnection.connect();
            } catch (MalformedURLException ex) {
                return false;
            } catch (IOException ex) {
                return false;
            }
            break;
        }
        return true;
    }
}
