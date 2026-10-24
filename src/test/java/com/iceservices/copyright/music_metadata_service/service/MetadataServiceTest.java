package com.iceservices.copyright.music_metadata_service.service;

import com.iceservices.copyright.music_metadata_service.dto.ArtistResponseDto;
import com.iceservices.copyright.music_metadata_service.dto.TrackRequestDto;
import com.iceservices.copyright.music_metadata_service.dto.TrackResponseDto;
import com.iceservices.copyright.music_metadata_service.entity.Artist;
import com.iceservices.copyright.music_metadata_service.entity.ArtistAlias;
import com.iceservices.copyright.music_metadata_service.entity.Track;
import com.iceservices.copyright.music_metadata_service.exception.ArtistAliasConflictException;
import com.iceservices.copyright.music_metadata_service.exception.ArtistNotFoundException;
import com.iceservices.copyright.music_metadata_service.repository.ArtistAliasRepository;
import com.iceservices.copyright.music_metadata_service.repository.ArtistRepository;
import com.iceservices.copyright.music_metadata_service.repository.TrackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetadataServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private ArtistAliasRepository artistAliasRepository;

    @InjectMocks
    private MetadataService metadataService;

    @Test
    void addTrackSavesTrackForArtist() {
        // Given
        UUID artistId = UUID.randomUUID();
        Artist artist = getArtist(artistId, "Artist");
        LocalDate releaseDate = LocalDate.of(2024, 5, 17);
        TrackRequestDto request = new TrackRequestDto("Song", 215, "Rock", releaseDate);
        Track savedTrack = Track.builder().id(UUID.randomUUID()).build();

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(trackRepository.save(any(Track.class))).thenReturn(savedTrack);

        // When
        Track result = metadataService.addTrack(artistId, request);

        // Then
        assertNotNull(result);
        assertSame(savedTrack, result);
        assertEquals(savedTrack.getId(), result.getId());
    }

    @Test
    void addTrackThrowsWhenArtistDoesNotExist() {
        // Given
        UUID artistId = UUID.randomUUID();
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(ArtistNotFoundException.class,
                () -> metadataService.addTrack(artistId, new TrackRequestDto("Song", 215, "Rock", null)));

        verifyNoInteractions(trackRepository);
    }

    @Test
    void addExplicitAliasSavesAliasAndReturnsResponse() {
        // Given
        UUID artistId = UUID.randomUUID();
        Artist artist = getArtist(artistId, "Artist");
        UUID aliasId = UUID.randomUUID();
        ArtistAlias savedAlias = ArtistAlias.builder()
                .id(aliasId)
                .artist(artist)
                .aliasName("Stage Name")
                .build();
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(artistAliasRepository.save(any(ArtistAlias.class))).thenReturn(savedAlias);

        // When
        ArtistAlias result = metadataService.addExplicitAlias(artistId, "Stage Name");

        // Then
        assertNotNull(result);
        assertSame(savedAlias, result);
        assertEquals(aliasId, result.getId());
        assertEquals(artistId, result.getArtist().getId());
        assertEquals("Stage Name", result.getAliasName());
        verify(artistAliasRepository).save(any(ArtistAlias.class));
        verify(artistRepository).findById(artistId);
    }

    @Test
    void addExplicitAliasThrowsWhenArtistDoesNotExist() {
        // Given
        UUID artistId = UUID.randomUUID();
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(ArtistNotFoundException.class,
                () -> metadataService.addExplicitAlias(artistId, "Stage Name"));

        verifyNoInteractions(artistAliasRepository);
        verify(artistRepository, never()).save(any(Artist.class));
    }

    @Test
    void addExplicitAliasThrowsWhenAliasAlreadyExists() {
        // Given
        UUID artistId = UUID.randomUUID();
        Artist artist = getArtist(artistId, "Artist");
        artist.getAliases().add(ArtistAlias.builder().artist(artist).aliasName("Stage Name").build());
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));

        // When / Then
        assertThrows(ArtistAliasConflictException.class,
                () -> metadataService.addExplicitAlias(artistId, "Stage Name"));

        verifyNoInteractions(artistAliasRepository);
        verify(artistRepository, never()).save(any(Artist.class));
    }

    @Test
    void updateArtistNameAddsOldNameAsAliasAndSavesArtist() {
        // Given
        UUID artistId = UUID.randomUUID();
        Artist artist = getArtist(artistId, "Old Name");
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(artistRepository.save(artist)).thenReturn(artist);

        // When
        Artist result = metadataService.updateArtistName(artistId, "New Name");

        // Then
        assertNotNull(result);
        assertSame(artist, result);
        assertEquals("New Name", result.getPrimaryName());
        verify(artistRepository).save(artist);
    }

    @Test
    void updateArtistNameDoesNotSaveWhenNameIsUnchangedIgnoringCase() {
        // Given
        UUID artistId = UUID.randomUUID();
        Artist artist = getArtist(artistId, "Artist");
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));

        // When
        Artist result = metadataService.updateArtistName(artistId, "aRTiSt");

        // Then
        assertSame(artist, result);
        verify(artistRepository, never()).save(any(Artist.class));
    }

    @Test
    void updateArtistNameThrows404WhenArtistDoesNotExist() {
        // Given
        UUID artistId = UUID.randomUUID();
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(ArtistNotFoundException.class,
                () -> metadataService.updateArtistName(artistId, "New Name"));

        verify(artistRepository, never()).save(any(Artist.class));
    }

    @Test
    void getArtistTracksMapsTracksToResponseDtos() {
        // Given
        UUID artistId = UUID.randomUUID();
        Artist artist = getArtist(artistId, "Artist");
        Track track = Track.builder()
                .id(UUID.randomUUID())
                .artist(artist)
                .title("Song")
                .lengthSeconds(215)
                .genre("Rock")
                .releaseDate(LocalDate.of(2024, 5, 17))
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Track> trackPage = new PageImpl<>(List.of(track), pageable, 1);

        when(artistRepository.existsById(artistId)).thenReturn(true);
        when(trackRepository.findByArtistId(artistId, pageable)).thenReturn(trackPage);

        // When
        Page<TrackResponseDto> result = metadataService.getArtistTracks(artistId, pageable);

        // Then
        assertEquals(1, result.getTotalElements());

        TrackResponseDto response = result.getContent().getFirst();
        assertEquals(track.getId(), response.id());
        assertEquals(artistId, response.artistId());
        assertEquals("Artist", response.artistName());
        assertEquals("Song", response.title());
        assertEquals(215, response.durationSeconds());
        assertEquals("Rock", response.genre());
        assertEquals(LocalDate.of(2024, 5, 17), response.releaseDate());
        verify(trackRepository).findByArtistId(artistId, pageable);
    }

    @Test
    void getArtistTracksThrowsWhenArtistDoesNotExist() {
        // Given
        UUID artistId = UUID.randomUUID();
        when(artistRepository.existsById(artistId)).thenReturn(false);

        // When / Then
        assertThrows(ArtistNotFoundException.class,
                () -> metadataService.getArtistTracks(artistId, PageRequest.of(0, 10)));

        verifyNoInteractions(trackRepository);
    }

    @Test
    void getArtistOfTheDayReturnsArtistAtTodaysUtcCatalogueIndex() {
        // Given
        long artistCount = 7;
        int expectedIndex = (int) Math.floorMod(LocalDate.now(ZoneOffset.UTC).toEpochDay(), artistCount);
        UUID artistId = UUID.randomUUID();
        Artist artist = getArtist(artistId, "Artist of the day");
        artist.setLegalName("Legal Artist Name");
        artist.setEntityType("PERSON");
        artist.setBiography("Biography");
        artist.setCreatedAt(new Date());
        artist.getAliases().add(ArtistAlias.builder().artist(artist).aliasName("Alias").build());
        when(artistRepository.count()).thenReturn(artistCount);
        when(artistRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(artist)));

        // When
        ArtistResponseDto result = metadataService.getArtistOfTheDay();

        // Then
        assertEquals(artistId, result.id());
        assertEquals("Artist of the day", result.primaryName());
        assertEquals("Legal Artist Name", result.legalName());
        assertEquals("PERSON", result.entityType());
        assertEquals(List.of("Alias"), result.aliases());
        verify(artistRepository).findAll(
                PageRequest.of(expectedIndex, 1, Sort.by(Sort.Direction.ASC, "id")));
    }

    @Test
    void getArtistOfTheDayThrowsWhenCatalogueIsEmpty() {
        // Given
        when(artistRepository.count()).thenReturn(0L);

        // When / Then
        assertThrows(ArtistNotFoundException.class, metadataService::getArtistOfTheDay);

        verify(artistRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getArtistOfTheDayThrowsWhenSelectedPageIsEmpty() {
        // Given
        when(artistRepository.count()).thenReturn(1L);
        when(artistRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        // When
        ArtistNotFoundException exception = assertThrows(
                ArtistNotFoundException.class,
                metadataService::getArtistOfTheDay);

        // Then
        assertEquals("No artists available", exception.getMessage());
    }

    private static Artist getArtist(UUID id, String primaryName) {
        return Artist.builder()
                .id(id)
                .primaryName(primaryName)
                .legalName(primaryName)
                .entityType("PERSON")
                .build();
    }
}
