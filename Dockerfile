# ---------------------------------------------------------------
# BioRNAlytics – Docker image
#
# Stage 1 builds the frontend (Vue/Vite) and backend (Spring Boot) into
# a single .jar file. Stage 2 is the slim runtime image with Java 26
# and R including DESeq2 for the analysis.
# ---------------------------------------------------------------

# ===== Stage 1: build =====
FROM eclipse-temurin:26-jdk AS build
WORKDIR /build

# Copy only the Maven files first so dependencies are cached and not
# downloaded again on every code change.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline || true

# Remaining source code (Java, Vue, Vite configuration)
COPY . .

# The frontend-maven-plugin in pom.xml installs Node, runs "npm install"
# and "npm run build" and writes the result to src/main/resources/public.
# Then the .jar is built.
# Tests are skipped because they need a running MongoDB.
# -Djava.version=26 matches the target version to the JDK in this image,
# regardless of the version set locally in pom.xml.
# chmod again because "COPY . ." overwrote mvnw without the execute bit
RUN chmod +x mvnw \
 && ./mvnw -B -DskipTests -DskipUTs=true -Djava.version=26 package \
 && cp target/*.jar /build/app.jar


# ===== Stage 2: runtime =====
FROM eclipse-temurin:26-jre

# R and all packages loaded by deg.R. The Ubuntu packages (r-bioc-*, r-cran-*)
# are precompiled, so the build takes minutes instead of an hour.
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

# Quick check during the build: fails if an R package is missing
RUN Rscript -e 'for (p in c("DESeq2","ggplot2","ggrepel","plotly","htmlwidgets","AnnotationDbi","org.Hs.eg.db")) suppressPackageStartupMessages(library(p, character.only = TRUE)); cat("R-Pakete OK\n")'

WORKDIR /app
COPY --from=build /build/app.jar /app/app.jar
COPY src/main/r/deg.R /app/r/deg.R

# Uploads and analysis results are stored here (mounted as a volume)
RUN mkdir -p /data
VOLUME /data

# Fixed paths for the AnalysisWorker inside the container
ENV APP_R_EXECUTABLE=/usr/bin/Rscript \
    APP_R_SCRIPTPATH=/app/r/deg.R \
    APP_STORAGE_BASEDIR=/data

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
