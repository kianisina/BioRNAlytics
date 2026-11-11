# RNAlytics

[![CI](https://github.com/kianisina/BioRNAlytics/actions/workflows/ci.yml/badge.svg)](https://github.com/kianisina/BioRNAlytics/actions/workflows/ci.yml)

RNAlytics is a web-based bioinformatics platform designed to simplify RNA-seq differential gene expression analysis for researchers and biologists. The platform allows users to upload gene expression count matrices, perform statistical analysis, visualize results, and download processed data without requiring advanced programming knowledge.

## Features

-  **Differential Gene Expression Analysis**
    - Upload RNA-seq count data
    - Compare experimental groups
    - Identify significantly differentially expressed genes (DEGs)
    - Calculate log2 fold changes and adjusted p-values

-  **Interactive Visualizations**
    - PCA plot
    - Volcano plots for DEG overview
    - Heatmaps for expression patterns
    - Visualization of upregulated and downregulated genes

-  **Data Export**
    - Download processed expression results as CSV files
    - Export DEG tables for downstream analysis

-  **User Accounts**
    - Registration with email verification (confirmation link valid for 24 hours)
    - Password reset via email
    - Admin user management (lock and unlock accounts)
    - Customizable corporate design (logo, colors, imprint)

-  **User-Friendly Interface**
    - Designed for researchers without extensive bioinformatics programming experience
    - Streamlined workflow from raw count matrix to biological interpretation

## Workflow

1. Upload RNA-seq count matrix as .txt or csv
2. Define experimental conditions/groups
3. Run differential expression analysis
4. Explore results using:
   - PCA plot
   - Volcano plots
   - Heatmaps
   - DEG tables
5. Download results for further analysis

## Supported Analysis

RNAlytics uses established bioinformatics methods for differential expression analysis, including:

- Count normalization
- Statistical testing
- Multiple testing correction
- Fold-change estimation
- DEG identification

## Input

RNAlytics accepts:

- Gene expression count matrices (CSV / txt format)
- Sample metadata describing experimental conditions


## Output

RNAlytics generates:

- PCA plot
- Differential expression table
- Adjusted p-values (FDR)
- Log2 fold changes
- Volcano plot
- Heatmap
- Downloadable CSV results

## Quick Start with Docker

The easiest way to run RNAlytics is with Docker. You only need [Docker Desktop](https://www.docker.com/products/docker-desktop/) (or Docker Engine with the Compose plugin). Java, R, DESeq2, MongoDB and RabbitMQ are all included in the containers.

```bash
git clone https://github.com/kianisina/BioRNAlytics.git
cd BioRNAlytics
cp .env.example .env        # optional: set your own JWT secret
docker compose up --build
```

The first build takes several minutes (it installs R and the Bioconductor packages). Afterwards the following services are available:

| Service | URL |
|---|---|
| RNAlytics web app | http://localhost:8080 |
| Mail inbox (verification and password reset emails) | http://localhost:8025 |
| RabbitMQ management | http://localhost:15672 (guest / guest) |

Stop everything with `docker compose down`. Add `-v` to also delete the database and all analysis results.

### First steps

1. Open http://localhost:8080 and create an account via **Konto erstellen**.
2. Open the mail inbox at http://localhost:8025 and click the verification link in the email.
3. Log in and start an analysis by uploading a count matrix and sample metadata.

No real emails are sent: [Mailpit](https://mailpit.axllent.org/) catches every email the application sends.

### Configuration

All settings have working defaults. To change them, copy `.env.example` to `.env` and set any of these variables. The `.env` file is git-ignored and must never be committed.

| Variable | Default | Purpose |
|---|---|---|
| `JWT_SECRET` | development key | Secret for signing login tokens (at least 64 characters) |
| `MONGODB_URI` | local MongoDB container | Use an existing database instead, e.g. MongoDB Atlas |
| `ANALYSIS_DATA_DIR` | Docker volume | Host folder for uploads and analysis results |
| `APP_BASE_URL` | `http://localhost:8080` | Public URL used in links inside emails |

## Architecture

```
Browser (Vue + Quasar)
   │  REST API, JWT authentication
   ▼
Spring Boot backend ──── MongoDB (users, settings, analysis jobs)
   │
   │  job ID
   ▼
RabbitMQ queue ──► AnalysisWorker ──► Rscript deg.R (DESeq2)
                                          │
                                          ▼
                              result files (CSV, PNG, HTML plots)
```

An uploaded analysis is stored as a job in MongoDB and its ID is put into a RabbitMQ queue. The `AnalysisWorker` processes one job at a time: it runs the R script `src/main/r/deg.R` on the job folder and marks the job as `COMPLETED` or `FAILED`. The frontend then loads the plots and tables from the backend.

## Development without Docker

Requirements:

- Java 26
- R with the packages DESeq2, AnnotationDbi, org.Hs.eg.db, ggplot2, ggrepel, plotly and htmlwidgets
- MongoDB 7 and RabbitMQ (for example via `docker compose up -d mongo rabbitmq`)

Node.js does not need to be installed: Maven downloads it and builds the frontend automatically.

Create `src/main/resources/application.properties` (git-ignored, never commit it) with at least:

```properties
spring.mongodb.uri=mongodb://localhost:27017/biornalytics
spring.rabbitmq.host=localhost
spring.mail.host=localhost
spring.mail.port=1025

security.auth-login-url=/api/authenticate
security.jwt-secret=<random value, at least 64 characters>
security.token-header=Authorization
security.token-prefix=Bearer
security.token-type=JWT
security.token-issuer=secure-api
security.token-audience=secure-app

app.base-url=http://localhost:8080
app.storage.base-dir=<folder for uploads and results>
app.r.executable=<path to Rscript>
app.r.script-path=src/main/r/deg.R
```

Then start the application with `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`) and open http://localhost:8080.

## Tests and CI

The tests start the complete Spring application and need MongoDB and RabbitMQ on localhost:

```bash
docker compose up -d mongo rabbitmq
./mvnw verify
```

Tests use their own configuration in `src/test/resources/application.properties`, so they never touch a real database.

GitHub Actions runs two jobs on every push and pull request:

- **build-and-test**: builds frontend and backend and runs the tests
- **docker-build**: checks that the Docker image can be built

## Troubleshooting

- **The old logo or old pages still appear after an update:** the app is a progressive web app and caches files in the browser. Reload with `Ctrl + F5`.
- **Login says the email address is not verified:** click the link in the verification email (Docker: http://localhost:8025). An admin can also unlock the account in the user management.
- **MongoDB version:** the project uses MongoDB 7 because MongoDB 8 refuses to start on Linux kernel 6.19 and newer ([SERVER-121912](https://jira.mongodb.org/browse/SERVER-121912)).
- **Port already in use:** stop other services on ports 8080, 8025, 5672 or 15672, or change the port mapping in `docker-compose.yml`.

## Technologies

- Backend: Java 26, Spring Boot 4, Spring Security (JWT)
- Analysis: R, DESeq2
- Visualization: ggplot2 / Plotly
- Frontend: Vue 3, Quasar, Pinia
- Database: MongoDB
- Job queue: RabbitMQ
- Deployment: Docker, Docker Compose
- CI: GitHub Actions

## License

RNAlytics is released under the MIT License.
