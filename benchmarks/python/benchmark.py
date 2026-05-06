"""
Python benchmark comparing statsmodels / pmdarima / pandas against tslib.

Run from the project root:
    cd benchmarks/python
    pip install -r requirements.txt
    python benchmark.py

Or from anywhere:
    python benchmarks/python/benchmark.py [--root /path/to/tslib]

Output: benchmarks/results/python_results.csv
Format: library,algorithm,dataset,metric,value
"""

import argparse
import csv
import os
import sys
import time
import warnings
from pathlib import Path

import numpy as np
import pandas as pd

warnings.filterwarnings("ignore")

# ── locate project root ────────────────────────────────────────────────────────

def find_root(start: Path) -> Path:
    for parent in [start, *start.parents]:
        if (parent / "data" / "hotel.txt").exists():
            return parent
    raise FileNotFoundError("Cannot find tslib project root (no data/hotel.txt)")

THIS_FILE = Path(__file__).resolve()
DEFAULT_ROOT = find_root(THIS_FILE)

WARMUP_RUNS = 2
TIMED_RUNS  = 5


# ── data loading ──────────────────────────────────────────────────────────────

def load_txt(path: Path) -> np.ndarray:
    return np.array([float(l.strip()) for l in path.read_text().splitlines() if l.strip()])

def load_csv(path: Path) -> np.ndarray:
    df = pd.read_csv(path)
    return df.iloc[:, 0].values.astype(float)


# ── timing helpers ────────────────────────────────────────────────────────────

def median_ms(fn, *args, **kwargs):
    """Return (result, median_elapsed_ms) over TIMED_RUNS executions."""
    for _ in range(WARMUP_RUNS):
        fn(*args, **kwargs)
    times = []
    result = None
    for _ in range(TIMED_RUNS):
        t0 = time.perf_counter()
        result = fn(*args, **kwargs)
        times.append(time.perf_counter() - t0)
    return result, float(np.median(times)) * 1000.0


# ── metric helpers ────────────────────────────────────────────────────────────

def mae(actual, pred):
    a, p = np.array(actual), np.array(pred)
    return float(np.mean(np.abs(a - p)))

def rmse(actual, pred):
    a, p = np.array(actual), np.array(pred)
    return float(np.sqrt(np.mean((a - p) ** 2)))

def mape(actual, pred):
    a, p = np.array(actual), np.array(pred)
    mask = a != 0
    return float(np.mean(np.abs((a[mask] - p[mask]) / a[mask])) * 100)


# ── CSV writer ────────────────────────────────────────────────────────────────

def write_csv(rows: list[list], path: Path):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", newline="") as f:
        w = csv.writer(f)
        w.writerows(rows)
    print(f"\nResults written to: {path}")


# ── individual benchmarks ─────────────────────────────────────────────────────

def bench_arima(data: np.ndarray, dataset: str) -> list[list]:
    from statsmodels.tsa.arima.model import ARIMA as smARIMA

    holdout = 12
    train, test = data[:-holdout], data[-holdout:]

    def run():
        fit = smARIMA(train, order=(1, 1, 1)).fit()
        return fit.forecast(holdout)

    fc, ms = median_ms(run)
    m, r, p = mae(test, fc), rmse(test, fc), mape(test, fc)
    print(f"  ARIMA(1,1,1) [{dataset}]  MAE={m:.4f}  RMSE={r:.4f}  MAPE={p:.2f}%  exec={ms:.3f} ms")
    return [
        ["python", "ARIMA_1_1_1", dataset, "MAE",     f"{m:.6f}"],
        ["python", "ARIMA_1_1_1", dataset, "RMSE",    f"{r:.6f}"],
        ["python", "ARIMA_1_1_1", dataset, "MAPE",    f"{p:.6f}"],
        ["python", "ARIMA_1_1_1", dataset, "exec_ms", f"{ms:.6f}"],
    ]


