#!/usr/bin/env bash
# Prints curated highlights from every tslib example, one section per screen.
# Designed to be recorded by VHS into docs/demo.gif.

B=$'\033[1m'
C=$'\033[96m'   # bright cyan  — section headers
G=$'\033[92m'   # bright green — key labels
Y=$'\033[93m'   # bright yellow — command labels
D=$'\033[2m'    # dim
R=$'\033[0m'    # reset

hdr() {
  printf "${B}${C}┌─────────────────────────────────────────────────────────────────┐${R}\n"
  printf "${B}${C}│  %-65s│${R}\n" "$1"
  printf "${B}${C}└─────────────────────────────────────────────────────────────────┘${R}\n"
  echo
}

lbl() { printf "  ${Y}▸ %-38s${R}%s\n" "$1" "$2"; }
kv()  { printf "  ${G}%-26s${R}%s\n" "$1" "$2"; }
out() { printf "  ${D}%s${R}\n" "$1"; }
gap() { sleep "$1"; }

# ── INTRO ─────────────────────────────────────────────────────────────────────
clear
echo
printf "${B}${G}  tslib${R}${B}  —  Java Time Series Library${R}\n"
printf "${D}  Pure Java · Apache Commons Math 3 · JUnit 5 · Serializable models${R}\n"
echo
printf "  ${D}collect · movingaverage · dataquality · transform · tests${R}\n"
printf "  ${D}model/arima · model/expsmoothing · model/statespace${R}\n"
printf "  ${D}decomposition · selection · evaluation · diagnostics${R}\n"
echo
printf "  ${B}13 example modules — run: java tslib.examples.TslibExamples${R}\n"
gap 3

# ── 01 ────────────────────────────────────────────────────────────────────────
clear
hdr "01 · Data Ingestion & Statistics   (Collect, Stats)"
lbl "new Collect(data, k=1, n=10)" ""
echo
kv  "N / Mean / StdDev"  "120     127.51     16.39"
kv  "Min / Max"          "100.00 @ idx 0     161.97 @ idx 119"
kv  "Autocorrelation"    "lag=1 → 0.9670"
kv  "ACF   (lags 0–4)"   "0.992  0.967  0.944  0.922  0.900"
kv  "PACF  (lags 0–4)"   "1.000  0.975  0.022  0.006  −0.002"
kv  "ADF stat"           "−1.6050   →  isStationary: false"
echo
lbl "getLogTransformed() / getBoxCoxTransformed()" ""
lbl "getFirstDifference() / getRollingAverage(5)"  ""
gap 4.5

# ── 02 ────────────────────────────────────────────────────────────────────────
clear
hdr "02 · Moving Averages   (SMA · EMA · WMA · CMA)"
lbl "SimpleMovingAverage(period=5)" ""
out "   null  null  null  null  12.20  13.00  13.80  15.20  16.00  16.80  ..."
echo
lbl "ExponentialMovingAverage(alpha=0.3)" ""
out "   10.00  10.60  10.72  11.40  12.48  12.94  13.86  15.10  15.67  ..."
echo
lbl "WeightedMovingAverage(period=5)" ""
out "   null  null  null  null  12.93  13.53  14.53  15.93  16.53  ..."
echo
lbl "CumulativeMovingAverage()" ""
out "   10.00  11.00  11.00  11.50  12.20  12.50  13.00  13.63  14.00  ..."
echo
out "   Incremental add():  ema.compute(newValue) → running update"
gap 4.5

