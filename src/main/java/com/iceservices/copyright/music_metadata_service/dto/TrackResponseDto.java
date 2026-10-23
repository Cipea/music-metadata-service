package com.iceservices.copyright.music_metadata_service.dto;


import com.iceservices.copyright.music_metadata_service.entity.Track;

import java.time.LocalDate;
import java.util.UUID;

public record TrackResponseDto(
        UUID id,
        UUID artistId,
        String artistName,
        String title,
        Integer durationSeconds,
        String genre,
        LocalDate releaseDate
) {
    public static TrackResponseDto fromEntity(Track track) {
        return new TrackResponseDto(
                track.getId(),
                track.getArtist().getId(),
                track.getArtist().getPrimaryName(),
                track.getTitle(),
                track.getLengthSeconds(),
                track.getGenre(),
                track.getReleaseDate()
        );
    }
}