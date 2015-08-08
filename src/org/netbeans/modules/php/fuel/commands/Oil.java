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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.extexecution.base.input.LineProcessor;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.fuel.util.FuelUtils;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommand;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
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
    private static final String REFINE_COMMAND = "refine"; // NOI18N
    // default params
    private static final List<String> DEFAULT_PARAMS = Collections.emptyList();
    private static final Set<String> IGNORE_HELP_COMMANDS = new HashSet<String>();
    private static final Set<String> IGNORE_TASK_METHOD_NAMES = new HashSet<String>();
    private final String oilPath;
    private static final Logger LOGGER = Logger.getLogger(Oil.class.getName());

    static {
        // help commands
        IGNORE_HELP_COMMANDS.add(CONSOLE_COMMAND);
        IGNORE_HELP_COMMANDS.add(TEST_COMMAND);
        // task method names
        IGNORE_TASK_METHOD_NAMES.add("run"); // NOI18N
        IGNORE_TASK_METHOD_NAMES.add("__construct"); // NOI18N
        IGNORE_TASK_METHOD_NAMES.add("__call"); // NOI18N
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
            String fullCommand;
            if (command.equals(GENERATE_COMMAND)) {
                for (String subCommand : getGenerateSubCommands(phpModule)) {
                    fullCommand = command + " " + subCommand; // NOI18N
                    commandList.add(new FuelPhpFrameworkCommand(phpModule, new String[]{command, subCommand}, fullCommand, fullCommand));
                }
            } else if (command.equals(REFINE_COMMAND)) {
                List<FileObject> tasks = new LinkedList<FileObject>();
                getTasks(phpModule, tasks);
                EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
                for (FileObject task : tasks) {
                    String taskName = task.getName();
                    fullCommand = command + " " + taskName; // NOI18N
                    for (PhpClass phpClass : editorSupport.getClasses(task)) {
                        Collection<PhpClass.Method> methods = phpClass.getMethods();
                        boolean existsHelp = false;
                        for (PhpClass.Method method : methods) {
                            if (method.getName().equals("help")) { // NOI18N
                                existsHelp = true;
                                break;
                            }
                        }
                        commandList.add(new FuelPhpFrameworkCommand(phpModule, new String[]{command, taskName}, fullCommand, fullCommand, existsHelp));
                        for (PhpClass.Method method : methods) {
                            String methodName = method.getName();
                            // migrate task
                            if (taskName.equals("migrate")) { // NOI18N
                                methodName = methodName.replaceFirst("_", ""); // NOI18N
                                if (methodName.startsWith("_")) { // NOI18N
                                    continue;
                                }
                            }
                            if (IGNORE_TASK_METHOD_NAMES.contains(methodName)) {
                                continue;
                            }
                            String subCommand = taskName + ":" + methodName;  // NOI18N
                            fullCommand = command + " " + subCommand;
                            commandList.add(new FuelPhpFrameworkCommand(phpModule, new String[]{command, subCommand}, fullCommand, fullCommand, existsHelp));
                        }
                    }
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

    private void getTasks(PhpModule phpModule, List<FileObject> tasks) {
        FileObject fuelDirectory = FuelUtils.getFuelDirectory(phpModule);
        if (fuelDirectory == null) {
            return;
        }
        FileObject tasksDirectory = fuelDirectory.getFileObject("app/tasks"); // NOI18N
        FileObject coreTasksDirectory = fuelDirectory.getFileObject("core/tasks"); // NOI18N
        FileObject packagesTasksDirectory = fuelDirectory.getFileObject("packages/oil/tasks"); // NOI18N
        addTasks(tasksDirectory, tasks);
        addTasks(coreTasksDirectory, tasks);
        addTasks(packagesTasksDirectory, tasks);
    }

    /**
     * Add tasks.
     *
     * @param folder
     * @param tasks
     */
    private void addTasks(FileObject folder, List<FileObject> tasks) {
        if (folder != null && folder.isFolder()) {
            tasks.addAll(Arrays.asList(folder.getChildren()));
        }
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
            LOGGER.log(Level.WARNING, "Not found commands: {0}", help);
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
        if (params.length > 1 && !params[0].equals(REFINE_COMMAND)) {
            return ""; // NOI18N
        }
        for (String param : params) {
            if (IGNORE_HELP_COMMANDS.contains(param)) {
                return ""; // NOI18N
            }
        }

        List<String> allParams = new ArrayList<String>();
        if (params.length > 1 && params[0].equals(REFINE_COMMAND)) {
            List<String> explode = StringUtils.explode(params[1], ":"); // NOI18N
            allParams.add(REFINE_COMMAND);
            allParams.add(explode.get(0) + ":help"); // NOI18N
        } else {
            allParams.addAll(Arrays.asList(params));
            allParams.add(HELP_COMMAND);
        }

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
    private ExecutionDescriptor.InputProcessorFactory2 getOutProcessorFactory(final LineProcessor lineProcessor) {
        return new ExecutionDescriptor.InputProcessorFactory2() {
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

        private final StringBuilder sb = new StringBuilder();

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
