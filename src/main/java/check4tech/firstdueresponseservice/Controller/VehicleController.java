package check4tech.firstdueresponseservice.Controller;


import org.springframework.security.access.prepost.PreAuthorize;
import check4tech.firstdueresponseservice.Dto.Request.VehicleRequestDto;
import check4tech.firstdueresponseservice.Dto.Response.VehicleResponseDto;
import check4tech.firstdueresponseservice.Service.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // =======================
    // CREATE / UPDATE / DELETE
    // =======================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<VehicleResponseDto> createVehicle(@RequestBody VehicleRequestDto request) {
        VehicleResponseDto response = vehicleService.toResponse(vehicleService.createVehicle(request));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> updateVehicle(@PathVariable Long id,
                                                            @RequestBody VehicleRequestDto request) {
        VehicleResponseDto response = vehicleService.toResponse(vehicleService.updateVehicle(request, id));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    // =======================
    // GET REQUESTS
    // =======================

    @GetMapping
    public ResponseEntity<List<VehicleResponseDto>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @GetMapping("/location/{locationId}/unavailable")
    public ResponseEntity<List<VehicleResponseDto>> getVehiclesByLocationAndUnavailable(@PathVariable Long locationId) {
        return ResponseEntity.ok(vehicleService.getVehiclesByLocationIdAndIsCurrentlyOnJob(locationId));
    }

    @GetMapping("/location/{locationId}/available")
    public ResponseEntity<List<VehicleResponseDto>> getVehiclesByLocationAndAvailable(@PathVariable Long locationId) {
        return ResponseEntity.ok(vehicleService.getVehiclesByLocationIdAndIsCurrentlyOnJob(locationId));
    }

    @GetMapping("/{id}/location/{locationId}")
    public ResponseEntity<VehicleResponseDto> getVehicleById(@PathVariable Long id,
                                                             @PathVariable Long locationId) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id, locationId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<VehicleResponseDto>> searchVehicles(@RequestParam Long locationId,
                                                                   @RequestParam(required = false) String searchTerm) {
        List<VehicleResponseDto> vehicles = vehicleService.searchVehiclesByLocationAndSearchTerm(locationId, searchTerm);
        return ResponseEntity.ok(vehicles);
    }
}
