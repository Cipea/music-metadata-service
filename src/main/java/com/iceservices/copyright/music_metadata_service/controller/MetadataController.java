package com.iceservices.copyright.music_metadata_service.controller;

import com.iceservices.copyright.music_metadata_service.dto.*;
import com.iceservices.copyright.music_metadata_service.entity.Artist;
import com.iceservices.copyright.music_metadata_service.entity.Track;
import com.iceservices.copyright.music_metadata_service.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/artist")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataService metadataService;

    /**
     * POST /api/v1/artist/{artistId}/tracks
     */
    @PostMapping("/{artistId}/tracks")
    public ResponseEntity<TrackResponseDto> addTrack(
            @PathVariable UUID artistId,
            @RequestBody TrackRequestDto request) {

        Track track = metadataService.addTrack(artistId, request);

        // Best practice: Return a 201 Created with a Location header pointing to the new resource
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(track.getId())
                .toUri();

        return ResponseEntity.created(location).body(TrackResponseDto.fromEntity(track));
    }

    /**
     * PATCH /api/v1/artist/{artistId}/name
     */
    @PatchMapping("/{artistId}/name")
    public ResponseEntity<ArtistResponseDto> updateArtistName(
            @PathVariable UUID artistId,
            @RequestBody ArtistUpdateDto request) {

        Artist updatedArtist = metadataService.updateArtistName(artistId, request.newPrimaryName());
        return ResponseEntity.ok(ArtistResponseDto.fromEntity(updatedArtist));
    }

    /**
     * POST /api/v1/artist/{artistId}/aliases
     */
    @PostMapping("/{artistId}/aliases")
    public ResponseEntity<ArtistResponseDto> addExplicitAlias(
            @PathVariable UUID artistId,
            @RequestBody AliasRequestDto request) {

        Artist updatedArtist = metadataService.addExplicitAlias(artistId, request.aliasName());
        return ResponseEntity.ok(ArtistResponseDto.fromEntity(updatedArtist));
    }

    /**
     * GET /api/v1/artist/{artistId}/tracks
     */
    @GetMapping("/{artistId}/tracks")
    public ResponseEntity<Page<TrackResponseDto>> getArtistTracks(
            @PathVariable UUID artistId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<TrackResponseDto> tracks = metadataService.getArtistTracks(artistId, pageable);
        return ResponseEntity.ok(tracks);
    }

    /**
     * GET /api/v1/artist/artist-of-the-day
     * Note: We override the class-level mapping to match the specific route.
     */
    @GetMapping("/artist-of-the-day")
    public ResponseEntity<ArtistResponseDto> getArtistOfTheDay() {
        ArtistResponseDto artist = metadataService.getArtistOfTheDay();
        return ResponseEntity.ok(artist);
    }
}
