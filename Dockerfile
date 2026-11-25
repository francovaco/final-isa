# syntax=docker/dockerfile:1
# Imagen de ClubReservas. Se construye todo dentro de Docker: no hace falta Java ni Node en la máquina.
#   docker build -t clubreservas:latest .
# (docker compose la construye sola la primera vez)

# ---------------------------------------------------------------------------
# Etapa 1: compilar backend y frontend (perfil prod) y generar el jar
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-noble AS build
WORKDIR /build

# Primero los archivos de dependencias, para aprovechar la cache de capas
COPY .mvn/ .mvn/
COPY mvnw pom.xml package.json package-lock.json ./
COPY sonar-project.properties checkstyle.xml ./

# Después el resto del código
COPY angular.json tsconfig.json tsconfig.app.json workbox.config.cjs .prettierrc .prettierignore eslint.config.mjs .lintstagedrc.cjs ./
COPY webpack/ webpack/
COPY src/main/ src/main/

# Las caches de Maven y npm se reutilizan entre builds (BuildKit)
RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=cache,target=/root/.npm \
    chmod +x mvnw && \
    ./mvnw -ntp -B -Pprod -DskipTests -Dcheckstyle.skip -Dmodernizer.skip package && \
    cp target/*.jar /build/app.jar

# ---------------------------------------------------------------------------
# Etapa 2: imagen final, solo con el JRE y el jar
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-noble

# curl para el healthcheck del compose
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd --system app && useradd --system --gid app --home-dir /app app

WORKDIR /app
COPY --from=build --chown=app:app /build/app.jar app.jar

# La app no corre como root
USER app

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
