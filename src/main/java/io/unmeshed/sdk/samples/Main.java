package io.unmeshed.sdk.samples;

import io.unmeshed.api.common.ProcessData;
import io.unmeshed.api.common.ProcessRequestData;
import io.unmeshed.client.ClientConfig;
import io.unmeshed.client.UnmeshedClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Entry point for the Unmeshed SDK example application.
 * <p>
 * This application demonstrates how to configure and start the Unmeshed SDK client,
 * and begin processing work requests.
 */
@Slf4j
public class Main {

    static class RunInfo {
        final long startTime;
        final long duration;

        RunInfo(long startTime, long duration) {
            this.startTime = startTime;
            this.duration = duration;
        }

        long getEndTime() {
            return startTime + duration;
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        // Retrieve configuration values from environment variables
        final String authId = System.getenv("UNMESHED_AUTH_ID");
        final String authToken = System.getenv("UNMESHED_AUTH_TOKEN");
        final String baseUrl = System.getenv("UNMESHED_BASE_URL");

        String port = System.getenv("UNMESHED_BASE_URL_PORT");
        if (port == null || port.isEmpty()) {
            // Default is the HTTPS port
            port = "443";
        }

        // Validate required environment variables
        if (authId == null || authToken == null) {
            log.error("Environment variables UNMESHED_AUTH_ID and UNMESHED_AUTH_TOKEN must be set.");
            throw new IllegalArgumentException("Missing authentication credentials.");
        }

        log.info("Starting the worker connecting to {}:{}", baseUrl, port);
        // Build the client configuration, you can customize all of the properties in this configuration
        ClientConfig clientConfig = ClientConfig.builder(authId, authToken)
                .baseUrl(baseUrl)
                .workRequestBatchSize(10)
                .initialDelayMillis(20)
                .stepTimeoutMillis(Long.MAX_VALUE) // No timeout
                .port(Integer.parseInt(port))
                .build();

        // Initialize and start the Unmeshed client
        UnmeshedClient client = new UnmeshedClient(clientConfig);
        // Start the client
        client.start();

        int totalRuns = 50000;
        CountDownLatch countDownLatch = new CountDownLatch(totalRuns);

        // Use thread-safe collections
        List<RunInfo> runInfos = Collections.synchronizedList(new ArrayList<>(totalRuns));

        ExecutorService executorService = Executors.newFixedThreadPool(200);
        // Adjust the thread pool size as needed

        for (int i = 0; i < totalRuns; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    // Mark transaction as started

                    // --- Your original logic here: run the process ---
                    ProcessRequestData request = ProcessRequestData.builder()
                            .name("testing")
                            .namespace("default")
                            .input(Map.of("abc", "pqr"))
                            .correlationId("abcd")
                            .requestId("req-001-%s".formatted(finalI))
                            .build();

                    // Synchronous call
                    ProcessData processResultDataSync = client.runProcessSync(request);

                    long duration = System.currentTimeMillis() - startTime;

                    // Capture data
                    runInfos.add(new RunInfo(startTime, duration));

                    log.info("Completed run number {} in {} ms", finalI, duration);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        // Shut down and wait for all tasks to finish
        countDownLatch.await();
        executorService.shutdown();
        if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
            log.warn("Not all tasks finished within the timeout!");
        }

        // Now compute metrics after all runs have completed
        printSummary(runInfos);

    }

    private static void printSummary(List<RunInfo> runInfos) {
        if (runInfos.isEmpty()) {
            log.info("No runs to summarize.");
            return;
        }

        // Prepare sorted durations for min, max, percentiles
        List<Long> durationsSorted;
        List<Long> startTimes = new ArrayList<>(runInfos.size());
        List<Long> endTimes = new ArrayList<>(runInfos.size());

        synchronized (runInfos) {
            durationsSorted = runInfos.stream()
                    .map(r -> r.duration)
                    .sorted()
                    .collect(Collectors.toList());

            for (RunInfo r : runInfos) {
                startTimes.add(r.startTime);
                endTimes.add(r.getEndTime());
            }
        }

        // Basic stats
        long minDuration = durationsSorted.getFirst();
        long maxDuration = durationsSorted.getLast();
        double avgDuration = durationsSorted.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        long p50 = getPercentile(durationsSorted, 50);
        long p75 = getPercentile(durationsSorted, 75);
        long p95 = getPercentile(durationsSorted, 95);

        // Throughput (transactions/second) across the entire run
        long earliestStart = startTimes.stream().min(Long::compare).orElse(0L);
        long latestEnd = endTimes.stream().max(Long::compare).orElse(0L);
        double totalSeconds = (latestEnd - earliestStart) / 1000.0;
        double tps = 0.0;
        if (totalSeconds > 0) {
            tps = runInfos.size() / totalSeconds;
        }

        // Calculate how many started/completed per minute
        Map<Long, Long> startedPerMin = groupByMinute(startTimes);
        Map<Long, Long> completedPerMin = groupByMinute(endTimes);

        // Print summary
        log.info("==== SUMMARY OF {} RUNS ====", runInfos.size());
        log.info("Average duration:     {} ms", avgDuration);
        log.info("Min duration:         {} ms", minDuration);
        log.info("Max duration:         {} ms", maxDuration);
        log.info("P50 duration:         {} ms", p50);
        log.info("P75 duration:         {} ms", p75);
        log.info("P95 duration:         {} ms", p95);
        log.info("Overall average TPS:  {} transactions/sec", tps);

        // Print optional per-minute stats
        log.info("Transactions STARTED per minute:");
        startedPerMin.forEach((minute, count) ->
                log.info("  Minute {} -> {} started", minute, count));

        log.info("Transactions COMPLETED per minute:");
        completedPerMin.forEach((minute, count) ->
                log.info("  Minute {} -> {} completed", minute, count));
    }

    private static long getPercentile(List<Long> sortedValues, int percentile) {
        if (sortedValues.isEmpty()) {
            return 0;
        }
        int index = (int) Math.ceil(percentile / 100.0 * sortedValues.size()) - 1;
        index = Math.min(Math.max(index, 0), sortedValues.size() - 1);
        return sortedValues.get(index);
    }

    private static Map<Long, Long> groupByMinute(List<Long> times) {
        return times.stream()
                .collect(Collectors.groupingBy(
                        t -> t / 60000,  // minute grouping
                        Collectors.counting()
                ));
    }

}
