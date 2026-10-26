package check4tech.firstdueresponseservice.Controller;


import org.springframework.security.access.prepost.PreAuthorize;
import check4tech.firstdueresponseservice.Dto.Request.EquipmentRequestDto;
import check4tech.firstdueresponseservice.Dto.Response.EquipmentResponseDto;
import check4tech.firstdueresponseservice.Service.EquipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EquipmentResponseDto> createEquipment(@RequestBody EquipmentRequestDto request) {
        return ResponseEntity.ok(equipmentService.toResponse(equipmentService.createEquipment(request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EquipmentResponseDto> updateEquipment(@PathVariable Long id,
                                                                @RequestBody EquipmentRequestDto request) {
        return ResponseEntity.ok(equipmentService.toResponse(equipmentService.updateEquipment(request, id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }

    // ====================
    // GET REQUESTS
    // ====================

    @GetMapping
    public ResponseEntity<List<EquipmentResponseDto>> getAllEquipment(@RequestParam Long locationId) {
        return ResponseEntity.ok(equipmentService.getAllEquipment(locationId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponseDto> getEquipmentById(@PathVariable Long id,
                                                                 @RequestParam Long locationId) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id, locationId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EquipmentResponseDto>> searchByNameAndSerialNumber(
            @RequestParam Long locationId,
            @RequestParam(required = false) String searchTerm) {
        return ResponseEntity.ok(equipmentService.searchByNameAndSerialNumber(locationId, searchTerm));
    }
}

