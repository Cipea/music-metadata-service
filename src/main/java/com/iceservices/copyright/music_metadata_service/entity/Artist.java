package com.iceservices.copyright.music_metadata_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "artist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(name = "primary_name", nullable = false)
    private String primaryName;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private ArtistEntityType entityType;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ArtistAlias> aliases = new ArrayList<>();

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Track> tracks = new ArrayList<>();


}
