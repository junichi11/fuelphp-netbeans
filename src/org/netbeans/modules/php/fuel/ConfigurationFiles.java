/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.php.fuel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.fuel.preferences.FuelPhpPreferences;
import org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

/**
 *
 * @author junichi11
 */
public final class ConfigurationFiles extends FileChangeAdapter implements ImportantFilesImplementation {

    private final PhpModule phpModule;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // @GuardedBy("this")
    private FileObject sourceDirectory = null;

    public ConfigurationFiles(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    @Override
    public Collection<FileInfo> getFiles() {
        FileObject sourceDir = getSourceDirectory();
        if (sourceDir == null) {
            return Collections.emptyList();
        }
        List<FileInfo> files = new ArrayList<FileInfo>();
        String configPath = FuelPhpPreferences.getFuelName(phpModule) + "/app/config"; // NOI18N
        FileObject config = sourceDir.getFileObject(configPath);
        if (config != null) {
            addFileInfo(files, config);
        }
        return files;
    }

    private void addFileInfo(List<FileInfo> files, FileObject root) {
        if (root == null) {
            return;
        }
        if (root.isFolder()) {
            for (FileObject child : root.getChildren()) {
                if (child.isFolder()) {
                    addFileInfo(files, child);
                } else {
                    files.add(new FileInfo(child));
                }
            }
        } else {
            files.add(new FileInfo(root));
        }
    }

    @CheckForNull
    private synchronized FileObject getSourceDirectory() {
        if (sourceDirectory == null) {
            sourceDirectory = phpModule.getSourceDirectory();
            if (sourceDirectory != null) {
                String configPath = FuelPhpPreferences.getFuelName(phpModule) + "/app/config"; // NOI18N
                File source = FileUtil.toFile(sourceDirectory);
                addListener(new File(source, configPath));
            }
        }
        return sourceDirectory;
    }

    private void addListener(File path) {
        try {
            FileUtil.addRecursiveListener(this, path);
        } catch (IllegalArgumentException ex) {
            // noop, already listening
            assert false : path;
        }
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private void fireChange() {
        changeSupport.fireChange();
    }

    //~ FS
    @Override
    public void fileRenamed(FileRenameEvent fe) {
        fireChange();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        fireChange();
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        fireChange();
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        fireChange();
    }

}