def bench_sarima(data: np.ndarray, dataset: str) -> list[list]:
    from statsmodels.tsa.statespace.sarimax import SARIMAX

    holdout = 12
    train, test = data[:-holdout], data[-holdout:]

    def run():
        fit = SARIMAX(train, order=(1, 1, 1), seasonal_order=(1, 1, 0, 12)).fit(disp=False)
        return fit.forecast(holdout)

    fc, ms = median_ms(run)
    m, r, p = mae(test, fc), rmse(test, fc), mape(test, fc)
    print(f"  SARIMA(1,1,1)(1,1,0,12) [{dataset}]  MAE={m:.4f}  RMSE={r:.4f}  MAPE={p:.2f}%  exec={ms:.3f} ms")
    algo = "SARIMA_1_1_1_1_1_0_12"
    return [
        ["python", algo, dataset, "MAE",     f"{m:.6f}"],
        ["python", algo, dataset, "RMSE",    f"{r:.6f}"],
        ["python", algo, dataset, "MAPE",    f"{p:.6f}"],
        ["python", algo, dataset, "exec_ms", f"{ms:.6f}"],
    ]


def bench_single_ets(data: np.ndarray, dataset: str) -> list[list]:
    from statsmodels.tsa.holtwinters import SimpleExpSmoothing

    holdout = 8
    train, test = data[:-holdout], data[-holdout:]

    def run():
        fit = SimpleExpSmoothing(train).fit(smoothing_level=0.3, optimized=False)
        return fit.forecast(holdout)

    fc, ms = median_ms(run)
    m, r, p = mae(test, fc), rmse(test, fc), mape(test, fc)
    print(f"  SingleETS(α=0.3) [{dataset}]  MAE={m:.4f}  RMSE={r:.4f}  MAPE={p:.2f}%  exec={ms:.3f} ms")
    algo = "SingleETS_a0.3"
    return [
        ["python", algo, dataset, "MAE",     f"{m:.6f}"],
        ["python", algo, dataset, "RMSE",    f"{r:.6f}"],
        ["python", algo, dataset, "MAPE",    f"{p:.6f}"],
        ["python", algo, dataset, "exec_ms", f"{ms:.6f}"],
    ]


def bench_double_ets(data: np.ndarray, dataset: str) -> list[list]:
    from statsmodels.tsa.holtwinters import Holt

    holdout = 12
    train, test = data[:-holdout], data[-holdout:]

    # tslib DoubleExpSmoothing: alpha=level, gamma=trend
    # statsmodels Holt:         smoothing_level=alpha, smoothing_trend=beta
    def run():
        fit = Holt(train).fit(smoothing_level=0.3, smoothing_trend=0.1, optimized=False)
        return fit.forecast(holdout)

    fc, ms = median_ms(run)
    m, r, p = mae(test, fc), rmse(test, fc), mape(test, fc)
    print(f"  DoubleETS(α=0.3,trend=0.1) [{dataset}]  MAE={m:.4f}  RMSE={r:.4f}  MAPE={p:.2f}%  exec={ms:.3f} ms")
    algo = "DoubleETS_a0.3_g0.1"
    return [
        ["python", algo, dataset, "MAE",     f"{m:.6f}"],
        ["python", algo, dataset, "RMSE",    f"{r:.6f}"],
        ["python", algo, dataset, "MAPE",    f"{p:.6f}"],
        ["python", algo, dataset, "exec_ms", f"{ms:.6f}"],
    ]


def bench_triple_ets(data: np.ndarray, dataset: str) -> list[list]:
    from statsmodels.tsa.holtwinters import ExponentialSmoothing

    holdout = 12
    train, test = data[:-holdout], data[-holdout:]

    # tslib TripleExpSmoothing: alpha=level, beta=seasonal, gamma=trend
    # statsmodels:              smoothing_level, smoothing_trend, smoothing_seasonal
    def run():
        fit = ExponentialSmoothing(
            train, trend="add", seasonal="mul", seasonal_periods=12
        ).fit(
            smoothing_level=0.3,
            smoothing_trend=0.1,   # maps to tslib gamma (trend)
            smoothing_seasonal=0.2, # maps to tslib beta  (seasonal)
            optimized=False,
        )
        return fit.forecast(holdout)

    fc, ms = median_ms(run)
    m, r, p = mae(test, fc), rmse(test, fc), mape(test, fc)
    print(f"  HoltWinters(α=0.3,trend=0.1,seasonal=0.2,s=12) [{dataset}]  "
          f"MAE={m:.4f}  RMSE={r:.4f}  MAPE={p:.2f}%  exec={ms:.3f} ms")
    algo = "HoltWinters_a0.3_b0.2_g0.1_s12"
    return [
        ["python", algo, dataset, "MAE",     f"{m:.6f}"],
        ["python", algo, dataset, "RMSE",    f"{r:.6f}"],
        ["python", algo, dataset, "MAPE",    f"{p:.6f}"],
        ["python", algo, dataset, "exec_ms", f"{ms:.6f}"],
    ]


