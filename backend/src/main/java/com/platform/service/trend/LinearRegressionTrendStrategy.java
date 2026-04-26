package com.platform.service.trend;

import com.platform.domain.entity.IndicatorValue;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Component
public class LinearRegressionTrendStrategy implements TrendComputationStrategy {

    @Override
    public TrendComputationResult compute(List<IndicatorValue> values, int forecastYear) {
        double[] years = values.stream().mapToDouble(v -> v.getYear()).toArray();
        double[] vals = values.stream().mapToDouble(v -> v.getValue().doubleValue()).toArray();
        int baseYear = values.get(0).getYear();
        double[] normalizedYears = values.stream()
                .mapToDouble(v -> v.getYear() - baseYear)
                .toArray();

        LinearRegressionResult lr = linearRegression(normalizedYears, vals);
        double forecast = lr.slope() * (forecastYear - baseYear) + lr.intercept();

        return new TrendComputationResult(
                BigDecimal.valueOf(lr.slope()).setScale(8, RoundingMode.HALF_UP),
                BigDecimal.valueOf(lr.intercept()).setScale(8, RoundingMode.HALF_UP),
                BigDecimal.valueOf(lr.rSquared()).setScale(6, RoundingMode.HALF_UP),
                BigDecimal.valueOf(forecast).setScale(4, RoundingMode.HALF_UP),
                Map.of(
                        "baseYear", baseYear,
                        "dataPoints", values.size(),
                        "yearRange", List.of((int) years[0], (int) years[years.length - 1])
                )
        );
    }

    @Override
    public String modelType() {
        return "LINEAR";
    }

    private LinearRegressionResult linearRegression(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
        }
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        double meanY = sumY / n;
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < n; i++) {
            ssTot += Math.pow(y[i] - meanY, 2);
            ssRes += Math.pow(y[i] - (slope * x[i] + intercept), 2);
        }
        double rSquared = ssTot == 0 ? 1.0 : 1.0 - ssRes / ssTot;
        return new LinearRegressionResult(slope, intercept, rSquared);
    }

    private record LinearRegressionResult(double slope, double intercept, double rSquared) {}
}

