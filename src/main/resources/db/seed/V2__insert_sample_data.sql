--- Insert Artists
INSERT INTO artist (id, legal_name, primary_name, entity_type, biography, created_at) VALUES
    ('11111111-1111-1111-1111-111111111111', 'The Midnight LLC', 'The Midnight', 'BAND', 'Los Angeles based synthwave band.', CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'Luna Smith', 'Luna & The Tides', 'SOLO', 'Rising stars in the indie dream-pop scene.', CURRENT_TIMESTAMP);

-- Insert Aliases
INSERT INTO artist_alias (id, artist_id, alias_name) VALUES
    ('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 'Midnight Son');

-- Insert Tracks
INSERT INTO track (id, artist_id, title, length_seconds, genre, release_date) VALUES
    ('44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111', 'Sunset', 326, 'Synthwave', '2016-08-05'),
    ('55555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111', 'Vampires', 317, 'Synthwave', '2016-08-05'),
    ('66666666-6666-6666-6666-666666666666', '22222222-2222-2222-2222-222222222222', 'Ocean Whispers', 240, 'Dream Pop', '2026-01-15');