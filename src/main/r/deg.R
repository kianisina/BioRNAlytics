args <- commandArgs(trailingOnly = TRUE)
if (length(args) == 0) { stop("FATAL ERROR: No job directory provided!") }
job_dir <- args[1]
setwd(job_dir)
cat("Starting DESeq2 Analysis in directory:", job_dir, "\n")

# LOAD LIBRARIES
suppressPackageStartupMessages(library(DESeq2))
suppressPackageStartupMessages(library(ggplot2))
suppressPackageStartupMessages(library(ggrepel))
suppressPackageStartupMessages(library(plotly))
suppressPackageStartupMessages(library(htmlwidgets))
suppressPackageStartupMessages(library(AnnotationDbi)) # NEW
suppressPackageStartupMessages(library(org.Hs.eg.db))  # NEW (Human Database)


# READ THE DATA
# Read the first few lines to detect the separator
sample_lines <- readLines("counts.csv", n = 5)

# Grab the first line that does NOT start with a '#'
first_data_line <- sample_lines[!grepl("^#", sample_lines)][1]

# check if the actual data uses tabs or commas
if (grepl("\t", first_data_line)) {
  cat("Detected Tab-Separated featureCounts file.\n")
  counts <- read.delim("counts.csv", comment.char="#", check.names=FALSE, row.names=1)
} else {
  cat("Detected Comma-Separated CSV file.\n")
  counts <- read.csv("counts.csv", comment.char="#", check.names=FALSE, row.names=1)
}

coldata <- read.csv("coldata.csv", row.names=1, check.names=FALSE)

# Clean up BAM/SAM suffixes
colnames(counts) <- basename(colnames(counts))
colnames(counts) <- gsub("\\.bam$", "", colnames(counts), ignore.case = TRUE)
colnames(counts) <- gsub("\\.sam$", "", colnames(counts), ignore.case = TRUE)
colnames(counts) <- gsub("\\.sorted$", "", colnames(counts), ignore.case = TRUE)

# If it's a raw featureCounts file, delete the genomic metadata columns.
metadata_cols <- c("Chr", "Start", "End", "Strand", "Length")
counts <- counts[, !(colnames(counts) %in% metadata_cols)]
# DATA WRANGLING & SAFETY CHECKS
coldata$condition <- as.factor(coldata$condition)
common_samples <- intersect(colnames(counts), rownames(coldata))

if (length(common_samples) == 0) {
  stop("FATAL ERROR: Sample names in coldata do not match the counts matrix!")
}

counts <- counts[, common_samples, drop=FALSE]
coldata <- coldata[common_samples, , drop=FALSE]
counts <- round(counts)


cat("Running Core DESeq2 Model...\n")
dds <- DESeqDataSetFromMatrix(countData = counts, colData = coldata, design = ~ condition)
dds <- DESeq(dds)


cat("Generating PCA Plot...\n")
# VST (Variance Stabilizing Transformation) normalizes data perfectly for PCA
vsd <- vst(dds, blind=FALSE)
pcaData <- plotPCA(vsd, intgroup=c("condition"), returnData=TRUE)
percentVar <- round(100 * attr(pcaData, "percentVar"))

pca_plot <- ggplot(pcaData, aes(PC1, PC2, color=condition)) +
  geom_point(size=3) +
  geom_text_repel(aes(label = rownames(pcaData)),
                  size = 4,
                  box.padding = 0.5,
                  point.padding = 0.5,
                  segment.color = "grey50")+
  # theme_minimal() +
  labs(title="PCA Plot: Sample Clustering",
       x=paste0("PC1: ", percentVar[1], "% variance"),
       y=paste0("PC2: ", percentVar[2], "% variance"))

ggsave("dataset_pca.png", plot=pca_plot, width=8, height=6, dpi=300)
# -----------------------------


# Keep only genes where the sum of counts across all samples is at least 10
keep <- rowSums(counts(dds)) >= 10
dds <- dds[keep,]

