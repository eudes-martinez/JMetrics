package fr.ubordeaux.jmetrics.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class Metrics {
    private Map<String, Integer> intMap = new HashMap<>();
    private Map<String, Double> doubleMap = new HashMap<>();

    public int getIntMetrics(String metricName) throws NoSuchElementException {
        if(intMap.containsKey(metricName)){
            return intMap.get(metricName);
        }
        throw new NoSuchElementException("No metric named " + metricName + " found");
    }

    public double getDoubleMetrics(String metricName) throws NoSuchElementException{
        if(doubleMap.containsKey(metricName)){
            return doubleMap.get(metricName);
        }
        throw new NoSuchElementException("No metric named " + metricName + " found");
    }

    protected void addMetric(String metricName, int metric){
        if(!intMap.containsKey(metricName) && !doubleMap.containsKey(metricName)){
            intMap.put(metricName, metric);
        }
    }

    protected void addMetric(String metricName, double metric){
        if(!intMap.containsKey(metricName) && !doubleMap.containsKey(metricName)){
            doubleMap.put(metricName, metric);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metrics metrics = (Metrics) o;
        return Objects.equals(intMap, metrics.intMap) &&
                Objects.equals(doubleMap, metrics.doubleMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(intMap, doubleMap);
    }
}
