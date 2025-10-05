package check4tech.firstdueresponseservice.Service.Scheduler;

import check4tech.firstdueresponseservice.Service.EmployeeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LicenseAlertScheduler {

    private final EmployeeService employeeService;

    public LicenseAlertScheduler(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // Run every day at 8 AM
    @Scheduled(cron = "0 0 8 * * ?")
    public void runDailyLicenseAlerts() {
        employeeService.alertExpiringLicense();
    }
}

