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
package org.netbeans.modules.php.fuel.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommand;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 *
 * @author junichi11
 */
public class Oil {

    public static final String OPTIONS_SUB_PATH = "FuelPHP"; // NOI18N
    // regex
    private static final String MAIN_COMMAND_REGEX = "\\A.+php oil \\[(.+?)\\].+\\z"; // NOI18N
    private static final String GENERATE_SUB_COMMAND_REGEX = "\\A.+php oil \\[.+?\\] ?\\[(.+?)\\].+\\z"; // NOI18N
    // commands
    private static final String OIL = "oil"; // NOI18N
    private static final String HELP_COMMAND = "help"; // NOI18N
    private static final String GENERATE_COMMAND = "generate"; // NOI18N
    private static final String CONSOLE_COMMAND = "console"; // NOI18N
    private static final String TEST_COMMAND = "test"; // NOI18N
    // default params
    private static final List<String> DEFAULT_PARAMS = Collections.emptyList();
    private static final Set<String> IGNORE_HELP_COMMANDS = new HashSet<String>();
    private final String oilPath;

    static {
        IGNORE_HELP_COMMANDS.add(CONSOLE_COMMAND);
        IGNORE_HELP_COMMANDS.add(TEST_COMMAND);
    }

    private Oil(String oilPath) {
        this.oilPath = oilPath;
    }

    /**
     * Get the project specific, <b>valid only</b> oil script. If not found,
     * {@code null} is returned.
     *
     * @param phpModule PHP module for which oil script is taken
     * @param warn <code>true</code> if user is warned when the oil script is
     * not valid
     * @return oil console script or {@code null} if the script is not valid
     */
    @NbBundle.Messages({
        "# {0} - error message",
        "Oil.script.invalid=<html>Project's Oil script is not valid.<br>({0})"
    })
    public static Oil forPhpModule(PhpModule phpModule, boolean warn) throws InvalidPhpExecutableException {
        // get oil file
        String oilFilePath = null;
        FileObject script = getOilFile(phpModule);
        if (script != null) {
            oilFilePath = FileUtil.toFile(script).getAbsolutePath();
        }

        // validate
        String error = validate(oilFilePath);
        if (error == null) {
            return new Oil(oilFilePath);
        }

        // not found oil
        if (warn) {
            NotifyDescriptor.Message message = new NotifyDescriptor.Message(
                    Bundle.Oil_script_invalid(error),
                    NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(message);
        }
        throw new InvalidPhpExecutableException(error);
    }

    /**
     * Get oil file.
     *
     * @param phpModule
     * @return oil file if file exists, otherewise null.
     */
    private static FileObject getOilFile(PhpModule phpModule) {
        if (phpModule == null) {
            return null;
        }
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            return null;
        }
        return sourceDirectory.getFileObject(OIL);
    }

    /**
     * Validate command.
     *
     * @param command
     * @return
     */
    @NbBundle.Messages("Oil.script.label=FuelPHP Oil")
    public static String validate(String command) {
        return PhpExecutableValidator.validateCommand(command, Bundle.Oil_script_label());
    }

    /**
     * Run command.
     *
     * @param phpModule
     * @param parameters
     * @param postExecution
     */
    public void runCommand(PhpModule phpModule, List<String> parameters, Runnable postExecution) {
        createPhpExecutable(phpModule)
                .displayName(getDisplayName(phpModule, parameters.get(0)))
                .additionalParameters(getAllParams(parameters))
                .run(getDescriptor(postExecution));
    }

    /**
     * Create PhpExecutable. Workind directory is source directory.
     *
     * @param phpModule
     * @return PhpExecutable
     */
    private PhpExecutable createPhpExecutable(PhpModule phpModule) {
        return new PhpExecutable(oilPath)
                .workDir(FileUtil.toFile(phpModule.getSourceDirectory()));
    }

    /**
     * Get descriptor.
     *
     * @param postExecution
     * @return ExecutionDescriptor
     */
    private ExecutionDescriptor getDescriptor(Runnable postExecution) {
        ExecutionDescriptor executionDescriptor = PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(OPTIONS_SUB_PATH);
        if (postExecution != null) {
            executionDescriptor = executionDescriptor.postExecution(postExecution);
        }
        return executionDescriptor;
    }

