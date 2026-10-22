package com.iceservices.copyright.music_metadata_service.dto;

import java.time.LocalDate;

public record TrackRequestDto(
        String title,
        Integer durationSeconds,
        String genre,
        LocalDate releaseDate
) {}