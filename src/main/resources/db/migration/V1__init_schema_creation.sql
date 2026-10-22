CREATE TABLE artist (
    id UUID PRIMARY KEY,
    legal_name VARCHAR(255) NOT NULL,
    primary_name VARCHAR(255) NOT NULL,
    entity_type VARCHAR(255) NOT NULL,
    biography TEXT,
    created_at TIMESTAMP
);

CREATE TABLE artist_alias (
    id UUID PRIMARY KEY,
    artist_id UUID NOT NULL,
    alias_name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_artist_alias_artist FOREIGN KEY (artist_id) REFERENCES artist (id) ON DELETE CASCADE
);

CREATE TABLE track (
    id UUID PRIMARY KEY,
    artist_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    length_seconds INT,
    genre VARCHAR(100),
    release_date DATE,
    CONSTRAINT fk_track_artist FOREIGN KEY (artist_id) REFERENCES artist (id) ON DELETE CASCADE
);