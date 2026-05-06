# Install all R packages needed for the benchmark.
# Run once before executing benchmark.R:
#   Rscript benchmarks/r/install.R

pkgs <- c("forecast", "tseries", "vars", "TTR", "KFAS")

missing <- pkgs[!pkgs %in% installed.packages()[, "Package"]]
if (length(missing) > 0) {
  cat("Installing:", paste(missing, collapse = ", "), "\n")
  install.packages(missing, repos = "https://cloud.r-project.org")
} else {
  cat("All packages already installed.\n")
}