def bench_sma(data: np.ndarray, dataset: str) -> list[list]:
    series = pd.Series(data)

    def run():
        return series.rolling(window=7).mean()

    smoothed, ms = median_ms(run)
    valid = smoothed.dropna()
    actual = series.iloc[6:]
    m = mae(actual.values, valid.values)
    r = rmse(actual.values, valid.values)
    print(f"  SMA(window=7) [{dataset}]  smoothing_mae={m:.4f}  rmse={r:.4f}  exec={ms:.3f} ms")
    return [
        ["python", "SMA_window7", dataset, "smoothing_mae",  f"{m:.6f}"],
        ["python", "SMA_window7", dataset, "smoothing_rmse", f"{r:.6f}"],
        ["python", "SMA_window7", dataset, "exec_ms",        f"{ms:.6f}"],
    ]


def bench_ema(data: np.ndarray, dataset: str) -> list[list]:
    series = pd.Series(data)

    # pandas ewm adjust=False matches the tslib recursive EMA formula
    def run():
        return series.ewm(alpha=0.2, adjust=False).mean()

    smoothed, ms = median_ms(run)
    m = mae(data, smoothed.values)
    r = rmse(data, smoothed.values)
    print(f"  EMA(α=0.2) [{dataset}]  smoothing_mae={m:.4f}  rmse={r:.4f}  exec={ms:.3f} ms")
    return [
        ["python", "EMA_a0.2", dataset, "smoothing_mae",  f"{m:.6f}"],
        ["python", "EMA_a0.2", dataset, "smoothing_rmse", f"{r:.6f}"],
        ["python", "EMA_a0.2", dataset, "exec_ms",        f"{ms:.6f}"],
    ]


def bench_stl(data: np.ndarray, dataset: str) -> list[list]:
    from statsmodels.tsa.seasonal import STL

    series = pd.Series(data)

    def run():
        return STL(series, period=12, seasonal=7).fit()

    result, ms = median_ms(run)
    rem_var = float(np.var(result.resid))
    seas_var = float(np.var(result.seasonal))
    trend_var = float(np.var(result.trend))
    seas_str = max(0, 1 - rem_var / (seas_var + rem_var))
    trend_str = max(0, 1 - rem_var / (trend_var + rem_var))
    print(f"  STL(period=12) [{dataset}]  seasonal_strength={seas_str:.4f}  "
          f"trend_strength={trend_str:.4f}  remainder_var={rem_var:.4f}  exec={ms:.3f} ms")
    return [
        ["python", "STL_period12", dataset, "seasonal_strength", f"{seas_str:.6f}"],
        ["python", "STL_period12", dataset, "trend_strength",    f"{trend_str:.6f}"],
        ["python", "STL_period12", dataset, "remainder_var",     f"{rem_var:.6f}"],
        ["python", "STL_period12", dataset, "exec_ms",           f"{ms:.6f}"],
    ]


def bench_var(data1: np.ndarray, data2: np.ndarray) -> list[list]:
    from statsmodels.tsa.vector_ar.var_model import VAR

    holdout = 8
    train1, test1 = data1[:-holdout], data1[-holdout:]
    train2 = data2[:-holdout]
    train = np.column_stack([train1, train2])

    def run():
        model = VAR(train)
        fit = model.fit(maxlags=1, ic=None)
        return fit.forecast(train[-1:], steps=holdout)

    fc, ms = median_ms(run)
    fc1 = fc[:, 0]
    m = mae(test1, fc1)
    r = rmse(test1, fc1)
    p = mape(test1, fc1)
    print(f"  VAR(1) [hotel+ca_unemp]  MAE={m:.4f}  RMSE={r:.4f}  MAPE={p:.2f}%  exec={ms:.3f} ms")
    return [
        ["python", "VAR_p1", "hotel+ca_unemp", "MAE",     f"{m:.6f}"],
        ["python", "VAR_p1", "hotel+ca_unemp", "RMSE",    f"{r:.6f}"],
        ["python", "VAR_p1", "hotel+ca_unemp", "MAPE",    f"{p:.6f}"],
        ["python", "VAR_p1", "hotel+ca_unemp", "exec_ms", f"{ms:.6f}"],
    ]


