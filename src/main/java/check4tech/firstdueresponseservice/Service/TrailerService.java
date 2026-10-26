package check4tech.firstdueresponseservice.Service;


import check4tech.firstdueresponseservice.security.LocationAccess;
import check4tech.firstdueresponseservice.Dto.Request.TrailerRequestDto;
import check4tech.firstdueresponseservice.Dto.Response.TrailerResponseDto;
import check4tech.firstdueresponseservice.Entity.Location;
import check4tech.firstdueresponseservice.Entity.Trailer;
import check4tech.firstdueresponseservice.Respository.LocationRepository;
import check4tech.firstdueresponseservice.Respository.TrailerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrailerService {

    private final TrailerRepository trailerRepository;
    private final LocationRepository locationRepository;

    public TrailerService(TrailerRepository trailerRepository,
                          LocationRepository locationRepository) {
        this.trailerRepository = trailerRepository;
        this.locationRepository = locationRepository;
    }

    public Trailer buildTrailer(TrailerRequestDto request) {
        Trailer trailer = new Trailer();
        trailer.setMake(request.getMake());
        trailer.setModel(request.getModel());
        trailer.setDmvInspection(request.getDmvInspection());
        trailer.setRegistration(request.getRegistration());
        trailer.setTrailerYear(request.getTrailerYear());
        trailer.setLicensePlate(request.getLicensePlate());
        trailer.setVinNumber(request.getVinNumber());

        return trailer;
    }

    public Trailer createTrailer(TrailerRequestDto request, Long locationId) {
        Trailer trailer = buildTrailer(request);

        LocationAccess.requireOwnLocation(locationId);

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        trailer.setLocation(location);

        return trailerRepository.save(trailer);
    }

    public Trailer updateTrailer(TrailerRequestDto request, Long id) {
        Trailer trailer = trailerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trailer not found"));

        trailer.setMake(request.getMake());
        trailer.setModel(request.getModel());
        trailer.setDmvInspection(request.getDmvInspection());
        trailer.setRegistration(request.getRegistration());
        trailer.setTrailerYear(request.getTrailerYear());
        trailer.setLicensePlate(request.getLicensePlate());
        trailer.setVinNumber(request.getVinNumber());

        if (request.getLocationId() != null) {
            LocationAccess.requireOwnLocation(request.getLocationId());
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new RuntimeException("Location not found"));
            trailer.setLocation(location);
        }

        return trailerRepository.save(trailer);
    }

    public void deleteTrailer(Long id) {
        trailerRepository.deleteById(id);
    }

    //========================
    // GET REQUESTS
    //=======================

    public TrailerResponseDto toResponse(Trailer trailer) {
        return TrailerResponseDto.builder()
                .id(trailer.getId())
                .make(trailer.getMake())
                .model(trailer.getModel())
                .dmvInspection(trailer.getDmvInspection())
                .registration(trailer.getRegistration())
                .isCurrentlyOnJob(trailer.getIsCurrentlyOnJob())
                .trailerYear(trailer.getTrailerYear())
                .licensePlate(trailer.getLicensePlate())
                .vinNumber(trailer.getVinNumber())
                .size(trailer.getSize())
                .build();
    }

    public List<TrailerResponseDto> getAllTrailers() {
        return trailerRepository.findAllByOrderBySizeAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TrailerResponseDto> getTrailersByLocationId(Long locationId) {
        locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        List<Trailer> trailers = trailerRepository.findAllByLocation_IdOrderBySizeAsc(locationId);

        return trailers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TrailerResponseDto getTrailerById(Long trailerId, Long locationId) {
        Trailer trailer = trailerRepository.findByIdAndLocation_Id(trailerId, locationId)
                .orElseThrow(() -> new RuntimeException(
                        "Trailer has trailer ID of: " + trailerId +
                                " Location has ID of: " + locationId)
                );

        return toResponse(trailer);
    }

    //======================
    // SEARCH
    //======================

    public List<TrailerResponseDto> searchTrailerBySearchTerm(Long locationId, String searchTerm) {
        String term = searchTerm != null ? searchTerm : "";

        List<Trailer> trailers = trailerRepository.searchByLocationAndSearchTerm(locationId, term);

        if (trailers.isEmpty()) {
            throw new RuntimeException(
                    "No trailers found matching: " + searchTerm + " in location ID: " + locationId
            );
        }

        return trailers.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<TrailerResponseDto> getTrailersBySizeCategory(String category) {
        int min;
        int max;

        switch (category.toLowerCase()) {
            case "small":
                min = 0;
                max = 5;
                break;
            case "medium":
                min = 6;
                max = 10;
                break;
            case "large":
                min = 11;
                max = 15;
                break;
            case "xl":
                min = 16;
                max = 20;
                break;
            case "xxl":
                min = 21;
                max = 30;
                break;
            case "xxxl":
                min = 31;
                max = 40;
                break;
            default:
                throw new IllegalArgumentException("Unknown size category: " + category);
        }

        return trailerRepository.findBySizeBetween(min, max)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

}
