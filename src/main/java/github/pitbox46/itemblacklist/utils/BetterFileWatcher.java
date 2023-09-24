package github.pitbox46.itemblacklist.utils;

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
        super("FileWatcher thread-" + name);
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

    @Override
    public void run() {
        while (serving) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                continue;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent<Path> fileEvent = cast(event);
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

    @SuppressWarnings("unchecked")
    private <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    @Override
    public void close() throws IOException {
        watchService.close();
        serving = false;
    }
}
