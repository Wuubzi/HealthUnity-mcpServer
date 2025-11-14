FROM gradle:8.10-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test


FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar


EXPOSE 8000


ENV JAVA_OPTS="-Xms256m -Xmx512m"


ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]