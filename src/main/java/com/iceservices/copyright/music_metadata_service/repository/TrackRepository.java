package com.iceservices.copyright.music_metadata_service.repository;

import com.iceservices.copyright.music_metadata_service.entity.Track;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrackRepository extends JpaRepository<Track, UUID> {

    Page<Track> findByArtistId(UUID artistId, Pageable pageable);
}
