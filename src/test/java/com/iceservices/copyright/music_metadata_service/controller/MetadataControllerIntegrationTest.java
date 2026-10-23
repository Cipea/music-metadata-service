package com.iceservices.copyright.music_metadata_service.controller;

import com.iceservices.copyright.music_metadata_service.entity.Artist;
import com.iceservices.copyright.music_metadata_service.entity.ArtistAlias;
import com.iceservices.copyright.music_metadata_service.entity.Track;
import com.iceservices.copyright.music_metadata_service.exception.ArtistAliasConflictException;
import com.iceservices.copyright.music_metadata_service.exception.ArtistNotFoundException;
import com.iceservices.copyright.music_metadata_service.service.MetadataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetadataController.class)
@AutoConfigureMockMvc(addFilters = false)
class MetadataControllerIntegrationTest {

    private static final String ARTIST_PATH = "/api/v1/artist";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetadataService metadataService;

    @Test
    void addTrackReturnsCreatedResponseAndLocation() throws Exception {
        UUID artistId = UUID.randomUUID();
        UUID trackId = UUID.randomUUID();
        Artist artist = artist(artistId, "Artist");
        Track track = Track.builder()
                .id(trackId)
                .artist(artist)
                .title("Song")
                .lengthSeconds(215)
                .genre("Rock")
                .releaseDate(LocalDate.of(2024, 5, 17))
                .build();
        when(metadataService.addTrack(eq(artistId), any())).thenReturn(track);

        mockMvc.perform(post(ARTIST_PATH + "/{artistId}/tracks", artistId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Song","durationSeconds":215,"genre":"Rock","releaseDate":"2024-05-17"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "http://localhost" + ARTIST_PATH + "/" + artistId + "/tracks/" + trackId))
                .andExpect(jsonPath("$.id").value(trackId.toString()))
                .andExpect(jsonPath("$.artistId").value(artistId.toString()))
                .andExpect(jsonPath("$.artistName").value("Artist"))
                .andExpect(jsonPath("$.title").value("Song"))
                .andExpect(jsonPath("$.durationSeconds").value(215))
                .andExpect(jsonPath("$.genre").value("Rock"))
                .andExpect(jsonPath("$.releaseDate").value("2024-05-17"));
    }

    @Test
    void updateArtistNameReturnsArtistAndAliases() throws Exception {
        UUID artistId = UUID.randomUUID();
        Artist artist = artist(artistId, "New Name");
        artist.getAliases().add(ArtistAlias.builder().artist(artist).aliasName("Old Name").build());
        when(metadataService.updateArtistName(artistId, "New Name")).thenReturn(artist);

        mockMvc.perform(patch(ARTIST_PATH + "/{artistId}/name", artistId)
                        .contentType(APPLICATION_JSON)
                        .content("{\"newPrimaryName\":\"New Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(artistId.toString()))
                .andExpect(jsonPath("$.primaryName").value("New Name"))
                .andExpect(jsonPath("$.aliases[0]").value("Old Name"));
    }

    @Test
    void addExplicitAliasReturnsUpdatedArtist() throws Exception {
        UUID artistId = UUID.randomUUID();
        Artist artist = artist(artistId, "Artist");
        artist.getAliases().add(ArtistAlias.builder().artist(artist).aliasName("Stage Name").build());
        when(metadataService.addExplicitAlias(artistId, "Stage Name")).thenReturn(artist);

        mockMvc.perform(post(ARTIST_PATH + "/{artistId}/aliases", artistId)
                        .contentType(APPLICATION_JSON)
                        .content("{\"aliasName\":\"Stage Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primaryName").value("Artist"))
                .andExpect(jsonPath("$.aliases[0]").value("Stage Name"));
    }

    @Test
    void getArtistTracksPassesPaginationAndReturnsPage() throws Exception {
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
        when(metadataService.getArtistTracks(eq(artistId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(
                        new com.iceservices.copyright.music_metadata_service.dto.TrackResponseDto(
                                track.getId(), artistId, "Artist", "Song", 215, "Rock", track.getReleaseDate()))));

        mockMvc.perform(get(ARTIST_PATH + "/{artistId}/tracks", artistId)
                        .param("page", "1")
                        .param("size", "3")
                        .param("sort", "title,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(track.getId().toString()))
                .andExpect(jsonPath("$.content[0].artistName").value("Artist"))
                .andExpect(jsonPath("$.content[0].title").value("Song"))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(3));

        org.mockito.ArgumentCaptor<Pageable> pageableCaptor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(metadataService).getArtistTracks(eq(artistId), pageableCaptor.capture());
        assertEquals(1, pageableCaptor.getValue().getPageNumber());
        assertEquals(3, pageableCaptor.getValue().getPageSize());
        assertEquals("title: DESC", pageableCaptor.getValue().getSort().toString());
    }

    @Test
    void getArtistOfTheDayReturnsArtistResponse() throws Exception {
        UUID artistId = UUID.randomUUID();
        when(metadataService.getArtistOfTheDay()).thenReturn(
                new com.iceservices.copyright.music_metadata_service.dto.ArtistResponseDto(
                        artistId, "Artist", "Legal Name", "PERSON", "Biography", new Date(), List.of("Alias")));

        mockMvc.perform(get(ARTIST_PATH + "/artist-of-the-day"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(artistId.toString()))
                .andExpect(jsonPath("$.primaryName").value("Artist"))
                .andExpect(jsonPath("$.aliases[0]").value("Alias"));
    }

    @Test
    void artistNotFoundIsMappedToProblemDetail() throws Exception {
        UUID artistId = UUID.randomUUID();
        when(metadataService.getArtistTracks(eq(artistId), any(Pageable.class)))
                .thenThrow(new ArtistNotFoundException("Artist not found: " + artistId));

        mockMvc.perform(get(ARTIST_PATH + "/{artistId}/tracks", artistId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Artist not found"))
                .andExpect(jsonPath("$.detail").value("Artist not found: " + artistId))
                .andExpect(jsonPath("$.instance").value(ARTIST_PATH + "/" + artistId + "/tracks"));
    }

    @Test
    void aliasConflictIsMappedToConflictProblemDetail() throws Exception {
        UUID artistId = UUID.randomUUID();
        when(metadataService.addExplicitAlias(artistId, "Stage Name"))
                .thenThrow(new ArtistAliasConflictException("Artist alias already exists: Stage Name"));

        mockMvc.perform(post(ARTIST_PATH + "/{artistId}/aliases", artistId)
                        .contentType(APPLICATION_JSON)
                        .content("{\"aliasName\":\"Stage Name\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Artist alias conflict"))
                .andExpect(jsonPath("$.detail").value("Artist alias already exists: Stage Name"));
    }

    @Test
    void malformedRequestBodyIsMappedToBadRequestProblemDetail() throws Exception {
        UUID artistId = UUID.randomUUID();

        mockMvc.perform(post(ARTIST_PATH + "/{artistId}/tracks", artistId)
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Song\",\"releaseDate\":\"not-a-date\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.detail").value("Request body is invalid"));
    }

    @Test
    void invalidArtistIdIsMappedToBadRequestProblemDetail() throws Exception {
        mockMvc.perform(get(ARTIST_PATH + "/not-a-uuid/tracks"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.detail").value("Invalid value for parameter 'artistId'"));
    }

    private static Artist artist(UUID id, String primaryName) {
        return Artist.builder()
                .id(id)
                .primaryName(primaryName)
                .legalName(primaryName)
                .entityType("PERSON")
                .createdAt(new Date())
                .build();
    }
}
