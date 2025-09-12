FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts /app/
COPY gradlew /app/
COPY gradle /app/gradle/

RUN chmod +x ./gradlew
RUN ./gradlew dependencies

COPY src /app/src

RUN ./gradlew build -x test


FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

EXPOSE 8080

COPY --from=builder /app/build/libs/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