# ── 03 ────────────────────────────────────────────────────────────────────────
clear
hdr "03 · Data Quality   (Imputer · OutlierDetector · Winsorizer)"
lbl "Input (nulls at idx 2, 3, 6)" "[1, 2, null, null, 5, 6, null, ...]"
echo
kv  "FORWARD_FILL"          "[1, 2, 2, 2, 5, 6, 6, 8, 9, 10]"
kv  "BACKWARD_FILL"         "[1, 2, 5, 5, 5, 6, 8, 8, 9, 10]"
kv  "LINEAR_INTERPOLATION"  "[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]"
kv  "MEAN"                  "[1, 2, 5.86, 5.86, 5, 6, 5.86, 8, 9, 10]"
echo
lbl "OutlierDetector.zScore(data, threshold=2.5)"   "→ indices [5, 11]"
lbl "OutlierDetector.iqr(data, multiplier=1.5)"     "→ indices [5, 11]"
lbl "Winsorizer.winsorize(data, 5%, 95%)"           "→ 200→19.03 / −100→−4.05"
gap 5

# ── 04 ────────────────────────────────────────────────────────────────────────
clear
hdr "04 · Transformations   (Transform · Differencing)"
lbl "Transform.log(x)"           "[0.0, 1.386, 2.197, 2.773, ...]"
lbl "Transform.sqrt(x)"          "[1.0, 2.0, 3.0, 4.0, 5.0, ...]"
lbl "Transform.boxCox(x)"        "auto λ=−1.0 → [0.0, 0.75, ...]"
lbl "Transform.inverseBoxCox(·)" "[1.0, 4.0, 9.0, ...]  ✓ roundtrip"
echo
lbl "Differencing.difference(s)"      "[3.0, 4.0, 5.0, 6.0, 7.0, ...]"
lbl "Differencing.difference(s, d=2)" "[1.0, 1.0, 1.0, 1.0, 1.0, ...]"
lbl "Differencing.seasonalDiff(s, 4)" "[4.0, 4.0, 4.0, 4.0, 4.0, ...]"
lbl "Differencing.inverseDifference"  "→ fully recovers original scale"
gap 4.5

# ── 05 ────────────────────────────────────────────────────────────────────────
clear
hdr "05 · Stationarity Tests   (ADF · KPSS)"
printf "  ${D}ADF  H₀: unit root  — reject → stationary${R}\n"
printf "  ${D}KPSS H₀: stationary — reject → non-stationary${R}\n"
echo
kv  "ADF  random walk"    "stat=−3.53   p≈0.043   isStationary=true"
kv  "ADF  AR(1) φ=0.4"   "stat=−4.92   p≈0.010   isStationary=true"
echo
kv  "KPSS random walk  (LEVEL)" "stat=0.665   stationaryAt5%=false"
kv  "KPSS AR(1) φ=0.4 (LEVEL)" "stat=0.181   stationaryAt5%=true"
kv  "KPSS AR(1) φ=0.4 (TREND)" "stat=0.081   stationaryAt5%=true"
echo
kv  "Critical values (LEVEL)"   "1%=0.739   5%=0.463   10%=0.347"
gap 5

# ── 06 ────────────────────────────────────────────────────────────────────────
clear
hdr "06 · Forecasting   (ARIMA · SARIMA · ARIMAX)"
lbl "ARIMA(1,1,1).fit(series).forecast(5)" ""
out "   point: [1.274, 1.218, 1.185, 1.150, 1.115]"
echo
printf "  ${D}95%% prediction intervals via forecastWithIntervals(5, 0.95):${R}\n"
out "   h=1  1.274  [−1.094,  3.641]"
out "   h=2  1.218  [−1.982,  4.418]"
out "   h=3  1.185  [−2.685,  5.056]"
echo
lbl "SARIMA(1,1,1)(1,0,1)₁₂.forecast(6)" ""
out "   [1416, 2532, 3511, 4437, 5357, 6299]"
echo
lbl "ARIMAX(1,0,1).fit(demand, tempMatrix)" ""
out "   exo coef (temperature): 0.7042"
out "   forecast (h=4): [25.45, 28.22, 27.43, 29.26]"
gap 5.5

