#!/bin/bash
# tslib REST API — curl examples
# Start the server: cd tslib-api && ../gradlew bootRun
# Swagger UI: http://localhost:8080/swagger-ui

BASE="http://localhost:8080/api"

DATA='[112,118,132,129,121,135,148,148,136,119,104,118,115,126,141,135,125,149,170,170,158,133,114,140]'

echo "=== ARIMA Forecast ==="
curl -s -X POST "$BASE/arima/forecast" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"p\": 1, \"d\": 1, \"q\": 1, \"steps\": 6}" | python3 -m json.tool

echo ""
echo "=== ARIMA Forecast with Intervals ==="
curl -s -X POST "$BASE/arima/forecast-intervals" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"p\": 1, \"d\": 1, \"q\": 1, \"steps\": 6, \"confidenceLevel\": 0.95}" | python3 -m json.tool

echo ""
echo "=== SARIMA Forecast ==="
curl -s -X POST "$BASE/sarima/forecast" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"p\": 1, \"d\": 1, \"q\": 1, \"seasonalP\": 1, \"seasonalD\": 1, \"seasonalQ\": 1, \"seasonalPeriod\": 12, \"steps\": 12}" | python3 -m json.tool

echo ""
echo "=== AutoARIMA ==="
curl -s -X POST "$BASE/auto/arima/forecast" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"maxP\": 3, \"maxD\": 2, \"maxQ\": 3, \"steps\": 6, \"criterion\": \"AIC\"}" | python3 -m json.tool

echo ""
echo "=== AutoETS ==="
curl -s -X POST "$BASE/auto/ets/forecast" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"steps\": 6, \"seasonalPeriod\": 12}" | python3 -m json.tool

echo ""
echo "=== Single ETS Forecast ==="
curl -s -X POST "$BASE/ets/single/forecast" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"alpha\": 0.3, \"steps\": 6}" | python3 -m json.tool

echo ""
echo "=== Triple ETS (Holt-Winters) ==="
curl -s -X POST "$BASE/ets/triple/forecast" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"alpha\": 0.3, \"beta\": 0.1, \"gamma\": 0.1, \"period\": 12, \"steps\": 12}" | python3 -m json.tool

echo ""
echo "=== Local Level (State Space) Forecast ==="
curl -s -X POST "$BASE/statespace/local-level/forecast" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"steps\": 6}" | python3 -m json.tool

echo ""
echo "=== Kalman Filter ==="
curl -s -X POST "$BASE/statespace/kalman/filter" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"processVariance\": 1.0, \"observationVariance\": 1.0, \"steps\": 6}" | python3 -m json.tool

echo ""
echo "=== ARIMA Order Search ==="
curl -s -X POST "$BASE/arima/order-search" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"maxP\": 3, \"maxD\": 2, \"maxQ\": 3, \"criterion\": \"AIC\"}" | python3 -m json.tool

echo ""
echo "=== VAR Forecast (two series) ==="
curl -s -X POST "$BASE/var/forecast" \
  -H "Content-Type: application/json" \
  -d "{\"series\": [$DATA, $DATA], \"steps\": 4}" | python3 -m json.tool

echo ""
echo "=== Forecast Metrics ==="
curl -s -X POST "$BASE/evaluate/metrics" \
  -H "Content-Type: application/json" \
  -d '{"actual":[100,110,120,115,125],"forecast":[102,108,122,113,127],"trainingSeries":[90,95,100,105],"seasonalPeriod":1}' | python3 -m json.tool

echo ""
echo "=== Backtest ==="
curl -s -X POST "$BASE/evaluate/backtest" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"modelSpec\": {\"type\": \"ARIMA\", \"p\": 1, \"d\": 1, \"q\": 1}, \"minTrainSize\": 12, \"horizon\": 3}" | python3 -m json.tool

echo ""
echo "=== Train/Test Split ==="
curl -s -X POST "$BASE/evaluate/train-test-split" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"trainRatio\": 0.8}" | python3 -m json.tool

echo ""
echo "=== ADF Stationarity Test ==="
curl -s -X POST "$BASE/tests/adf" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA}" | python3 -m json.tool

echo ""
echo "=== KPSS Test ==="
curl -s -X POST "$BASE/tests/kpss" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"regressionType\": \"LEVEL\"}" | python3 -m json.tool

echo ""
echo "=== Ljung-Box Diagnostics ==="
RESID='[0.1,-0.2,0.3,-0.1,0.05,0.2,-0.15,0.1,-0.05,0.0]'
curl -s -X POST "$BASE/diagnostics/ljung-box" \
  -H "Content-Type: application/json" \
  -d "{\"residuals\": $RESID, \"lags\": 5}" | python3 -m json.tool

echo ""
echo "=== STL Decomposition ==="
curl -s -X POST "$BASE/decompose/stl" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"period\": 12}" | python3 -m json.tool

echo ""
echo "=== Box-Cox Transform (auto lambda) ==="
curl -s -X POST "$BASE/transform/boxcox" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA}" | python3 -m json.tool

echo ""
echo "=== Differencing ==="
curl -s -X POST "$BASE/transform/difference" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"order\": 1}" | python3 -m json.tool

echo ""
echo "=== Impute Missing Values ==="
curl -s -X POST "$BASE/dataquality/impute" \
  -H "Content-Type: application/json" \
  -d '{"data":[1.0,null,3.0,null,5.0],"strategy":"LINEAR_INTERPOLATION"}' | python3 -m json.tool

echo ""
echo "=== Outlier Detection ==="
curl -s -X POST "$BASE/dataquality/outliers" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"method\": \"Z_SCORE\", \"threshold\": 2.5}" | python3 -m json.tool

echo ""
echo "=== Winsorize ==="
curl -s -X POST "$BASE/dataquality/winsorize" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"lowerProbability\": 0.05, \"upperProbability\": 0.95}" | python3 -m json.tool

echo ""
echo "=== Cube Root Transform ==="
curl -s -X POST "$BASE/transform/cbrt" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA}" | python3 -m json.tool

echo ""
echo "=== Arbitrary Root Transform (4th root) ==="
curl -s -X POST "$BASE/transform/root" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"r\": 4.0}" | python3 -m json.tool

echo ""
echo "=== Model Benchmark (compare ARIMA vs AutoETS) ==="
curl -s -X POST "$BASE/evaluate/benchmark" \
  -H "Content-Type: application/json" \
  -d "{
    \"data\": $DATA,
    \"models\": [
      {\"name\": \"ARIMA(1,1,1)\", \"spec\": {\"type\": \"ARIMA\", \"p\": 1, \"d\": 1, \"q\": 1}},
      {\"name\": \"AutoARIMA\",    \"spec\": {\"type\": \"AUTO_ARIMA\", \"maxP\": 3, \"maxD\": 2, \"maxQ\": 3}},
      {\"name\": \"AutoETS\",      \"spec\": {\"type\": \"AUTO_ETS\"}}
    ],
    \"minTrainSize\": 12, \"horizon\": 3
  }" | python3 -m json.tool

echo ""
echo "=== Moving Average ==="
curl -s -X POST "$BASE/moving-average" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"period\": 3, \"type\": \"SIMPLE\"}" | python3 -m json.tool

echo ""
echo "=== Analyze ==="
curl -s -X POST "$BASE/analyze" \
  -H "Content-Type: application/json" \
  -d "{\"data\": $DATA, \"k\": 1, \"n\": 8, \"windowSize\": 3}" | python3 -m json.tool
