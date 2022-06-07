package app.vaazar.domain.deleteAccount.boundary;

import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AutomatedDeleteTasks {

    private final DeleteAccountService deleteAccountService;
    private final Logger log;


    @Scheduled(cron = "0 0 1,2 * * *")
    public void handleAutomatedDeletes() {
        log.info("Handling automated delete jobs");
        deleteAccountService.handleDueDeletes();
    }

    public AutomatedDeleteTasks(DeleteAccountService deleteAccountService, Logger log) {
        this.deleteAccountService = deleteAccountService;
        this.log = log;
    }

}