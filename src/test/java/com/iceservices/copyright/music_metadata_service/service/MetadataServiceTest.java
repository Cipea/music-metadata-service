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
import org.mockito.ArgumentCaptor;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
        UUID artistId = UUID.randomUUID();
        Artist artist = artist(artistId, "Artist");
        LocalDate releaseDate = LocalDate.of(2024, 5, 17);
        TrackRequestDto request = new TrackRequestDto("Song", 215, "Rock", releaseDate);
        Track savedTrack = Track.builder().id(UUID.randomUUID()).build();
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(trackRepository.save(any(Track.class))).thenReturn(savedTrack);

        Track result = metadataService.addTrack(artistId, request);

        assertSame(savedTrack, result);
        ArgumentCaptor<Track> trackCaptor = ArgumentCaptor.forClass(Track.class);
        verify(trackRepository).save(trackCaptor.capture());
        Track track = trackCaptor.getValue();
        assertSame(artist, track.getArtist());
        assertEquals("Song", track.getTitle());
        assertEquals(215, track.getLengthSeconds());
        assertEquals("Rock", track.getGenre());
        assertEquals(releaseDate, track.getReleaseDate());
    }

    @Test
    void addTrackThrowsWhenArtistDoesNotExist() {
        UUID artistId = UUID.randomUUID();
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        assertThrows(ArtistNotFoundException.class,
                () -> metadataService.addTrack(artistId, new TrackRequestDto("Song", 215, "Rock", null)));

        verifyNoInteractions(trackRepository);
    }

    @Test
    void addExplicitAliasSavesAliasAndReturnsArtist() {
        UUID artistId = UUID.randomUUID();
        Artist artist = artist(artistId, "Artist");
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));

        Artist result = metadataService.addExplicitAlias(artistId, "Stage Name");

        assertSame(artist, result);
        ArgumentCaptor<ArtistAlias> aliasCaptor = ArgumentCaptor.forClass(ArtistAlias.class);
        verify(artistAliasRepository).save(aliasCaptor.capture());
        assertSame(artist, aliasCaptor.getValue().getArtist());
        assertEquals("Stage Name", aliasCaptor.getValue().getAliasName());
        verify(artistRepository, times(2)).findById(artistId);
    }

    @Test
    void addExplicitAliasThrowsWhenArtistDoesNotExist() {
        UUID artistId = UUID.randomUUID();
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        assertThrows(ArtistNotFoundException.class,
                () -> metadataService.addExplicitAlias(artistId, "Stage Name"));

        verifyNoInteractions(artistAliasRepository);
        verify(artistRepository, never()).save(any(Artist.class));
    }

    @Test
    void addExplicitAliasThrowsWhenAliasAlreadyExists() {
        UUID artistId = UUID.randomUUID();
        Artist artist = artist(artistId, "Artist");
        artist.getAliases().add(ArtistAlias.builder().artist(artist).aliasName("Stage Name").build());
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));

        assertThrows(ArtistAliasConflictException.class,
                () -> metadataService.addExplicitAlias(artistId, "Stage Name"));

        verifyNoInteractions(artistAliasRepository);
        verify(artistRepository, never()).save(any(Artist.class));
    }

    @Test
    void updateArtistNameAddsOldNameAsAliasAndSavesArtist() {
        UUID artistId = UUID.randomUUID();
        Artist artist = artist(artistId, "Old Name");
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(artistRepository.save(artist)).thenReturn(artist);

        Artist result = metadataService.updateArtistName(artistId, "New Name");

        assertSame(artist, result);
        assertEquals("New Name", artist.getPrimaryName());
        assertEquals(1, artist.getAliases().size());
        assertEquals("Old Name", artist.getAliases().getFirst().getAliasName());
        assertSame(artist, artist.getAliases().getFirst().getArtist());
        verify(artistRepository).save(artist);
    }

    @Test
    void updateArtistNameDoesNotSaveWhenNameIsUnchangedIgnoringCase() {
        UUID artistId = UUID.randomUUID();
        Artist artist = artist(artistId, "Artist");
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));

        Artist result = metadataService.updateArtistName(artistId, "aRTiSt");

        assertSame(artist, result);
        assertTrue(artist.getAliases().isEmpty());
        verify(artistRepository, never()).save(any(Artist.class));
    }

    @Test
    void updateArtistNameThrowsWhenArtistDoesNotExist() {
        UUID artistId = UUID.randomUUID();
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        assertThrows(ArtistNotFoundException.class,
                () -> metadataService.updateArtistName(artistId, "New Name"));

        verify(artistRepository, never()).save(any(Artist.class));
    }

    @Test
    void getArtistTracksMapsTracksToResponseDtos() {
        UUID artistId = UUID.randomUUID();
        Artist artist = artist(artistId, "Artist");
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

        Page<TrackResponseDto> result = metadataService.getArtistTracks(artistId, pageable);

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
        UUID artistId = UUID.randomUUID();
        when(artistRepository.existsById(artistId)).thenReturn(false);

        assertThrows(ArtistNotFoundException.class,
                () -> metadataService.getArtistTracks(artistId, PageRequest.of(0, 10)));

        verifyNoInteractions(trackRepository);
    }

    @Test
    void getArtistOfTheDayReturnsArtistAtTodaysUtcCatalogueIndex() {
        long artistCount = 7;
        int expectedIndex = (int) Math.floorMod(LocalDate.now(ZoneOffset.UTC).toEpochDay(), artistCount);
        UUID artistId = UUID.randomUUID();
        Artist artist = artist(artistId, "Artist of the day");
        artist.setLegalName("Legal Artist Name");
        artist.setEntityType("PERSON");
        artist.setBiography("Biography");
        artist.setCreatedAt(new Date());
        artist.getAliases().add(ArtistAlias.builder().artist(artist).aliasName("Alias").build());
        when(artistRepository.count()).thenReturn(artistCount);
        when(artistRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(artist)));

        ArtistResponseDto result = metadataService.getArtistOfTheDay();

        assertEquals(artistId, result.id());
        assertEquals("Artist of the day", result.primaryName());
        assertEquals("Legal Artist Name", result.legalName());
        assertEquals("PERSON", result.entityType());
        assertEquals(List.of("Alias"), result.aliases());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(artistRepository).findAll(pageableCaptor.capture());
        assertEquals(PageRequest.of(expectedIndex, 1, Sort.by(Sort.Direction.ASC, "id")),
                pageableCaptor.getValue());
    }

    @Test
    void getArtistOfTheDayThrowsWhenCatalogueIsEmpty() {
        when(artistRepository.count()).thenReturn(0L);

        assertThrows(ArtistNotFoundException.class, metadataService::getArtistOfTheDay);

        verify(artistRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getArtistOfTheDayThrowsWhenSelectedPageIsEmpty() {
        when(artistRepository.count()).thenReturn(1L);
        when(artistRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        ArtistNotFoundException exception = assertThrows(
                ArtistNotFoundException.class,
                metadataService::getArtistOfTheDay);

        assertEquals("No artists available", exception.getMessage());
    }

    @Test
    void addExplicitAliasThrowsIfArtistDisappearsBeforeItIsReturned() {
        UUID artistId = UUID.randomUUID();
        Artist artist = artist(artistId, "Artist");
        when(artistRepository.findById(artistId))
                .thenReturn(Optional.of(artist))
                .thenReturn(Optional.empty());

        assertThrows(ArtistNotFoundException.class,
                () -> metadataService.addExplicitAlias(artistId, "Stage Name"));

        verify(artistAliasRepository).save(any(ArtistAlias.class));
    }

    private static Artist artist(UUID id, String primaryName) {
        return Artist.builder()
                .id(id)
                .primaryName(primaryName)
                .legalName(primaryName)
                .entityType("PERSON")
                .build();
    }
}
