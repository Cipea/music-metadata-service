-- Insert additional artists
INSERT INTO artist (id, legal_name, primary_name, entity_type, biography, created_at) VALUES
    ('77777777-7777-7777-7777-777777777777', 'Northstar Audio', 'Northstar', 'BAND', 'An atmospheric electronic group blending analog synths and live percussion.', CURRENT_TIMESTAMP),
    ('88888888-8888-8888-8888-888888888888', 'Maya Rivers', 'Maya Rivers', 'SOLO', 'A singer-songwriter known for intimate folk arrangements.', CURRENT_TIMESTAMP),
    ('99999999-9999-9999-9999-999999999999', 'Paper Lanterns', 'Paper Lanterns', 'BAND', 'An indie rock trio with bright melodies and thoughtful lyrics.', CURRENT_TIMESTAMP),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Elias Vale', 'Elias Vale', 'SOLO', 'A jazz pianist and composer exploring modern small-group sounds.', CURRENT_TIMESTAMP),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Cinder Coast', 'Cinder Coast', 'BAND', 'A coastal alternative band with a warm, guitar-driven sound.', CURRENT_TIMESTAMP),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Nia Sol', 'Nia Sol', 'SOLO', 'An independent R&B vocalist with a soulful, understated style.', CURRENT_TIMESTAMP),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Juniper Vale', 'Juniper Vale', 'BAND', 'A chamber-pop ensemble featuring strings and layered harmonies.', CURRENT_TIMESTAMP),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Theo Park', 'Theo Park', 'SOLO', 'An instrumental guitarist combining ambient and post-rock textures.', CURRENT_TIMESTAMP);

-- Insert additional aliases
INSERT INTO artist_alias (id, artist_id, alias_name) VALUES
    ('00000000-0000-0000-0000-000000000101', '77777777-7777-7777-7777-777777777777', 'Northstar Sessions'),
    ('00000000-0000-0000-0000-000000000102', '88888888-8888-8888-8888-888888888888', 'Maya R.'),
    ('00000000-0000-0000-0000-000000000103', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Cinder Coastline'),
    ('00000000-0000-0000-0000-000000000104', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Theo P.');

-- Insert additional tracks
INSERT INTO track (id, artist_id, title, length_seconds, genre, release_date) VALUES
    ('00000000-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'Neon Afterglow', 284, 'Synthwave', '2017-02-10'),
    ('00000000-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'City Lights', 301, 'Synthwave', '2018-04-20'),
    ('00000000-0000-0000-0000-000000000003', '22222222-2222-2222-2222-222222222222', 'Tidepool', 219, 'Dream Pop', '2026-03-06'),
    ('00000000-0000-0000-0000-000000000004', '77777777-7777-7777-7777-777777777777', 'Northern Signal', 258, 'Electronic', '2024-05-17'),
    ('00000000-0000-0000-0000-000000000005', '88888888-8888-8888-8888-888888888888', 'Open Window', 232, 'Folk', '2023-09-08'),
    ('00000000-0000-0000-0000-000000000006', '99999999-9999-9999-9999-999999999999', 'Paper Moons', 207, 'Indie Rock', '2022-06-24'),
    ('00000000-0000-0000-0000-000000000007', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Blue Hour Trio', 344, 'Jazz', '2021-11-12'),
    ('00000000-0000-0000-0000-000000000008', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Saltwater Days', 246, 'Alternative', '2024-07-19'),
    ('00000000-0000-0000-0000-000000000009', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'Slow Motion', 225, 'R&B', '2023-02-03'),
    ('00000000-0000-0000-0000-000000000010', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'Glass Garden', 271, 'Chamber Pop', '2020-10-16'),
    ('00000000-0000-0000-0000-000000000011', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Quiet Satellites', 312, 'Post-Rock', '2025-01-31'),
    ('00000000-0000-0000-0000-000000000012', '77777777-7777-7777-7777-777777777777', 'Static Bloom', 267, 'Electronic', '2025-04-11');