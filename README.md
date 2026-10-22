# music-metadata-service
Music Metadata Service that stores and provides metadata about different music tracks and artists.

## Run with Docker

Build the image from this directory:

```sh
docker build -t music-metadata-service .
```

Run it, providing the database settings required by your environment:

```sh
docker run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/music_metadata \
  -e SPRING_DATASOURCE_USERNAME=your_username \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  music-metadata-service
```

The container listens on port `8080`. Replace the example database values with your PostgreSQL configuration.
