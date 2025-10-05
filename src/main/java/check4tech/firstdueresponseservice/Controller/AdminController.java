package check4tech.firstdueresponseservice.Controller;

import check4tech.firstdueresponseservice.Dto.Request.EmployeeRequestDto;
import check4tech.firstdueresponseservice.Dto.Response.EmployeeResponseDto;
import check4tech.firstdueresponseservice.Entity.Employee;
import check4tech.firstdueresponseservice.Service.EmployeeService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final EmployeeService employeeService;

    public AdminController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/employees/{locationId}")
    public List<EmployeeResponseDto> getAllEmployeesNotTerminated(@PathVariable Long locationId) {
        return employeeService.getAllEmployeesByIsNotTerminated(locationId);
    }

    @GetMapping("/employees/{locationId}/terminated")
    public List<EmployeeResponseDto> getAllEmployeesTerminated(@PathVariable Long locationId) {
        return employeeService.getAllEmployeesByIsNotTerminated(locationId);
    }

    // Example: Create employee
    @PostMapping("/employees")
    public Employee createEmployee(@RequestBody EmployeeRequestDto request, @RequestParam Long locationId) {
        return employeeService.createEmployee(request, locationId);
    }

    // Example: Update employee
    @PutMapping("/location/{locationId}/employees/{id}")
    public Employee updateEmployee(@PathVariable Long locationId, @PathVariable Long id, @RequestBody EmployeeRequestDto request) {
        return employeeService.updateEmployee(locationId, request, id);
    }

    // Example: Admin dashboard
    @GetMapping("/dashboard")
    public String dashboard() {
        return "Welcome to the admin dashboard!";
    }
}