# ── 07 ────────────────────────────────────────────────────────────────────────
clear
hdr "07 · Exponential Smoothing   (SES · DES · TES / Holt-Winters)"
printf "  ${D}SES — alpha controls how quickly level tracks new data${R}\n"
echo
kv  "alpha=0.1 (slow)"  "[100.84, 100.84, 100.84]"
kv  "alpha=0.3"         "[101.67, 101.67, 101.67]"
kv  "alpha=0.9 (fast)"  "[102.32, 102.32, 102.32]"
echo
lbl "DoubleExpSmoothing(α=0.4, γ=0.2, init=2).forecast(5)" ""
out "   [210.12, 212.21, 214.29, 216.37, 218.45]   ← Holt linear trend"
echo
lbl "TripleExpSmoothing(α=0.3, β=0.1, γ=0.2, period=12).forecast(12)" ""
out "   [547, 561, 575, 582, 584, 578, 565, 565, 572, 578, 590, 590]"
echo
out "   debug=true prints level/trend/seasonal state at each step"
gap 5

# ── 08 ────────────────────────────────────────────────────────────────────────
clear
hdr "08 · State-Space Models   (KalmanFilter · LocalLevelModel)"
lbl "KalmanFilter(processVar=1.0, obsVar=4.0)" ""
kv  "Log-likelihood"    "−166.5986"
kv  "Filtered [0–4]"    "[3.142, 3.266, 2.767, 2.618, 3.272]"
kv  "Forecast (h=6)"    "[40.85, 40.85, 40.85, 40.85, 40.85, 40.85]"
kv  "Forecast variances" "[6.56, 7.56, 8.56, 9.56, 10.56, 11.56]"
echo
lbl "LocalLevelModel().fit(series)" ""
out "   (Brent MLE — finds optimal q/r ratio via log-likelihood)"
kv  "Smoothed tail"     "[39.72, 40.28, 40.49, 41.18, 40.98]"
kv  "Forecast (h=3)"    "[40.977, 40.977, 40.977]"
echo
printf "  ${D}95%% intervals via forecastWithIntervals(6, 0.95):${R}\n"
out "   h=1 [36.948, 45.005]  h=2 [36.509, 45.444]  h=3 [36.109, 45.844]"
gap 5.5

# ── 09 ────────────────────────────────────────────────────────────────────────
clear
hdr "09 · Multivariate Forecasting   (VARModel)"
lbl "VARModel(lagOrder=2).fit([gdp, consumption])" ""
echo
kv  "Eq₀  intercept=−0.028"  "φ₁₁=0.546   φ₁₂=0.261"
kv  "Eq₁  intercept=−0.119"  "φ₂₁=0.436   φ₂₂=0.450"
echo
kv  "GDP forecast (h=5)"          "[−0.612, −0.604, −0.574, −0.549, −0.526]"
kv  "Consumption forecast (h=5)"  "[−0.881, −0.813, −0.772, −0.740, −0.714]"
kv  "VAR(2) AIC"                  "6.2186"
echo
lbl "VARModel.fitOptimal(series, maxLag=4)" "→ AIC-based lag order selection"
gap 4.5

# ── 10 ────────────────────────────────────────────────────────────────────────
clear
hdr "10 · STL Decomposition   (Seasonal-Trend via Loess)"
lbl "STLDecomposition(period=12).decompose(series)" ""
echo
kv  "Trend     [0–5]"  "[113.86, 115.35, 115.71, 115.47, 115.29, 114.73]"
kv  "Seasonal  [0–5]"  "[ −7.73,  −4.00,  17.97,  16.44,  20.66,  12.19]"
kv  "Remainder [0–5]"  "[ −1.76,   1.06,   1.97,   1.80,   1.69,   0.29]"
echo
out "   trend + seasonal + remainder = original  (max error: 2.84e-14 ✓)"
echo
lbl "STLDecomposition(period, trendWin, seasonWin, iters, outerIters=2)" ""
out "   Robust mode: bisquare re-weighting down-weights outliers"
out "   Trend at spike: robust=137.93   plain=146.03  (outlier → remainder)"
gap 5

