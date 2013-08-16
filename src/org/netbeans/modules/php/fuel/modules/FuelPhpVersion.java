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
package org.netbeans.modules.php.fuel.modules;

import org.netbeans.modules.php.api.util.StringUtils;

/**
 *
 * @author junichi11
 */
public final class FuelPhpVersion {

    private static final String UNKOWN = "UNKOWN"; // NOI18N
    private int major = -1;
    private int minor = -1;
    private int revision = -1;
    private String notStable = ""; // NOI18N
    private String versionNumber;

    public FuelPhpVersion(String versionNumber) {
        this.versionNumber = versionNumber;
        setVersions(versionNumber);
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getNotStable() {
        return notStable;
    }

    public void setNotStable(String notStable) {
        this.notStable = notStable;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    private void setVersions(String versionNumber) throws NumberFormatException {
        if (StringUtils.isEmpty(versionNumber)) {
            this.versionNumber = UNKOWN;
            return;
        }

        String[] versions = versionNumber.split("[.-]"); // NOI18N
        int length = versions.length;
        if (length == 4) {
            notStable = versions[3];
        }

        if (length >= 3) {
            try {
                revision = Integer.parseInt(versions[2]);
            } catch (NumberFormatException e) {
                notStable = versions[2];
            }
        }

        if (length >= 2) {
            minor = Integer.parseInt(versions[1]);
        }

        if (length >= 1) {
            major = Integer.parseInt(versions[0]);
        }
    }
}
