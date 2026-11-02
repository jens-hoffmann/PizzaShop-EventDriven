FROM docker.io/library/maven:3.9.16-eclipse-temurin-25-noble AS build

WORKDIR /workspace
ARG SERVICE

COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY shared shared
COPY services services
COPY openapi openapi

RUN ./mvnw -q -pl "services/${SERVICE}" -am package -DskipTests

FROM docker.io/library/eclipse-temurin:25-jre-noble

ARG SERVICE
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --system --uid 10001 --create-home appuser
WORKDIR /app
COPY --from=build "/workspace/services/${SERVICE}/target/${SERVICE}-"*.jar /app/app.jar
RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
