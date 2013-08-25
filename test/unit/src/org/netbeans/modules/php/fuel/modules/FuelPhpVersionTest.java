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

import static junit.framework.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author junichi11
 */
public class FuelPhpVersionTest extends NbTestCase {

    private FuelPhpVersion revisionNotStableVersion;
    private FuelPhpVersion minorNotStableVersion;
    private FuelPhpVersion revisionVersion;
    private FuelPhpVersion minorVersion;
    private FuelPhpVersion nullVersion;

    public FuelPhpVersionTest(String name) {
        super(name);
    }

    @Before
    @Override
    public void setUp() {
        revisionNotStableVersion = new FuelPhpVersion("1.6.1-rc1");
        minorNotStableVersion = new FuelPhpVersion("1.6-beta");
        revisionVersion = new FuelPhpVersion("1.5.1");
        minorVersion = new FuelPhpVersion("1.6");
        nullVersion = new FuelPhpVersion(null);
    }

    @After
    @Override
    public void tearDown() {
    }

    /**
     * Test of getMajor method, of class FuelPhpVersion.
     */
    @Test
    public void testGetMajor() {
        assertEquals(1, revisionNotStableVersion.getMajor());
        assertEquals(1, minorNotStableVersion.getMajor());
        assertEquals(1, revisionVersion.getMajor());
        assertEquals(1, minorVersion.getMajor());

        assertEquals(-1, nullVersion.getMajor());
    }

    /**
     * Test of getMinor method, of class FuelPhpVersion.
     */
    @Test
    public void testGetMinor() {
        assertEquals(6, revisionNotStableVersion.getMinor());
        assertEquals(6, minorNotStableVersion.getMinor());
        assertEquals(5, revisionVersion.getMinor());
        assertEquals(6, minorVersion.getMinor());

        assertEquals(-1, nullVersion.getMinor());
    }

    /**
     * Test of getRevision method, of class FuelPhpVersion.
     */
    @Test
    public void testGetRevision() {
        assertEquals(1, revisionNotStableVersion.getRevision());
        assertEquals(-1, minorNotStableVersion.getRevision());
        assertEquals(1, revisionVersion.getRevision());
        assertEquals(-1, minorVersion.getRevision());

        assertEquals(-1, nullVersion.getRevision());
    }

    /**
     * Test of getNotStable method, of class FuelPhpVersion.
     */
    @Test
    public void testGetNotStable() {
        assertEquals("rc1", revisionNotStableVersion.getNotStable());
        assertEquals("beta", minorNotStableVersion.getNotStable());
        assertEquals("", revisionVersion.getNotStable());
        assertEquals("", minorVersion.getNotStable());

        assertEquals("", nullVersion.getNotStable());
    }

    /**
     * Test of getVersionNumber method, of class FuelPhpVersion.
     */
    @Test
    public void testGetVersionNumber() {
        assertEquals("1.6.1-rc1", revisionNotStableVersion.getVersionNumber());
        assertEquals("1.6-beta", minorNotStableVersion.getVersionNumber());
        assertEquals("1.5.1", revisionVersion.getVersionNumber());
        assertEquals("1.6", minorVersion.getVersionNumber());

        assertEquals("UNKOWN", nullVersion.getVersionNumber());
    }
}
