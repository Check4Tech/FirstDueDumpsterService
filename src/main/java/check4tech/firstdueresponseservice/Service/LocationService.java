package check4tech.firstdueresponseservice.Service;

import check4tech.firstdueresponseservice.Dto.Request.LocationRequestDto;
import check4tech.firstdueresponseservice.Dto.Response.LocationResponseDto;
import check4tech.firstdueresponseservice.Entity.Location;
import check4tech.firstdueresponseservice.Respository.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location buildLocation(LocationRequestDto request) {
        Location location = new Location();
        location.setName(request.getName());
        location.setAddress(request.getAddress());
        location.setState(request.getState());
        location.setZipCode(request.getZipCode());

        return location;
    }

    public Location createLocation(LocationRequestDto request) {
        Location location = buildLocation(request);
        return locationRepository.save(location);
    }

    public Location updateLocation(LocationRequestDto request, Long id) {

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        location.setName(request.getName());
        location.setAddress(request.getAddress());
        location.setState(request.getState());
        location.setZipCode(request.getZipCode());

        return locationRepository.save(location);
    }

    public void deleteLocation(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        location.setInBusiness(true); //set business to inactive
        locationRepository.save(location);
    }

    //====================================
    // GET REQUESTS
    //====================================

    private LocationResponseDto toResponse(Location location) {
        return LocationResponseDto.builder()
                .id(location.getId())
                .name(location.getName())
                .address(location.getAddress())
                .state(location.getState())
                .zipCode(location.getZipCode())
                .build();
    }

    public List<LocationResponseDto> getAllLocations() {
        return locationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public LocationResponseDto findLocationById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        return toResponse(location);
    }

}
