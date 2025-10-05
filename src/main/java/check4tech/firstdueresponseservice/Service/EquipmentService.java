package check4tech.firstdueresponseservice.Service;

import check4tech.firstdueresponseservice.Dto.Request.EquipmentRequestDto;
import check4tech.firstdueresponseservice.Dto.Response.EquipmentResponseDto;
import check4tech.firstdueresponseservice.Entity.Equipment;
import check4tech.firstdueresponseservice.Entity.Location;
import check4tech.firstdueresponseservice.Respository.EquipmentRepository;
import check4tech.firstdueresponseservice.Respository.LocationRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final LocationRepository locationRepository;
    private final JavaMailSender mailSender;

    public EquipmentService(EquipmentRepository equipmentRepository, LocationRepository locationRepository, JavaMailSender mailSender) {
        this.equipmentRepository = equipmentRepository;
        this.locationRepository = locationRepository;
        this.mailSender = mailSender;
    }

    public Equipment buildEquipment(EquipmentRequestDto request) {
        Equipment equipment = new Equipment();
        equipment.setName(request.getName());
        equipment.setSerialNumber(request.getSerialNumber());
        equipment.setWarrantyExpiration(request.getWarrantyExpiration());
        equipment.setQuantity(request.getQuantity());
        equipment.setCost(request.getCost());
        equipment.setRestockAlert(request.getRestockAlert());

        return equipment;
    }

    public Equipment createEquipment(EquipmentRequestDto request) {
        Equipment equipment = buildEquipment(request);

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));

        equipment.setLocation(location);

        return equipmentRepository.save(equipment);
    }

    public Equipment updateEquipment(EquipmentRequestDto request, Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        equipment.setName(request.getName());
        equipment.setSerialNumber(request.getSerialNumber());
        equipment.setWarrantyExpiration(request.getWarrantyExpiration());
        equipment.setQuantity(request.getQuantity());
        equipment.setCost(request.getCost());
        equipment.setRestockAlert(request.getRestockAlert());

        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new RuntimeException("Location not found"));
            equipment.setLocation(location);
        }

        return equipmentRepository.save(equipment);
    }

    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }

    //========================
    // GET METHODS
    //=======================

    public EquipmentResponseDto toResponse(Equipment equipment) {
        return EquipmentResponseDto.builder()
                .id(equipment.getId())
                .name(equipment.getName())
                .serialNumber(equipment.getSerialNumber())
                .warrantyExpiration(equipment.getWarrantyExpiration())
                .quantity(equipment.getQuantity())
                .cost(equipment.getCost())
                .restockAlert(equipment.getRestockAlert())
                .build();
    }

    public List<EquipmentResponseDto> getAllEquipment(Long locationId) {
        return equipmentRepository.findByLocation_IdOrderByNameAsc(locationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

    }

    public List<EquipmentResponseDto> getEquipmentByLocationId(Long locationId) {
        locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        List<Equipment> equipment = equipmentRepository.findByLocation_IdOrderByNameAsc(locationId);

        return equipment.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EquipmentResponseDto getEquipmentById(Long id, Long locationId) {
        return toResponse(equipmentRepository.findByIdAndLocation_Id(id, locationId));
    }

    public List<EquipmentResponseDto> searchByNameAndSerialNumber(Long locationId, String searchTerm) {
        String term = searchTerm != null ? searchTerm : "";

        List<Equipment> equipment = equipmentRepository.searchByLocationAndNameOrSerial(locationId, term);

        return equipment.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    //TODO Add flagging for restock depending on conversation with client
}