# ── 11 ────────────────────────────────────────────────────────────────────────
clear
hdr "11 · Automated Model Selection   (AutoARIMA · AutoETS)"
lbl "AutoArima(maxP=3, maxD=2, maxQ=3, AIC).fit(series)" ""
kv  "Best order"      "ARIMA(1,0,0)     AIC = 7.467"
kv  "Forecast (h=5)"  "[1.345, 1.214, 1.118, 1.047, 0.995]"
echo
lbl "AutoArima(···, maxP_s=1, maxD_s=1, maxQ_s=1, period=12, AIC)" ""
kv  "Best seasonal"   "SARIMA(1,1,1)(1,1,0)₁₂   AIC = 57.32"
kv  "Forecast (h=12)" "[241, 245, 251, 253, 255, 256, ...]"
echo
lbl "AutoETS(seasonalPeriod=12).fit(series)" ""
kv  "Best model"   "TRIPLE  (Holt-Winters multiplicative)"
kv  "Forecast"     "[109, 120, 123, 131, 130, 124, ...]"
gap 5.5

# ── 12 ────────────────────────────────────────────────────────────────────────
clear
hdr "12 · Evaluation & Benchmarking   (Metrics · Backtest · Benchmark)"
lbl "RollingOriginBacktest(minTrain=100, horizon=5).run(series, arima)" ""
kv  "RMSE" "1.8306    MAE=1.4656    MASE=1.0738"
echo
printf "  ${D}ModelBenchmark.compare(series, forecasters) — sorted by RMSE:${R}\n"
echo
printf "  ${B}  %-22s  %8s  %8s  %8s${R}\n" "Model" "RMSE" "MAE" "MASE"
printf "  ${G}  %-22s  %8s  %8s  %8s${R}\n" "ARIMA(1,0,1)" "1.8039" "1.4296" "1.0475"
printf "    %-22s  %8s  %8s  %8s\n"          "SES(0.3)" "2.1966" "1.7177" "1.2585"
printf "    %-22s  %8s  %8s  %8s\n"          "DES(0.3,0.1)" "2.4975" "1.9794" "1.4503"
printf "    %-22s  %8s  %8s  %8s\n"          "Naive" "2.5647" "2.0026" "1.4673"
echo
lbl "BenchmarkMarkdown.toMarkdown(summaries)" "→ Markdown table ready for README"
echo
lbl "LjungBoxTest(residuals, lags=10)" ""
out "   stat=12.51   p=0.252   rejectsAt5%=false  ✓ no residual autocorrelation"
gap 5.5

# ── 13 ────────────────────────────────────────────────────────────────────────
clear
hdr "13 · Model Serialization   (ModelSerializer)"
lbl "ModelSerializer.save(fittedModel, path)" ""
lbl "ModelSerializer.load(path)"              ""
echo
kv  "Forecast before save"  "[1.799, 1.651, 1.577, 1.539, 1.520]"
kv  "Forecast after load"   "[1.799, 1.651, 1.577, 1.539, 1.520]"
kv  "Forecasts match"       "true  ✓"
echo
out "   All model classes implement java.io.Serializable."
out "   Fit once → persist → reload at startup.  No re-training needed."
echo
printf "  ${D}Works for: ARIMA, SARIMA, ARIMAX, LocalLevelModel, KalmanFilter, ...${R}\n"
gap 4.5

# ── OUTRO ────────────────────────────────────────────────────────────────────
clear
echo
printf "${B}${G}  tslib — 13 modules · 255 passing tests · zero extra dependencies${R}\n"
echo
printf "  ${D}Collect · MovingAverage · DataQuality · Transform · Tests${R}\n"
printf "  ${D}ARIMA · SARIMA · ARIMAX · VAR · ETS · LocalLevel · STL${R}\n"
printf "  ${D}AutoARIMA · AutoETS · Evaluation · Benchmarking · Serialization${R}\n"
echo
printf "  ${B}github.com/navdeep-G/tslib${R}\n"
echo
gap 3.5
