package com.iceservices.copyright.music_metadata_service.dto;

import com.iceservices.copyright.music_metadata_service.entity.Artist;
import com.iceservices.copyright.music_metadata_service.entity.ArtistAlias;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record ArtistResponseDto(
        UUID id,
        String primaryName,
        String legalName,
        String entityType,
        String biography,
        Date createdAt,
        List<String> aliases
) {
    public static ArtistResponseDto fromEntity(Artist artist) {
        return new ArtistResponseDto(
                artist.getId(),
                artist.getPrimaryName(),
                artist.getLegalName(),
                artist.getEntityType(),
                artist.getBiography(),
                artist.getCreatedAt(),
                artist.getAliases().stream()
                        .map(ArtistAlias::getAliasName)
                        .collect(Collectors.toList())
        );
    }
}