# GENERATE ALL PAIRWISE COMPARISONS
# Get all unique conditions
conditions <- levels(coldata$condition)

# Create a matrix of all possible pairs
# Create a dataframe of ALL permutations (25 total: 5x5)
all_combinations <- expand.grid(group1 = conditions, group2 = conditions)

# Remove the self-comparisons (e.g., control vs control)
valid_pairs <- all_combinations[all_combinations$group1 != all_combinations$group2, ]

# Convert it into a 2-row matrix so it perfectly matches your existing 'pairs' format
pairs <- t(valid_pairs)

cat("Found", ncol(pairs), "possible comparisons. Generating results...\n")

# Loop through every single pair
for (i in 1:ncol(pairs)) {

  group1 <- pairs[1, i]
  group2 <- pairs[2, i]
  comparison_name <- paste0(group1, "_vs_", group2)
  cat("Processing:", comparison_name, "\n")

  res <- results(dds, contrast=c("condition", group1, group2))
  res_df <- as.data.frame(res)

  # Add Significance categories
  res_df$Significance <- "Not Significant"
  valid_rows <- !is.na(res_df$padj)
  res_df$Significance[valid_rows & res_df$padj < 0.05 & res_df$log2FoldChange > 1] <- "Upregulated"
  res_df$Significance[valid_rows & res_df$padj < 0.05 & res_df$log2FoldChange < -1] <- "Downregulated"

  # Explicitly save Gene Names as a real column so Vue can read it
  res_df$Gene <- rownames(res_df)

  if (any(grepl("^ENSG", res_df$Gene))) {
    cat("Detected Ensembl IDs. Translating to Gene Symbols...\n")

    # Sometimes featureCounts leaves version numbers (e.g. ENSG000.14). We strip the .14 to match the dictionary.
    clean_ids <- gsub("\\.[0-9]+$", "", res_df$Gene)

    # Query the offline human dictionary
    mapped_symbols <- mapIds(org.Hs.eg.db,
                             keys = clean_ids,
                             column = "SYMBOL",
                             keytype = "ENSEMBL",
                             multiVals = "first")

    # If a gene doesn't have a known symbol, keep the original ENSG ID so it isn't blank
    mapped_symbols[is.na(mapped_symbols)] <- res_df$Gene[is.na(mapped_symbols)]
    res_df$Symbol <- mapped_symbols
  }
  cat("Generating Heatmap matrix for", comparison_name, "\n")

  # Get the top 50 significant genes sorted by lowest p-value
  top_genes <- head(res_df$Gene[valid_rows & res_df$padj < 0.05], 50)

  # Only proceed if we actually have significant genes
  if (length(top_genes) > 0) {
    # 2. Extract their normalized counts from the 'vsd' object
    mat <- assay(vsd)[top_genes, , drop=FALSE]

    # Center the data (calculate Z-scores across rows)
    mat <- mat - rowMeans(mat)

    
    # Calculate the mathematical distance between genes
    gene_dist <- dist(mat)
    # Build the hierarchical tree
    gene_tree <- hclust(gene_dist)
    # Reorder the matrix rows to match the tree!
    mat <- mat[gene_tree$order, , drop=FALSE]
   

    # Format for export
    heatmap_df <- as.data.frame(mat)
    heatmap_df$Gene <- rownames(heatmap_df)

    # Add symbols if we translated them earlier
    if ("Symbol" %in% colnames(res_df)) {
      heatmap_df$Symbol <- res_df$Symbol[match(heatmap_df$Gene, res_df$Gene)]
    }

    # Save the heatmap matrix
    write.csv(heatmap_df, file=paste0(comparison_name, "_heatmap.csv"), row.names=FALSE)
  }

  # ONLY EXPORT CSV
  csv_filename <- paste0(comparison_name, "_results.csv")
  write.csv(res_df, file=csv_filename, row.names=FALSE)
}
writeLines("done", "status.txt")
cat("PIPELINE COMPLETED SUCCESSFULLY!\n")
