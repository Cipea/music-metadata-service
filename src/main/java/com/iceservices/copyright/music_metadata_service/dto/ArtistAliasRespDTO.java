package com.iceservices.copyright.music_metadata_service.dto;

import com.iceservices.copyright.music_metadata_service.entity.ArtistAlias;

import java.io.Serializable;
import java.util.UUID;

public record ArtistAliasRespDTO(
        UUID id,
        UUID artistId,
        String aliasName
) implements Serializable {
    public static ArtistAliasRespDTO fromEntity(ArtistAlias artistAlias) {
        return new ArtistAliasRespDTO(
                artistAlias.getId(),
                artistAlias.getArtist().getId(),
                artistAlias.getAliasName());
    }
}