def bench_adf(data: np.ndarray, dataset: str) -> list[list]:
    from statsmodels.tsa.stattools import adfuller

    def run():
        return adfuller(data, autolag="AIC")

    result, ms = median_ms(run)
    stat, pval = result[0], result[1]
    print(f"  ADF test [{dataset}]  stat={stat:.4f}  p={pval:.4f}  exec={ms:.3f} ms")
    return [
        ["python", "ADF_test", dataset, "statistic", f"{stat:.6f}"],
        ["python", "ADF_test", dataset, "pvalue",    f"{pval:.6f}"],
        ["python", "ADF_test", dataset, "exec_ms",   f"{ms:.6f}"],
    ]


def bench_kpss(data: np.ndarray, dataset: str) -> list[list]:
    from statsmodels.tsa.stattools import kpss

    def run():
        return kpss(data, regression="c", nlags="auto")

    result, ms = median_ms(run)
    stat, pval = result[0], result[1]
    print(f"  KPSS test [{dataset}]  stat={stat:.4f}  p={pval:.4f}  exec={ms:.3f} ms")
    return [
        ["python", "KPSS_test", dataset, "statistic", f"{stat:.6f}"],
        ["python", "KPSS_test", dataset, "pvalue",    f"{pval:.6f}"],
        ["python", "KPSS_test", dataset, "exec_ms",   f"{ms:.6f}"],
    ]


def bench_local_level(data: np.ndarray, dataset: str) -> list[list]:
    from statsmodels.tsa.statespace.structural import UnobservedComponents

    holdout = 8
    train, test = data[:-holdout], data[-holdout:]

    def run():
        model = UnobservedComponents(train, level="local level")
        fit = model.fit(disp=False)
        fc = fit.forecast(holdout)
        return fc

    fc, ms = median_ms(run)
    m = mae(test, fc)
    r = rmse(test, fc)
    p = mape(test, fc)
    print(f"  LocalLevel (MLE) [{dataset}]  MAE={m:.4f}  RMSE={r:.4f}  MAPE={p:.2f}%  exec={ms:.3f} ms")
    return [
        ["python", "LocalLevel_MLE", dataset, "MAE",     f"{m:.6f}"],
        ["python", "LocalLevel_MLE", dataset, "RMSE",    f"{r:.6f}"],
        ["python", "LocalLevel_MLE", dataset, "MAPE",    f"{p:.6f}"],
        ["python", "LocalLevel_MLE", dataset, "exec_ms", f"{ms:.6f}"],
    ]


def bench_auto_arima(data: np.ndarray, dataset: str) -> list[list]:
    import pmdarima as pm

    holdout = 12
    train, test = data[:-holdout], data[-holdout:]

    def run():
        model = pm.auto_arima(
            train,
            max_p=3, max_d=2, max_q=3,
            information_criterion="aic",
            stepwise=True,
            suppress_warnings=True,
            error_action="ignore",
        )
        return model.predict(n_periods=holdout)

    fc, ms = median_ms(run)
    m = mae(test, fc)
    r = rmse(test, fc)
    p = mape(test, fc)
    print(f"  AutoARIMA(max p,d,q=3) [{dataset}]  MAE={m:.4f}  RMSE={r:.4f}  MAPE={p:.2f}%  exec={ms:.3f} ms")
    algo = "AutoARIMA_max3_AIC"
    return [
        ["python", algo, dataset, "MAE",     f"{m:.6f}"],
        ["python", algo, dataset, "RMSE",    f"{r:.6f}"],
        ["python", algo, dataset, "MAPE",    f"{p:.6f}"],
        ["python", algo, dataset, "exec_ms", f"{ms:.6f}"],
    ]


# NA positions consistent with Java and R benchmark scripts
NA_POSITIONS = [15, 30, 45, 60, 75, 90, 105, 120, 135, 150]


def bench_wma(data: np.ndarray, dataset: str) -> list[list]:
    series = pd.Series(data)
    n = 7
    weights = np.arange(1, n + 1, dtype=float)

    def run():
        return series.rolling(n).apply(lambda x: np.dot(x, weights) / weights.sum(), raw=True)

    smoothed, ms = median_ms(run)
    valid = smoothed.dropna().values
    actual = data[n - 1:]
    m = mae(actual, valid)
    r = rmse(actual, valid)
    print(f"  WMA(period=7) [{dataset}]  smoothing_mae={m:.4f}  rmse={r:.4f}  exec={ms:.3f} ms")
    return [
        ["python", "WMA_period7", dataset, "smoothing_mae",  f"{m:.6f}"],
        ["python", "WMA_period7", dataset, "smoothing_rmse", f"{r:.6f}"],
        ["python", "WMA_period7", dataset, "exec_ms",        f"{ms:.6f}"],
    ]


