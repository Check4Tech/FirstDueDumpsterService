package check4tech.firstdueresponseservice.Controller;

import check4tech.firstdueresponseservice.Dto.Request.EmployeeRequestDto;
import check4tech.firstdueresponseservice.Dto.Response.EmployeeResponseDto;
import check4tech.firstdueresponseservice.Entity.Employee;
import check4tech.firstdueresponseservice.Service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // ==========================
    // CREATE
    // ==========================
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody EmployeeRequestDto request,
                                                   @RequestParam Long locationId) {
        Employee employee = employeeService.createEmployee(request, locationId);
        return ResponseEntity.ok(employee);
    }

    // ==========================
    // UPDATE
    // ==========================
    @PutMapping("/location/{locationId}/employee/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long locationId,
                                                   @PathVariable Long id,
                                                   @RequestBody EmployeeRequestDto request) {
        Employee updated = employeeService.updateEmployee(locationId, request, id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{locationId}/password")
    public String updatePassword(@PathVariable Long locationId,
                                 @RequestParam String usernameOrEmail,
                                 @RequestParam String newPassword) {
        employeeService.updatePassword(locationId, usernameOrEmail, newPassword);
        return "Password updated successfully";
    }

    // ==========================
    // DELETE (soft delete)
    // ==========================
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee terminated successfully.");
    }

    // ==========================
    // GET
    // ==========================
    @GetMapping("/location/{locationId}/employee/{id}")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(@PathVariable Long id, @PathVariable Long locationId) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id, locationId));
    }

    @GetMapping("/location/{locationId}/all")
    public ResponseEntity<List<EmployeeResponseDto>> getAllEmployees(Long locationId) {
        return ResponseEntity.ok(employeeService.getAllEmployeesByIsNotTerminated(locationId));
    }

    @GetMapping("/terminated")
    public ResponseEntity<List<EmployeeResponseDto>> getTerminatedEmployees(Long locationId) {
        return ResponseEntity.ok(employeeService.getAllEmployeesByIsTerminated(locationId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeResponseDto>> searchEmployees(
            @RequestParam Long locationId,
            @RequestParam(required = false) String searchTerm) {

        return ResponseEntity.ok(employeeService.searchEmployee(locationId, searchTerm));
    }

    // ==========================
    // EMAIL ALERTS
    // ==========================
    @PostMapping("/send-test-email")
    public ResponseEntity<String> sendTestEmail(@RequestParam String to) {
        employeeService.sendTestEmail(to);
        return ResponseEntity.ok("Test email sent to " + to);
    }

    @PostMapping("/alert-expiring-license")
    public ResponseEntity<String> alertExpiringLicense() {
        employeeService.alertExpiringLicense();
        return ResponseEntity.ok("License expiry alert emails sent.");
    }
}
