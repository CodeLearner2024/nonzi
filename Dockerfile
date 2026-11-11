# =============================================================================
# Dockerfile — Build multi-étapes pour l'application RH
# Étape 1 : compilation Maven
# Étape 2 : image d'exécution allégée (JRE uniquement)
# =============================================================================

# ── Étape 1 : Build ──────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

# Copier le pom.xml en premier pour bénéficier du cache des dépendances Maven
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copier les sources et compiler
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Étape 2 : Image d'exécution ──────────────────────────────────────────────
FROM eclipse-temurin:17-jre

# Installer curl (utilisé par le healthcheck Docker)
RUN apt-get update -q && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Utilisateur non-root pour la sécurité
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

WORKDIR /app

# Créer les dossiers de données avec les bons droits AVANT de passer à appuser
# /data  : volume persistant (Docker ou Render Disk) — chemin prod par défaut
# /app/data : fallback si le volume n'est pas monté
RUN mkdir -p /data/logs /app/data \
    && chown -R appuser:appgroup /data /app/data

# Copier le JAR depuis l'étape de build
COPY --from=build /workspace/target/rh-app.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

# Port de l'application
EXPOSE 8222

# Healthcheck intégré
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8222/actuator/health || exit 1

# Options JVM recommandées pour conteneur (mémoire adaptative)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
