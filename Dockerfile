FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY app.jar app.jar

EXPOSE 8080 9092

ENTRYPOINT ["java", "-jar", "/app/app.jar"]