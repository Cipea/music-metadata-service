package com.iceservices.copyright.music_metadata_service.service;

import com.iceservices.copyright.music_metadata_service.dto.*;
import com.iceservices.copyright.music_metadata_service.entity.Artist;
import com.iceservices.copyright.music_metadata_service.entity.ArtistAlias;
import com.iceservices.copyright.music_metadata_service.entity.Track;
import com.iceservices.copyright.music_metadata_service.repository.ArtistAliasRepository;
import com.iceservices.copyright.music_metadata_service.repository.ArtistRepository;
import com.iceservices.copyright.music_metadata_service.repository.TrackRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetadataService {

    private final ArtistRepository artistRepository;
    private final TrackRepository trackRepository;
    private final ArtistAliasRepository artistAliasRepository;

    /**
     * POST /api/v1/artist/{artistId}/tracks
     * Appends a new track and evicts the track cache for this artist.
     */
    @Transactional
    @CacheEvict(value = "artist-tracks", key = "#artistId")
    public Track addTrack(UUID artistId, TrackRequestDto request) {
        log.info("Adding new track '{}' for artist ID: {}", request.title(), artistId);

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        Track track = Track.builder()
                .artist(artist)
                .title(request.title())
                .lengthSeconds(request.durationSeconds())
                .genre(request.genre())
                .releaseDate(request.releaseDate())
                .build();

        return trackRepository.save(track);
    }

    /**
     * POST /api/v1/artist/{artistId}/tracks
     * Appends a new track and evicts the track cache for this artist.
     */
    @Transactional
    @CacheEvict(value = "artist-tracks", key = "#artistId")
    public Artist addExplicitAlias(UUID artistId, String aliasName) {
        log.info("Adding new alias '{}' for artist ID: {}", aliasName, artistId);

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        ArtistAlias artistAlias = ArtistAlias.builder()
                .artist(artist)
                .aliasName(aliasName)
                .build();

        ArtistAlias savedArtistAlias = artistAliasRepository.save(artistAlias);

        return artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));
    }

    /**
     * PATCH /api/v1/artist/{artistId}/name
     * Updates the primary name, moves the old name to aliases, and evicts the cache.
     */
    @Transactional
    @CacheEvict(value = "artist-tracks", key = "#artistId")
    public Artist updateArtistName(UUID artistId, String newName) {
        log.info("Updating name to '{}' for artist ID: {}", newName, artistId);

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        if (artist.getPrimaryName().equalsIgnoreCase(newName)) {
            return artist; // No change needed
        }

        // Create an alias from the current name before updating
        ArtistAlias alias = ArtistAlias.builder()
                .artist(artist)
                .aliasName(artist.getPrimaryName())
                .build();

        // Leveraging the CascadeType.ALL on the aliases list
        artist.getAliases().add(alias);
        artist.setPrimaryName(newName);

        return artistRepository.save(artist);
    }

    /**
     * GET /api/v1/artists/{artistId}/tracks
     * Retrieves paginated tracks. Results are cached in Redis.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "artist-tracks", key = "#artistId")
    public Page<Track> getArtistTracks(UUID artistId, Pageable pageable) {
        log.info("Fetching tracks from database for artist ID: {}", artistId);

        if (!artistRepository.existsById(artistId)) {
            throw new EntityNotFoundException("Artist not found");
        }

        // Requires a custom method in TrackRepository:
        // Page<Track> findByArtistId(UUID artistId, Pageable pageable);
        return trackRepository.findByArtistId(artistId, pageable);
    }

    /**
     * GET /api/v1/artists/artist-of-the-day
     * Returns the currently cached featured artist.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "artist-of-the-day")
    public Artist getArtistOfTheDay() {
        log.info("Fetching Artist of the Day from database");

        // Requires a custom method in ArtistRepository:
        // Optional<Artist> findFirstByFeaturedDate(LocalDate date);
//        return artistRepository.findFirstByFeaturedDate(LocalDate.now())
//                .orElseThrow(() -> new EntityNotFoundException("No featured artist found for today"));
        return new Artist();
    }
}