# A3: backend runtime image. The jar is built on the host with the repo wrapper
# (./mvnw -B -DskipTests package) because the local network MITM proxy breaks
# in-container Maven downloads. Run the host build before docker compose build.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/job-tracker-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
