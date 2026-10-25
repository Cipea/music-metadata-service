package com.iceservices.copyright.music_metadata_service.controller;

import com.iceservices.copyright.music_metadata_service.dto.*;
import com.iceservices.copyright.music_metadata_service.entity.Artist;
import com.iceservices.copyright.music_metadata_service.entity.ArtistAlias;
import com.iceservices.copyright.music_metadata_service.entity.Track;
import com.iceservices.copyright.music_metadata_service.service.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/artist")
@RequiredArgsConstructor
@Slf4j
public class MetadataController {

    private final MetadataService metadataService;

    /**
     * POST /api/v1/artist/{artistId}/tracks
     */
    @PostMapping("/{artistId}/tracks")
    public ResponseEntity<TrackResponseDto> addTrack(
            @PathVariable UUID artistId,
            @RequestBody TrackRequestDto request) {

        log.info("Received request for adding a new track for artist id: {}", artistId);

        Track track = metadataService.addTrack(artistId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(TrackResponseDto.fromEntity(track));
    }

    /**
     * PATCH /api/v1/artist/{artistId}/name
     */
    @PatchMapping("/{artistId}/name")
    public ResponseEntity<ArtistResponseDto> updateArtistName(
            @PathVariable UUID artistId,
            @RequestBody ArtistUpdateDto request) {

        log.info("Received request for updating the name for artist id: {}", artistId);

        Artist updatedArtist = metadataService.updateArtistName(artistId, request.newPrimaryName());
        return ResponseEntity.ok(ArtistResponseDto.fromEntity(updatedArtist));
    }

    /**
     * POST /api/v1/artist/{artistId}/aliases
     */
    @PostMapping("/{artistId}/aliases")
    public ResponseEntity<ArtistAliasRespDTO> addExplicitAlias(
            @PathVariable UUID artistId,
            @RequestBody AliasRequestDto request) {

        log.info("Received request for adding a new alias for artist id: {}", artistId);

        ArtistAlias artistAlias = metadataService.addExplicitAlias(artistId, request.aliasName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ArtistAliasRespDTO.fromEntity(artistAlias));
    }

    /**
     * GET /api/v1/artist/{artistId}/tracks
     */
    @GetMapping("/{artistId}/tracks")
    public ResponseEntity<Page<TrackResponseDto>> getArtistTracks(
            @PathVariable UUID artistId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Received request for retrieving the tracks for artist id: {}", artistId);

        Page<TrackResponseDto> tracks = metadataService.getArtistTracks(artistId, pageable);
        return ResponseEntity.ok(tracks);
    }

    /**
     * GET /api/v1/artist/artist-of-the-day
     * Note: We override the class-level mapping to match the specific route.
     */
    @GetMapping("/artist-of-the-day")
    public ResponseEntity<ArtistResponseDto> getArtistOfTheDay() {

        log.info("Received request for retrieving the artist of the day");

        ArtistResponseDto artist = metadataService.getArtistOfTheDay();
        return ResponseEntity.ok(artist);
    }
}
