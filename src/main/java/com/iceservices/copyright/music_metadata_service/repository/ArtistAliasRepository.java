package com.iceservices.copyright.music_metadata_service.repository;

import com.iceservices.copyright.music_metadata_service.entity.ArtistAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ArtistAliasRepository extends JpaRepository<ArtistAlias, UUID> {
}
