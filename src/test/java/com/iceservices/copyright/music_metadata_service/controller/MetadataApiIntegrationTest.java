package com.iceservices.copyright.music_metadata_service.controller;


import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MetadataApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Using the seed data from your Flyway scripts as defined in the README
    private static final String ARTIST_ID = "11111111-1111-1111-1111-111111111111";
    private static final String ARTIST_PATH = "/api/v1/artist";
    UUID randomId = UUID.randomUUID();

    @Test
    void addTrackReturnsCreatedResponse() throws Exception {
        String newTrackJson = """
                {
                  "title": "Integration Test Song",
                  "durationSeconds": 300,
                  "genre": "Synthwave",
                  "releaseDate": "2026-10-25"
                }
                """;

        mockMvc.perform(post(ARTIST_PATH + "/{artistId}/tracks", ARTIST_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newTrackJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.artistId").value(ARTIST_ID))
                .andExpect(jsonPath("$.title").value("Integration Test Song"))
                .andExpect(jsonPath("$.durationSeconds").value(300))
                .andExpect(jsonPath("$.genre").value("Synthwave"))
                .andExpect(jsonPath("$.releaseDate").value("2026-10-25"));
    }

    @Test
    void addTrackReturnsArtistNotFound() throws Exception {
        String newTrackJson = """
                {
                  "title": "Integration Test Song",
                  "durationSeconds": 300,
                  "genre": "Synthwave",
                  "releaseDate": "2026-10-25"
                }
                """;

        mockMvc.perform(post(ARTIST_PATH + "/{artistId}/tracks", randomId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newTrackJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Artist not found"));
    }

    @Test
    void getArtistTracksPassesPaginationAndReturnsPage() throws Exception {
        mockMvc.perform(get(ARTIST_PATH + "/{artistId}/tracks", ARTIST_ID)
                        .with(jwt())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void getArtistTracksMatchesThePageSizeThatWasSet() throws Exception {
        mockMvc.perform(get(ARTIST_PATH + "/{artistId}/tracks", ARTIST_ID)
                        .with(jwt())
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void updateArtistDoesNotSaveWhenNewNameIsTheSame() throws Exception {
        String updateJson = """
                {
                  "newPrimaryName": "The Midnight"
                }
                """;

        mockMvc.perform(patch(ARTIST_PATH + "/{artistId}/name", ARTIST_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ARTIST_ID))
                .andExpect(jsonPath("$.primaryName").value("The Midnight"))
                .andExpect(jsonPath("$.aliases").isArray());
    }

    @Test
    void updateArtistNameReturnsArtistAndAliases() throws Exception {
        String updateJson = """
                {
                  "newPrimaryName": "The Midnight Updated Integration"
                }
                """;

        mockMvc.perform(patch(ARTIST_PATH + "/{artistId}/name", ARTIST_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ARTIST_ID))
                .andExpect(jsonPath("$.primaryName").value("The Midnight Updated Integration"))
                .andExpect(jsonPath("$.aliases").isArray());
    }

    @Test
    void updateArtistNameWhenArtistDoesNotExist() throws Exception {
        String updateJson = """
                {
                  "newPrimaryName": "The Midnight Updated Integration"
                }
                """;

        mockMvc.perform(patch(ARTIST_PATH + "/{artistId}/name", randomId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Artist not found"));
    }

    @Test
    void addExplicitAliasReturnsAliasResponse() throws Exception {
        String uniqueAlias = "Stage Name " + UUID.randomUUID().toString().substring(0, 8);
        String aliasJson = String.format("{\"aliasName\":\"%s\"}", uniqueAlias);

        mockMvc.perform(post(ARTIST_PATH + "/{artistId}/aliases", ARTIST_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aliasJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.artistId").value(ARTIST_ID))
                .andExpect(jsonPath("$.aliasName").value(uniqueAlias));
    }

    @Test
    void addExplicitAliasThatAlreadyExists() throws Exception {
        String uniqueAlias = "Midnight Son";
        String aliasJson = String.format("{\"aliasName\":\"%s\"}", uniqueAlias);

        mockMvc.perform(post(ARTIST_PATH + "/{artistId}/aliases", ARTIST_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aliasJson))
                .andExpect(status().isConflict());
    }

    @Test
    void getArtistOfTheDayReturnsArtistResponse() throws Exception {
        mockMvc.perform(get(ARTIST_PATH + "/artist-of-the-day")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.primaryName").exists());
    }

    @Test
    void requestWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get(ARTIST_PATH + "/artist-of-the-day"))
                // Deliberately omitting .with(jwt()) to test the perimeter
                .andExpect(status().isUnauthorized());
    }

    @Test
    void artistNotFoundIsMappedToProblemDetail() throws Exception {
        mockMvc.perform(get(ARTIST_PATH + "/{artistId}/tracks", randomId)
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Artist not found"))
                .andExpect(jsonPath("$.instance").value(ARTIST_PATH + "/" + randomId + "/tracks"));
    }
}