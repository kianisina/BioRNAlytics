# ---------------------------------------------------------------
# BioRNAlytics – Docker-Image
#
# Stufe 1 baut Frontend (Vue/Vite) und Backend (Spring Boot) zu einer
# einzigen .jar-Datei. Stufe 2 ist das schlanke Laufzeit-Image mit
# Java 26 und R inklusive DESeq2 für die Auswertung.
# ---------------------------------------------------------------

# ===== Stufe 1: Build =====
FROM eclipse-temurin:26-jdk AS build
WORKDIR /build

# Zuerst nur die Maven-Dateien kopieren, damit die Abhängigkeiten
# gecacht werden und nicht bei jeder Code-Änderung neu laden.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline || true

# Restlicher Quellcode (Java, Vue, Konfiguration für Vite)
COPY . .

# Das frontend-maven-plugin in der pom.xml installiert Node, führt
# "npm install" und "npm run build" aus und legt das Ergebnis in
# src/main/resources/public ab. Danach wird die .jar gebaut.
# Tests werden übersprungen, weil sie eine laufende MongoDB bräuchten.
# -Djava.version=26 passt die Zielversion an das JDK im Image an,
# egal welche Version lokal in der pom.xml steht.
RUN ./mvnw -B -DskipTests -DskipUTs=true -Djava.version=26 package \
 && cp target/*.jar /build/app.jar


# ===== Stufe 2: Laufzeit =====
FROM eclipse-temurin:26-jre

# R und alle Pakete, die deg.R lädt. Die Ubuntu-Pakete (r-bioc-*, r-cran-*)
# sind fertig kompiliert, dadurch dauert der Build Minuten statt einer Stunde.
RUN apt-get update \
 && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
      r-base-core \
      r-bioc-deseq2 \
      r-bioc-annotationdbi \
      r-bioc-org.hs.eg.db \
      r-cran-ggplot2 \
      r-cran-ggrepel \
      r-cran-plotly \
      r-cran-htmlwidgets \
 && rm -rf /var/lib/apt/lists/*

# Schneller Test beim Bauen: bricht ab, falls ein R-Paket fehlt
RUN Rscript -e 'for (p in c("DESeq2","ggplot2","ggrepel","plotly","htmlwidgets","AnnotationDbi","org.Hs.eg.db")) suppressPackageStartupMessages(library(p, character.only = TRUE)); cat("R-Pakete OK\n")'

WORKDIR /app
COPY --from=build /build/app.jar /app/app.jar
COPY src/main/r/deg.R /app/r/deg.R

# Hier landen Uploads und Analyse-Ergebnisse (als Volume eingebunden)
RUN mkdir -p /data
VOLUME /data

# Feste Pfade für den AnalysisWorker innerhalb des Containers
ENV APP_R_EXECUTABLE=/usr/bin/Rscript \
    APP_R_SCRIPTPATH=/app/r/deg.R \
    APP_STORAGE_BASEDIR=/data

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
