package com.iceservices.copyright.music_metadata_service.controller;

import com.iceservices.copyright.music_metadata_service.dto.AliasRequestDto;
import com.iceservices.copyright.music_metadata_service.dto.ArtistUpdateDto;
import com.iceservices.copyright.music_metadata_service.dto.TrackRequestDto;
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
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataService metadataService;

    /**
     * POST /api/v1/artists/{artistId}/tracks
     */
    @PostMapping("/{artistId}/tracks")
    public ResponseEntity<Track> addTrack(
            @PathVariable UUID artistId,
            @RequestBody TrackRequestDto request) {

        Track track = metadataService.addTrack(artistId, request);

        // Best practice: Return a 201 Created with a Location header pointing to the new resource
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(track.getId())
                .toUri();

        return ResponseEntity.created(location).body(track);
    }

    /**
     * PATCH /api/v1/artists/{artistId}/name
     */
    @PatchMapping("/{artistId}/name")
    public ResponseEntity<Artist> updateArtistName(
            @PathVariable UUID artistId,
            @RequestBody ArtistUpdateDto request) {

        Artist updatedArtist = metadataService.updateArtistName(artistId, request.newPrimaryName());
        return ResponseEntity.ok(updatedArtist);
    }

    /**
     * POST /api/v1/artists/{artistId}/aliases
     */
    @PostMapping("/{artistId}/aliases")
    public ResponseEntity<Artist> addExplicitAlias(
            @PathVariable UUID artistId,
            @RequestBody AliasRequestDto request) {

        Artist updatedArtist = metadataService.addExplicitAlias(artistId, request.aliasName());
        return ResponseEntity.ok(updatedArtist);
    }

    /**
     * GET /api/v1/artists/{artistId}/tracks
     */
    @GetMapping("/{artistId}/tracks")
    public ResponseEntity<Page<Track>> getArtistTracks(
            @PathVariable UUID artistId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Track> tracks = metadataService.getArtistTracks(artistId, pageable);
        return ResponseEntity.ok(tracks);
    }

    /**
     * GET /api/v1/artists/artist-of-the-day
     * Note: We override the class-level mapping to match the specific route.
     */
    @GetMapping("/artist-of-the-day")
    public ResponseEntity<Artist> getArtistOfTheDay() {
        Artist artist = metadataService.getArtistOfTheDay();
        return ResponseEntity.ok(artist);
    }
}
