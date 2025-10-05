package check4tech.firstdueresponseservice.Service;

import check4tech.firstdueresponseservice.Dto.Request.EmployeeRequestDto;
import check4tech.firstdueresponseservice.Dto.Response.EmployeeResponseDto;
import check4tech.firstdueresponseservice.Entity.Employee;
import check4tech.firstdueresponseservice.Entity.Location;
import check4tech.firstdueresponseservice.Entity.Role;
import check4tech.firstdueresponseservice.Respository.EmployeeRepository;
import check4tech.firstdueresponseservice.Respository.LocationRepository;
import check4tech.firstdueresponseservice.util.EncryptionUtil;
import jakarta.transaction.Transactional;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final LocationRepository locationRepository;
    private final EncryptionUtil encryptionUtil;
    private final JavaMailSender mailSender;

    public EmployeeService(EmployeeRepository employeeRepository,
                           LocationRepository locationRepository,
                           EncryptionUtil encryptionUtil,
                           JavaMailSender mailSender) {
        this.employeeRepository = employeeRepository;
        this.locationRepository = locationRepository;
        this.encryptionUtil = encryptionUtil;
        this.mailSender = mailSender;
    }

    public String generateUsername(Long locationId, String firstName, String lastName) {
        String baseUsername = (firstName.charAt(0) + lastName).toUpperCase();
        String username = baseUsername;
        int counter = 1;

        while (employeeRepository.findByLocation_IdAndUsernameContainingIgnoreCase(locationId, username).isPresent()) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    public Employee buildEmployee(EmployeeRequestDto request) {
        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setUsername(request.getUsername());
        employee.setPassword(request.getPassword());
        employee.setRole(Role.valueOf(request.getRole().toUpperCase()));
        employee.setSsn(encryptionUtil.encrypt(request.getSsn()));
        employee.setLicenseId(encryptionUtil.encrypt(request.getLicenseId()));
        employee.setLicenseExpiration(request.getLicenseExpiration());
        employee.setLicenseType(request.getLicenseType());

        return employee;
    }

    public Employee createEmployee(EmployeeRequestDto request, Long locationId) {
        Employee employee = buildEmployee(request);

        String username = generateUsername(locationId, request.getFirstName(), request.getLastName());
        employee.setUsername(username);

        String defaultPassword = "Welcome!";
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        employee.setPassword(passwordEncoder.encode(defaultPassword));

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        employee.setLocation(location);

        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long locationId, EmployeeRequestDto request, Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        boolean nameChanged = false;

        if (request.getFirstName() != null && !request.getFirstName().equals(employee.getFirstName())) {
            employee.setFirstName(request.getFirstName());
            nameChanged = true;
        }
        if (request.getMiddleName() != null) {
            employee.setMiddleName(request.getMiddleName());
        }
        if (request.getLastName() != null && !request.getLastName().equals(employee.getLastName())) {
            employee.setLastName(request.getLastName());
            nameChanged = true;
        }
        if (request.getEmail() != null) {
            employee.setEmail(request.getEmail());
        }

        // Update encrypted fields
        updateEncryptedFields(employee, request);

        if (request.getLicenseType() != null) employee.setLicenseType(request.getLicenseType());
        if (request.getLicenseExpiration() != null) employee.setLicenseExpiration(request.getLicenseExpiration());
        if (request.getRole() != null) employee.setRole(Role.valueOf(request.getRole().toUpperCase()));

        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new RuntimeException("Location not found"));
            employee.setLocation(location);
        }

        // Regenerate username if first or last name changed
        if (nameChanged) {
            String newUsername = generateUsername(locationId, employee.getFirstName(), employee.getLastName());
            employee.setUsername(newUsername);
        }

        return employeeRepository.save(employee);
    }

    @Transactional
    public void updatePassword(Long locationId, String usernameOrEmail, String newPassword) {
        Employee employee = employeeRepository.findByLocation_IdAndUsernameContainingIgnoreCase(locationId, usernameOrEmail)
                .or(() -> employeeRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        employee.setPassword(passwordEncoder.encode(newPassword));

        employeeRepository.save(employee);
    }

    // Helper method
    private void updateEncryptedFields(Employee employee, EmployeeRequestDto request) {
        if (request.getSsn() != null) employee.setSsn(encryptionUtil.encrypt(request.getSsn()));
        if (request.getLicenseId() != null) employee.setLicenseId(encryptionUtil.encrypt(request.getLicenseId()));
    }

    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setIsTerminated(true);
        employeeRepository.save(employee);
    }

    //==========================
    // GET REQUESTS
    //==========================

    public String getLast4OfSsn(String encryptedSsn) {
        if (encryptedSsn == null) return null;

        String decrypted = encryptionUtil.decrypt(encryptedSsn);
        if (decrypted.length() >= 4) {
            return decrypted.substring(decrypted.length() - 4);
        }
        return decrypted;
    }

    public EmployeeResponseDto toResponse(Employee employee) {
        if (employee == null) return null;

        return EmployeeResponseDto.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .middleName(employee.getMiddleName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .ssn(employee.getSsn() != null ? getLast4OfSsn(employee.getSsn()) : null)
                .licenseId(employee.getLicenseId() != null ? encryptionUtil.decrypt(employee.getLicenseId()) : null)
                .licenseType(employee.getLicenseType())
                .licenseExpiration(employee.getLicenseExpiration())
                .build();
    }

    public List<EmployeeResponseDto> getAllEmployeesByIsNotTerminated(Long locationId) {
        return employeeRepository.findAllByLocation_IdAndIsTerminated(locationId, false)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponseDto> getAllEmployeesByIsTerminated(Long locationId) {
        return employeeRepository.findAllByLocation_IdAndIsTerminated(locationId, true)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    public EmployeeResponseDto getEmployeeById(Long locationId, Long id) {
        Employee employee = employeeRepository.findByLocation_IdAndId(locationId, id)
                .orElseThrow(() -> new RuntimeException("Employee ID not found"));

        return toResponse(employee);
    }

    public EmployeeResponseDto getEmployeeByUsernameAndLocation(Long locationId, String username) {
        Employee employee = employeeRepository.findByLocation_IdAndUsernameContainingIgnoreCase(locationId, username)
                .orElseThrow(() -> new RuntimeException("Employee username not found"));

        return toResponse(employee);
    }

    public List<EmployeeResponseDto> searchEmployee(Long locationId, String searchTerm) {
        String term = searchTerm != null ? searchTerm : "";
        List<Employee> employees = employeeRepository.searchEmployeeByLocationAndSearchTerm(locationId, term);

        return employees.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    //==========================
    // EMAIL ALERTS
    //==========================

    public void sendTestEmail(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Test Email");
        message.setText("Hello, this is a test email!");
        mailSender.send(message);
    }

    public void alertExpiringLicense() {
        LocalDate today = LocalDate.now();
        LocalDate threeMonthsFromNow = today.plusMonths(3);

        List<Employee> expiringEmployees = employeeRepository
                .findAllByLicenseExpirationBetween(today, threeMonthsFromNow);

        for (Employee employee : expiringEmployees) {
            sendExpiryEmail(employee);
        }
    }

    private void sendExpiryEmail(Employee employee) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(employee.getEmail());
        message.setSubject("License Expiry Alert");
        message.setText("Hello " + employee.getFirstName() + ",\n\n"
                + "Your license is expiring on " + employee.getLicenseExpiration() + ". "
                + "Please renew it within the next 3 months.\n\nThank you.");

        mailSender.send(message);
    }
}
