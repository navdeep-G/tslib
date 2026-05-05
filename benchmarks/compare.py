"""
Merge tslib / Python / R benchmark results and print a comparison table.

Usage (from project root):
    python benchmarks/compare.py

Requires pandas:
    pip install pandas
"""

import sys
from pathlib import Path

try:
    import pandas as pd
except ImportError:
    sys.exit("pandas is required: pip install pandas")

RESULTS_DIR = Path(__file__).resolve().parent / "results"
FILES = {
    "tslib":  RESULTS_DIR / "java_results.csv",
    "python": RESULTS_DIR / "python_results.csv",
    "r":      RESULTS_DIR / "r_results.csv",
}

ACCURACY_METRICS = {"MAE", "RMSE", "MAPE"}
SPEED_METRIC     = "exec_ms"
OTHER_METRICS    = {"smoothing_mae", "smoothing_rmse", "seasonal_strength",
                    "trend_strength", "remainder_var", "statistic", "pvalue",
                    "stationary_5pct"}


def load_all() -> pd.DataFrame:
    frames = []
    for lib, path in FILES.items():
        if not path.exists():
            print(f"  [skip] {path.name} not found – run that benchmark first.")
            continue
        df = pd.read_csv(path)
        df["library"] = lib
        frames.append(df)
    if not frames:
        sys.exit("No result files found. Run at least one benchmark first.")
    return pd.concat(frames, ignore_index=True)


def pivot_table(df: pd.DataFrame, metrics: set) -> pd.DataFrame | None:
    sub = df[df["metric"].isin(metrics)].copy()
    if sub.empty:
        return None
    sub["value"] = pd.to_numeric(sub["value"], errors="coerce")
    pt = sub.pivot_table(
        index=["algorithm", "dataset"],
        columns=["library", "metric"],
        values="value",
        aggfunc="first",
    )
    pt.columns = [f"{lib}_{metric}" for lib, metric in pt.columns]
    return pt.reset_index()


def section(title: str) -> None:
    print(f"\n{'='*80}")
    print(f"  {title}")
    print(f"{'='*80}")


def fmt_row(row: dict, cols: list[str]) -> str:
    parts = []
    for c in cols:
        val = row.get(c)
        if pd.isna(val) if val is not None else True:
            parts.append("   N/A  ")
        elif isinstance(val, float):
            parts.append(f"{val:8.4f}")
        else:
            parts.append(str(val)[:8].rjust(8))
    return "  ".join(parts)


def print_comparison(df: pd.DataFrame, metrics: list[str], title: str) -> None:
    section(title)
    libraries = sorted({c.split("_")[0] for c in df.columns if "_" in c})
    for metric in metrics:
        cols = [f"{lib}_{metric}" for lib in libraries if f"{lib}_{metric}" in df.columns]
        if not cols:
            continue
        header_parts = [f"{'algorithm':<40}", f"{'dataset':<20}"] + \
                       [f"{lib:>10}" for lib in libraries if f"{lib}_{metric}" in df.columns]
        sep = "-" * (40 + 20 + 12 * len(cols) + 4)
        print(f"\n  --- {metric} ---")
        print("  " + "  ".join(header_parts))
        print("  " + sep)
        for _, row in df.iterrows():
            vals = [f"{row.get(c, float('nan')):10.4f}"
                    if pd.notna(row.get(c)) else "       N/A"
                    for c in cols]
            print(f"  {row['algorithm']:<40}  {row['dataset']:<20}  " + "  ".join(vals))


def speed_summary(df: pd.DataFrame) -> None:
    section("Execution Speed (exec_ms, median of 5 runs)")
    speed_cols = [c for c in df.columns if c.endswith("_exec_ms")]
    libraries  = [c.replace("_exec_ms", "") for c in speed_cols]
    header = f"  {'algorithm':<40}  {'dataset':<20}" + \
             "".join(f"  {lib:>10}" for lib in libraries)
    print(header)
    print("  " + "-" * len(header))
    for _, row in df.iterrows():
        vals = "".join(
            f"  {row[c]:10.3f}" if pd.notna(row.get(c)) else "       N/A"
            for c in speed_cols
        )
        print(f"  {row['algorithm']:<40}  {row['dataset']:<20}{vals}")
    print()
    print("  Note: Java times include JVM warmup amortisation (2 warm-up + 5 timed runs).")
    print("        Python/R times are wall-clock (cold interpreter start excluded).")


def main() -> None:
    df = load_all()

    acc_pt   = pivot_table(df, ACCURACY_METRICS)
    speed_pt = pivot_table(df, {SPEED_METRIC})
    other_pt = pivot_table(df, OTHER_METRICS)

    if acc_pt is not None:
        print_comparison(acc_pt, ["MAE", "RMSE", "MAPE"],
                         "Forecast Accuracy on Holdout Set")

    if other_pt is not None:
        print_comparison(other_pt,
                         ["smoothing_mae", "smoothing_rmse",
                          "seasonal_strength", "trend_strength", "remainder_var",
                          "statistic", "pvalue"],
                         "Smoothing / Decomposition / Statistical Tests")

    if speed_pt is not None:
        speed_summary(speed_pt)

    # write merged CSV
    all_pt = pivot_table(df, {m for ms in [ACCURACY_METRICS, {SPEED_METRIC}, OTHER_METRICS]
                               for m in ms})
    if all_pt is not None:
        out = RESULTS_DIR / "comparison.csv"
        all_pt.to_csv(out, index=False)
        print(f"\nMerged table written to: {out}")


if __name__ == "__main__":
    main()