    /**
     * Get commands from help. (php oil [cell|generate|...|test])
     *
     * @param phpModule
     * @return oil commands
     */
    public List<FrameworkCommand> getCommands(PhpModule phpModule) {
        // get commands from help
        String[] commands = getCommands(phpModule, new String[]{}, MAIN_COMMAND_REGEX);
        ArrayList<FrameworkCommand> commandList = new ArrayList<FrameworkCommand>();

        // add commands
        for (String command : commands) {
            commandList.add(new FuelPhpFrameworkCommand(phpModule, command, command, command));
            // add sub commands
            if (command.equals(GENERATE_COMMAND)) {
                for (String subCommand : getGenerateSubCommands(phpModule)) {
                    String fullCommand = command + " " + subCommand; // NOI18N
                    commandList.add(new FuelPhpFrameworkCommand(phpModule, new String[]{command, subCommand}, fullCommand, fullCommand));
                }
            }
        }
        return commandList;
    }

    /**
     * Get sub commands for generate command.
     *
     * @param phpModule
     * @return sub commands
     */
    private String[] getGenerateSubCommands(PhpModule phpModule) {
        return getCommands(phpModule, new String[]{GENERATE_COMMAND}, GENERATE_SUB_COMMAND_REGEX);
    }

    /**
     * Get commands from help.
     *
     * @param phpModule
     * @param commands
     * @param regex
     * @return commands
     */
    private String[] getCommands(PhpModule phpModule, String[] commands, String regex) {
        // get commands from help
        String help = getHelp(phpModule, commands);
        help = help.replaceAll("\n", " "); // NOI18N
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(help);
        String cmd = ""; // NOI18N
        if (matcher.matches()) {
            cmd = matcher.group(1);
        }
        if (StringUtils.isEmpty(cmd)) {
            return new String[]{};
        }
        // split command
        return cmd.split("\\|"); // NOI18N
    }

    /**
     * Get help.
     *
     * @param phpModule
     * @param params
     * @return help text
     */
    public String getHelp(PhpModule phpModule, String[] params) {
        assert phpModule != null;

        // no help commands
        if (params.length > 1) {
            return ""; // NOI18N
        }
        for (String param : params) {
            if (IGNORE_HELP_COMMANDS.contains(param)) {
                return ""; // NOI18N
            }
        }

        List<String> allParams = new ArrayList<String>();
        allParams.addAll(Arrays.asList(params));
        allParams.add(HELP_COMMAND);

        HelpLineProcessor lineProcessor = new HelpLineProcessor();
        Future<Integer> result = createPhpExecutable(phpModule)
                .displayName(getDisplayName(phpModule, allParams.get(0)))
                .additionalParameters(getAllParams(allParams))
                .run(getSilentDescriptor(), getOutProcessorFactory(lineProcessor));
        try {
            if (result != null) {
                result.get();
            }
        } catch (CancellationException ex) {
            // canceled
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, OPTIONS_SUB_PATH);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return lineProcessor.getHelp();
    }

    /**
     * Get all params.
     *
     * @param params
     * @return
     */
    private List<String> getAllParams(List<String> params) {
        List<String> allParams = new ArrayList<String>();
        allParams.addAll(DEFAULT_PARAMS);
        allParams.addAll(params);
        return allParams;
    }

    /**
     * Get display name.
     *
     * @param phpModule
     * @param command
     * @return
     */
    @NbBundle.Messages({
        "# {0} - project name",
        "# {1} - command",
        "Oil.command.title={0} ({1})"
    })
    private String getDisplayName(PhpModule phpModule, String command) {
        return Bundle.Oil_command_title(phpModule.getDisplayName(), command);
    }

    /**
     * Get InputProcessFactory.
     *
     * @param lineProcessor
     * @return
     */
    private ExecutionDescriptor.InputProcessorFactory getOutProcessorFactory(final LineProcessor lineProcessor) {
        return new ExecutionDescriptor.InputProcessorFactory() {
            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.ansiStripping(InputProcessors.bridge(lineProcessor));
            }
        };
    }

    /**
     * Get silent descriptor.
     *
     * @return ExecutionDescriptor
     */
    private ExecutionDescriptor getSilentDescriptor() {
        return new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL);
    }

    //~ Inner classes
    private static class HelpLineProcessor implements LineProcessor {

        private StringBuilder sb = new StringBuilder();

        @Override
        public void processLine(String line) {
            sb.append(line);
            sb.append("\n"); // NOI18N
        }

        @Override
        public void reset() {
        }

        @Override
        public void close() {
        }

        public String getHelp() {
            return sb.toString();
        }
    }
}
