# STAGE1: Build
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY . .

RUN chmod +x gradlew

RUN ./gradlew shadowJar --no-daemon

# STAGE2: Runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/build/libs/web-invoice-generator-all.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
