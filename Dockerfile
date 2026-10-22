FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN chmod +x mvnw \
    && ./mvnw -B -DskipTests package \
    && jar="$(find target -maxdepth 1 -type f -name 'music-metadata-service-*.jar' ! -name '*.original' -print -quit)" \
    && test -n "$jar" \
    && cp "$jar" /workspace/app.jar

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build --chown=10001:10001 /workspace/app.jar /app/app.jar

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
