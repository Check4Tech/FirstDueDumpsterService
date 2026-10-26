package check4tech.firstdueresponseservice.Controller;



import check4tech.firstdueresponseservice.security.LocationAccess;
import org.springframework.security.access.prepost.PreAuthorize;
import check4tech.firstdueresponseservice.Dto.Request.LocationRequestDto;
import check4tech.firstdueresponseservice.Dto.Response.LocationResponseDto;
import check4tech.firstdueresponseservice.Service.LocationService;
import check4tech.firstdueresponseservice.Entity.Location;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<LocationResponseDto> createLocation(@RequestBody LocationRequestDto request) {
        Location savedLocation = locationService.createLocation(request);
        LocationResponseDto response = LocationResponseDto.builder()
                .id(savedLocation.getId())
                .name(savedLocation.getName())
                .address(savedLocation.getAddress())
                .state(savedLocation.getState())
                .zipCode(savedLocation.getZipCode())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<LocationResponseDto> updateLocation(
            @PathVariable Long id,
            @RequestBody LocationRequestDto request) {

        LocationAccess.requireOwnLocation(id);
        Location updatedLocation = locationService.updateLocation(request, id);
        LocationResponseDto response = LocationResponseDto.builder()
                .id(updatedLocation.getId())
                .name(updatedLocation.getName())
                .address(updatedLocation.getAddress())
                .state(updatedLocation.getState())
                .zipCode(updatedLocation.getZipCode())
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        LocationAccess.requireOwnLocation(id);
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    // Get all locations
    @GetMapping
    public ResponseEntity<List<LocationResponseDto>> getAllLocations() {
        List<LocationResponseDto> locations = locationService.getAllLocations().stream()
                .filter(l -> l.getId() != null && l.getId().equals(LocationAccess.currentLocationId()))
                .toList();
        return ResponseEntity.ok(locations);
    }

    // Get a single location by ID
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponseDto> getLocationById(@PathVariable Long id) {
        LocationAccess.requireOwnLocation(id);
        LocationResponseDto location = locationService.findLocationById(id);
        return ResponseEntity.ok(location);
    }
}
