FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/resumebuilderapi-0.0.1-SNAPSHOT.jar resumebuilderapi.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "resumebuilderapi.jar"]