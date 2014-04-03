/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.fuel.validator;

import java.io.IOException;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author junichi11
 */
public class FuelPhpCustomizerValidatorTest extends NbTestCase {

    public FuelPhpCustomizerValidatorTest(String name) {
        super(name);
    }

    /**
     * Test of validateFuelDirectoryName method, of class
     * FuelPhpCustomizerValidator.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testValidateFuelDirectoryName() throws IOException {
        FileSystem fileSystem = FileUtil.createMemoryFileSystem();
        FileObject sourceDirectory = fileSystem.getRoot();
        FileObject fuelDirectory = sourceDirectory.createFolder("myfuel");
        FileObject fuelFile = sourceDirectory.createData("myfuel.php");

        // directory
        FuelPhpCustomizerValidator validator = new FuelPhpCustomizerValidator()
                .validateFuelDirectoryName(sourceDirectory, "myfuel");
        ValidationResult result = validator.getResult();
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());

        // not existing directory
        validator = new FuelPhpCustomizerValidator()
                .validateFuelDirectoryName(sourceDirectory, "something");
        result = validator.getResult();
        assertFalse(result.hasErrors());
        assertTrue(result.hasWarnings());

        // file
        validator = new FuelPhpCustomizerValidator()
                .validateFuelDirectoryName(sourceDirectory, "myfuel.php");
        result = validator.getResult();
        assertFalse(result.hasErrors());
        assertTrue(result.hasWarnings());

    }

    /**
     * Test of validateOilPath method, of class FuelPhpCustomizerValidator.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testValidateOilPathFile() throws IOException {
        FileSystem fileSystem = FileUtil.createMemoryFileSystem();
        FileObject sourceDirectory = fileSystem.getRoot();
        FileObject oil = sourceDirectory.createData("oil");

        // directory
        FuelPhpCustomizerValidator validator = new FuelPhpCustomizerValidator()
                .validateOilPath(sourceDirectory);
        ValidationResult result = validator.getResult();
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
    }

    /**
     * Test of validateOilPath method, of class FuelPhpCustomizerValidator.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testValidateOilPathDirectory() throws IOException {
        FileSystem fileSystem = FileUtil.createMemoryFileSystem();
        FileObject sourceDirectory = fileSystem.getRoot();
        FileObject oil = sourceDirectory.createFolder("oil");
        // directory
        FuelPhpCustomizerValidator validator = new FuelPhpCustomizerValidator()
                .validateOilPath(sourceDirectory);
        ValidationResult result = validator.getResult();
        assertFalse(result.hasErrors());
        assertTrue(result.hasWarnings());
    }

}
