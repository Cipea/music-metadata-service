package com.iceservices.copyright.music_metadata_service.service;

import com.iceservices.copyright.music_metadata_service.dto.*;
import com.iceservices.copyright.music_metadata_service.entity.Artist;
import com.iceservices.copyright.music_metadata_service.entity.ArtistAlias;
import com.iceservices.copyright.music_metadata_service.entity.Track;
import com.iceservices.copyright.music_metadata_service.repository.ArtistAliasRepository;
import com.iceservices.copyright.music_metadata_service.repository.ArtistRepository;
import com.iceservices.copyright.music_metadata_service.repository.TrackRepository;
import com.iceservices.copyright.music_metadata_service.exception.ArtistAliasConflictException;
import com.iceservices.copyright.music_metadata_service.exception.ArtistNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetadataService {

    private final ArtistRepository artistRepository;
    private final TrackRepository trackRepository;
    private final ArtistAliasRepository artistAliasRepository;

    @Transactional
    @CacheEvict(value = "artist-tracks", allEntries = true)
    public Track addTrack(UUID artistId, TrackRequestDto request) {
        log.info("Adding new track '{}' for artist ID: {}", request.title(), artistId);

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ArtistNotFoundException("Artist not found: " + artistId));

        Track track = Track.builder()
                .artist(artist)
                .title(request.title())
                .lengthSeconds(request.durationSeconds())
                .genre(request.genre())
                .releaseDate(request.releaseDate())
                .build();

        return trackRepository.save(track);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "artist-tracks", allEntries = true),
            @CacheEvict(value = "artist-of-the-day", allEntries = true)
    })
    public ArtistAlias addExplicitAlias(UUID artistId, String aliasName) {
        log.info("Adding new alias '{}' for artist ID: {}", aliasName, artistId);

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ArtistNotFoundException("Artist not found: " + artistId));

        // Prevent duplicate aliases for this artist before writing the new row.
        boolean artistAliasExist = artist.getAliases()
                .stream()
                .anyMatch(artistAlias ->
                        artistAlias.getAliasName().equals(aliasName));

        if (artistAliasExist) {
            throw new ArtistAliasConflictException("Artist alias already exists: " + aliasName);
        }

        ArtistAlias artistAlias = ArtistAlias.builder()
                .artist(artist)
                .aliasName(aliasName)
                .build();

        return artistAliasRepository.save(artistAlias);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "artist-tracks", allEntries = true),
            @CacheEvict(value = "artist-of-the-day", allEntries = true)
    })
    public Artist updateArtistName(UUID artistId, String newName) {
        log.info("Updating name to '{}' for artist ID: {}", newName, artistId);

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ArtistNotFoundException("Artist not found: " + artistId));

        if (artist.getPrimaryName().equalsIgnoreCase(newName)) {
            return artist; // No change needed
        }

        // Keep the old name searchable after changing the artist's primary name.
        ArtistAlias alias = ArtistAlias.builder()
                .artist(artist)
                .aliasName(artist.getPrimaryName())
                .build();

        // CascadeType.ALL on aliases persists this new alias with the artist.
        artist.getAliases().add(alias);
        artist.setPrimaryName(newName);

        return artistRepository.save(artist);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "artist-tracks",
            key = "#artistId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public Page<TrackResponseDto> getArtistTracks(UUID artistId, Pageable pageable) {
        log.info("Fetching tracks from database for artist ID: {}", artistId);

        if (!artistRepository.existsById(artistId)) {
            throw new ArtistNotFoundException("Artist not found: " + artistId);
        }

        return trackRepository.findByArtistId(artistId, pageable)
                .map(TrackResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "artist-of-the-day",
            key = "T(java.time.LocalDate).now(T(java.time.ZoneOffset).UTC).toString()")
    public ArtistResponseDto getArtistOfTheDay() {
        log.info("Retrieving the Artist of the Day");

        // The catalogue size is the length of one complete rotation.
        long artistCount = artistRepository.count();
        if (artistCount == 0) {
            throw new ArtistNotFoundException("No artists available for Artist of the Day");
        }

        // Turn today's UTC date into a day number, then wrap it into a valid
        // zero-based catalogue index. The index advances by one each day.
        long dayIndex = Math.floorMod(LocalDate.now(ZoneOffset.UTC)
                .toEpochDay(), artistCount);

        // Use one-item pagination to fetch only the selected artist. Sorting by ID
        // gives each index a stable artist, regardless of database row order.
        Pageable artistPage = PageRequest.of(
                Math.toIntExact(dayIndex),
                1,
                Sort.by(Sort.Direction.ASC, "id"));

        Artist artist = artistRepository.findAll(artistPage).getContent()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ArtistNotFoundException("No artists available"));

        // Cache and return a response DTO rather than a JPA entity.
        return ArtistResponseDto.fromEntity(artist);
    }
}