def bench_ljung_box(data: np.ndarray, dataset: str) -> list[list]:
    from statsmodels.tsa.arima.model import ARIMA as smARIMA
    from statsmodels.stats.diagnostic import acorr_ljungbox

    fit = smARIMA(data, order=(1, 1, 1)).fit()
    residuals = fit.resid

    def run():
        return acorr_ljungbox(residuals, lags=[12], return_df=True)

    result, ms = median_ms(run)
    stat = float(result["lb_stat"].iloc[0])
    pval = float(result["lb_pvalue"].iloc[0])
    print(f"  Ljung-Box(lags=12) [{dataset}]  stat={stat:.4f}  p={pval:.4f}  exec={ms:.3f} ms")
    return [
        ["python", "LjungBox_lag12", dataset, "statistic", f"{stat:.6f}"],
        ["python", "LjungBox_lag12", dataset, "pvalue",    f"{pval:.6f}"],
        ["python", "LjungBox_lag12", dataset, "exec_ms",   f"{ms:.6f}"],
    ]


def bench_imputation(data: np.ndarray, dataset: str) -> list[list]:
    original = data.copy()
    corrupted = data.copy().astype(float)
    for pos in NA_POSITIONS:
        corrupted[pos] = np.nan
    series = pd.Series(corrupted)

    def run():
        return series.interpolate(method="linear")

    imputed, ms = median_ms(run)
    m = float(np.mean([abs(original[p] - imputed.iloc[p]) for p in NA_POSITIONS]))
    print(f"  Imputation (linear interp, 10 NAs) [{dataset}]  imputation_mae={m:.4f}  exec={ms:.3f} ms")
    return [
        ["python", "Imputation_linear", dataset, "imputation_mae", f"{m:.6f}"],
        ["python", "Imputation_linear", dataset, "exec_ms",        f"{ms:.6f}"],
    ]


def bench_boxcox(data: np.ndarray, dataset: str) -> list[list]:
    from scipy.stats import boxcox_normmax

    def run():
        return boxcox_normmax(data, method="mle")

    lam, ms = median_ms(run)
    print(f"  Box-Cox lambda search [{dataset}]  lambda={lam:.4f}  exec={ms:.3f} ms")
    return [
        ["python", "BoxCox_lambda", dataset, "lambda",  f"{lam:.6f}"],
        ["python", "BoxCox_lambda", dataset, "exec_ms", f"{ms:.6f}"],
    ]


# ── main ──────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=str(DEFAULT_ROOT))
    args = parser.parse_args()

    root = Path(args.root)
    hotel = load_txt(root / "data" / "hotel.txt")
    jj    = load_txt(root / "data" / "jj.txt")
    ap    = load_csv(root / "benchmarks" / "data" / "airpassengers.csv")
    ca    = load_txt(root / "data" / "CA_Unemployment_Rate.txt")[:168]

    results_path = root / "benchmarks" / "results" / "python_results.csv"

    rows: list[list] = [["library", "algorithm", "dataset", "metric", "value"]]

    print("=== Python Benchmarks ===\n")

    rows += bench_arima(hotel, "hotel")
    rows += bench_sarima(ap, "airpassengers")
    rows += bench_single_ets(jj, "jj")
    rows += bench_double_ets(hotel, "hotel")
    rows += bench_triple_ets(ap, "airpassengers")
    rows += bench_sma(hotel, "hotel")
    rows += bench_ema(hotel, "hotel")
    rows += bench_stl(ap, "airpassengers")
    rows += bench_var(hotel, ca)
    rows += bench_adf(hotel, "hotel")
    rows += bench_kpss(hotel, "hotel")
    rows += bench_local_level(jj, "jj")
    rows += bench_auto_arima(hotel, "hotel")
    rows += bench_wma(hotel, "hotel")
    rows += bench_ljung_box(hotel, "hotel")
    rows += bench_imputation(hotel, "hotel")
    rows += bench_boxcox(ap, "airpassengers")

    write_csv(rows, results_path)


if __name__ == "__main__":
    main()
