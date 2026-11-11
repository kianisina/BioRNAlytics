# RNAlytics

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
| Mail inbox (password reset emails) | http://localhost:8025 |
| RabbitMQ management | http://localhost:15672 (guest / guest) |

Stop everything with `docker compose down`. Add `-v` to also delete the database and all analysis results.

## Technologies

- Backend: Java / R
- Differential expression: DESeq2
- Visualization: ggplot2 / Plotly
- Frontend: Vue
- Database: MongoDB

## License

RNAlytics is released under the MIT License.