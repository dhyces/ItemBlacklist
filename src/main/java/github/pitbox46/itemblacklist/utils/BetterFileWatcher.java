package github.pitbox46.itemblacklist.utils;

import github.pitbox46.itemblacklist.ItemBlacklist;

import java.io.IOException;
import java.nio.file.*;
import java.util.function.Consumer;

public class BetterFileWatcher extends Thread implements AutoCloseable {
    private final Path file;
    private final Consumer<Path> onFileRemoved;
    private final Consumer<Path> onFileModified;
    private WatchService watchService;
    private boolean serving;

    public BetterFileWatcher(Path file, Consumer<Path> onFileRemoved, Consumer<Path> onFileModified, String name) {
        super("FileWatcher thread - " + name);
        setDaemon(true);
        this.file = file;
        this.onFileRemoved = onFileRemoved;
        this.onFileModified = onFileModified;
    }

    @Override
    public synchronized void start() {
        try {
            watchService = file.getFileSystem().newWatchService();
            file.getParent().register(watchService, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
            serving = true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to start file watcher. ", e);
        }
        super.start();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        while (serving) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                continue;
            } catch (ClosedWatchServiceException ignored) {
                serving = false;
                ItemBlacklist.LOGGER.info("Watch service closed, stopping file watcher");
                continue;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent<Path> fileEvent = (WatchEvent<Path>) event;
                if (!fileEvent.context().equals(file)) {
                    continue;
                }
                if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY || event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    onFileModified.accept(file);
                } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    onFileRemoved.accept(file);
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        watchService.close();
        serving = false;
    }
}
