package check4tech.firstdueresponseservice.Controller;


import org.springframework.security.access.prepost.PreAuthorize;
import check4tech.firstdueresponseservice.Dto.Request.TrailerRequestDto;
import check4tech.firstdueresponseservice.Dto.Response.TrailerResponseDto;
import check4tech.firstdueresponseservice.Service.TrailerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trailer")
public class TrailerController {

    private final TrailerService trailerService;

    public TrailerController(TrailerService trailerService) {
        this.trailerService = trailerService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{locationId}")
    public ResponseEntity<TrailerResponseDto> createTrailer(@RequestBody TrailerRequestDto request,
                                                            @PathVariable Long locationId) {
        TrailerResponseDto response = trailerService.toResponse(
                trailerService.createTrailer(request, locationId)
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TrailerResponseDto> updateTrailer(@RequestBody TrailerRequestDto request,
                                                            @PathVariable Long id) {
        TrailerResponseDto response = trailerService.toResponse(
                trailerService.updateTrailer(request, id)
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrailer(@PathVariable Long id) {
        trailerService.deleteTrailer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<TrailerResponseDto>> getAllTrailers() {
        return ResponseEntity.ok(trailerService.getAllTrailers());
    }

    @GetMapping("/{trailerId}/location/{locationId}")
    public ResponseEntity<TrailerResponseDto> getTrailerById(@PathVariable Long trailerId,
                                                             @PathVariable Long locationId) {
        return ResponseEntity.ok(trailerService.getTrailerById(trailerId, locationId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TrailerResponseDto>> searchTrailers(
            @RequestParam Long locationId,
            @RequestParam(required = false) String searchTerm) {

        List<TrailerResponseDto> trailers = trailerService.searchTrailerBySearchTerm(locationId, searchTerm);
        return ResponseEntity.ok(trailers);
    }

    @GetMapping("/search/size")
    public ResponseEntity<List<TrailerResponseDto>> getTrailersBySizeCategory(@RequestParam String category) {
        return ResponseEntity.ok(trailerService.getTrailersBySizeCategory(category));
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<TrailerResponseDto>> getTrailersByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(trailerService.getTrailersByLocationId(locationId));
    }
}

