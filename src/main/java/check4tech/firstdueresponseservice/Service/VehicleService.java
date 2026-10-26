package check4tech.firstdueresponseservice.Service;


import check4tech.firstdueresponseservice.security.LocationAccess;
import check4tech.firstdueresponseservice.Dto.Request.VehicleRequestDto;
import check4tech.firstdueresponseservice.Dto.Response.VehicleResponseDto;
import check4tech.firstdueresponseservice.Entity.Location;
import check4tech.firstdueresponseservice.Entity.Vehicle;
import check4tech.firstdueresponseservice.Respository.LocationRepository;
import check4tech.firstdueresponseservice.Respository.VehicleRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final LocationRepository locationRepository;
    private final JavaMailSender mailSender;


    public VehicleService(VehicleRepository vehicleRepository, LocationRepository locationRepository, JavaMailSender mailSender) {
        this.vehicleRepository = vehicleRepository;
        this.locationRepository = locationRepository;
        this.mailSender = mailSender;
    }

    public Vehicle buildVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setMake(vehicle.getMake());
        vehicle.setModel(vehicle.getModel());
        vehicle.setDmvInspection(vehicle.getDmvInspection());
        vehicle.setRegistration(vehicle.getRegistration());
        vehicle.setVehicleYear(vehicle.getVehicleYear());
        vehicle.setLicensePlate(vehicle.getLicensePlate());
        vehicle.setVinNumber(vehicle.getVinNumber());
        vehicle.setMileage(vehicle.getMileage());

        return vehicle;
    }

    public Vehicle createVehicle(VehicleRequestDto request) {
        Vehicle vehicle = buildVehicle();

        LocationAccess.requireOwnLocation(request.getLocationId());

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));

        vehicle.setLocation(location);

        return vehicleRepository.save(vehicle);
    }

    public Vehicle updateVehicle(VehicleRequestDto request, Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setDmvInspection(request.getDmvInspection());
        vehicle.setRegistration(request.getRegistration());
        vehicle.setVehicleYear(request.getVehicleYear());
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setVinNumber(request.getVinNumber());
        vehicle.setMileage(request.getMileage());

        if (request.getLocationId() != null) {
            LocationAccess.requireOwnLocation(request.getLocationId());
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new RuntimeException("Location not found"));

            vehicle.setLocation(location);
        }

        return vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }

    // =======================
    // GET METHODS
    // =======================

    public VehicleResponseDto toResponse(Vehicle vehicle) {
        return VehicleResponseDto.builder()
                .id(vehicle.getId())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .dmvInspection(vehicle.getDmvInspection())
                .registration(vehicle.getRegistration())
                .vehicleYear(vehicle.getVehicleYear())
                .licensePlate(vehicle.getLicensePlate())
                .isCurrentlyOnJob(vehicle.getIsCurrentlyOnJob())
                .vinNumber(vehicle.getVinNumber())
                .mileage(vehicle.getMileage())
                .build();
    }

    public List<VehicleResponseDto> getAllVehicles() {
        return vehicleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<VehicleResponseDto> getVehiclesByLocationIdAndIsCurrentlyOnJob(Long locationId) {
       locationRepository.findById(locationId)
               .orElseThrow(() -> new RuntimeException("Location not found"));

       List<Vehicle> vehicle = vehicleRepository.findByLocation_IdAndIsCurrentlyOnJobOrderByVehicleYearDesc(locationId, true);

        return vehicle.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<VehicleResponseDto> getVehiclesByLocationIdAndIsNotCurrentlyOnJob(Long locationId) {
        locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        List<Vehicle> vehicle = vehicleRepository.findByLocation_IdAndIsCurrentlyOnJobOrderByVehicleYearDesc(locationId, false);

        return vehicle.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public VehicleResponseDto getVehicleById(Long id, Long locationId) {
        return toResponse(vehicleRepository.findByLocation_IdAndId(id, locationId));
    }

    public List<VehicleResponseDto> searchVehiclesByLocationAndSearchTerm(Long locationId, String searchTerm) {
        String term = searchTerm != null ? searchTerm : "";

        List<Vehicle> vehicles = vehicleRepository.searchByLocationAndVinOrLicensePlate(locationId, term);

        return vehicles.stream()
                .map(this::toResponse) // map to VehicleResponseDto
                .collect(Collectors.toList());
    }

}
