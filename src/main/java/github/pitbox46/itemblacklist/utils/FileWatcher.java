package github.pitbox46.itemblacklist.utils;

import github.pitbox46.itemblacklist.ItemBlacklist;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.function.Consumer;

public class FileWatcher extends Thread {
    private final Object BLOCK = new Object();
    private final Path path;
    private final Consumer<Path> onFileRemoved;
    private boolean run = true;
    private boolean hasFileChanged;
    private FileTime lastTime;

    public FileWatcher(Path path, Consumer<Path> onFileRemoved, @NotNull String name) {
        super("Watcher thread-" + name);
        this.path = path;
        this.onFileRemoved = onFileRemoved;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (run) {
            synchronized (BLOCK) {
                try {
                    if (Files.exists(path)) {
                        FileTime time = Files.getLastModifiedTime(path);
                        if (!time.equals(lastTime)) {
                            lastTime = time;
                            ItemBlacklist.LOGGER.info("File modified");
                            hasFileChanged = true;
                        }
                    } else {
                        onFileRemoved.accept(path);
                    }
                } catch (NoSuchFileException | AccessDeniedException | FileAlreadyExistsException ignored) { // Bah humbug, just ignore it
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized boolean hasFileChanged() {
        return hasFileChanged;
    }

    public synchronized void close() {
        this.run = false;
    }
}
