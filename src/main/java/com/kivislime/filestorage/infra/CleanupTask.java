package com.kivislime.filestorage.infra;

import com.kivislime.filestorage.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CleanupTask {
    private final FileService fileService;

    @Scheduled(cron = "0 0 */6 * * *")
    public void cleanup() {
        fileService.cleanupOrphanFiles();
    }
}
