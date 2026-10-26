package check4tech.firstdueresponseservice.Service;


import check4tech.firstdueresponseservice.security.LocationAccess;
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

        String tempPassword = generateTempPassword();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        employee.setPassword(passwordEncoder.encode(tempPassword));

        LocationAccess.requireOwnLocation(locationId);

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        employee.setLocation(location);

        Employee saved = employeeRepository.save(employee);
        sendTempPasswordEmail(saved, tempPassword);
        return saved;
    }

    private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

    private static String generateTempPassword() {
        String charset = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789!@#$%";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 14; i++) {
            sb.append(charset.charAt(SECURE_RANDOM.nextInt(charset.length())));
        }
        return sb.toString();
    }

    private void sendTempPasswordEmail(Employee employee, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(employee.getEmail());
        message.setSubject("Your account");
        message.setText("Hello " + employee.getFirstName() + ",\n\nYour username is " + employee.getUsername()
                + " and your temporary password is " + tempPassword
                + "\n\nPlease sign in and change it right away.");
        mailSender.send(message);
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
            LocationAccess.requireOwnLocation(request.getLocationId());
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
        LocationAccess.requireOwnLocation(locationId);
        if (newPassword == null || newPassword.length() < 10 || newPassword.length() > 100
                || "Welcome!".equalsIgnoreCase(newPassword)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Password must be 10 to 100 characters.");
        }
        String target = usernameOrEmail == null ? "" : usernameOrEmail.trim();
        Employee employee = employeeRepository.findByUsername(target)
                .or(() -> employeeRepository.findByUsername(target.toUpperCase()))
                .or(() -> employeeRepository.findByEmail(target))
                .filter(e -> e.getLocation() != null && locationId.equals(e.getLocation().getId()))
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Employee not found"));

        if (!LocationAccess.isAdmin() && !employee.getId().equals(LocationAccess.currentEmployeeId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Not allowed.");
        }

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

    private boolean canSeePrivate(Employee employee) {
        return LocationAccess.isAdmin() || employee.getId().equals(LocationAccess.currentEmployeeId());
    }

    public EmployeeResponseDto toResponse(Employee employee) {
        if (employee == null) return null;

        return EmployeeResponseDto.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .middleName(employee.getMiddleName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .ssn(canSeePrivate(employee) && employee.getSsn() != null ? getLast4OfSsn(employee.getSsn()) : null)
                .licenseId(canSeePrivate(employee) && employee.getLicenseId() != null ? encryptionUtil.decrypt(employee.getLicenseId()) : null)
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
        boolean known = employeeRepository.findByEmail(to)
                .filter(e -> e.getLocation() != null && e.getLocation().getId().equals(LocationAccess.currentLocationId()))
                .isPresent();
        if (!known) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Recipient must be an employee of your location.");
        }
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
