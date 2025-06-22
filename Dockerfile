FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

ARG JAR_NAME
COPY target/${JAR_NAME} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]