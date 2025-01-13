package io.unmeshed.sdk.samples;

import com.fasterxml.jackson.databind.JsonNode;
import io.unmeshed.api.common.ApiCallType;
import io.unmeshed.api.common.ProcessData;
import io.unmeshed.api.common.ProcessRequestData;
import io.unmeshed.api.common.ProcessSearchRequest;
import io.unmeshed.client.ClientConfig;
import io.unmeshed.client.UnmeshedClient;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Entry point for the Unmeshed SDK example application.
 *
 * This application demonstrates how to configure and start the Unmeshed SDK client,
 * and begin processing work requests.
 */
@Slf4j
public class Main {

    /**
     * Main method to initialize and start the Unmeshed SDK client.
     * @param args Command-line arguments (not used in this sample application).
     */
    public static void main(String[] args) {
        // Retrieve configuration values from environment variables
        final String authId = System.getenv("UNMESHED_AUTH_ID");
        final String authToken = System.getenv("UNMESHED_AUTH_TOKEN");
        final String baseUrl = System.getenv("UNMESHED_BASE_URL");

        String port = System.getenv("UNMESHED_BASE_URL_PORT");
        if(port == null || port.isEmpty()) {
            // Default is the HTTPS port
            port = "443";
        }

        // Validate required environment variables
        if (authId == null || authToken == null) {
            log.error("Environment variables UNMESHED_AUTH_ID and UNMESHED_AUTH_TOKEN must be set.");
            throw new IllegalArgumentException("Missing authentication credentials.");
        }

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

        // Register workers by specifying the package
        String packageName = "io.unmeshed.sdk.samples";
        log.info("Scanning package: {} for workers", packageName);
        client.registerWorkers(packageName);

        // Start the client
        client.start();

        log.info("Started Unmeshed SDK successfully.");

        log.info("Running some other samples APIs:");

        ProcessRequestData request = ProcessRequestData.builder()
                .name("testing")
                .namespace("default")
                .input(Map.of("abc", "pqr"))
                .correlationId("abcd")
                .requestId("req-001")
                .build();

        ProcessData processResultDataAsync = client.runProcessAsync(request);
        log.info("Process : {}", processResultDataAsync);
        log.info("Aync output: (should be empty if the process is not completed) {}", processResultDataAsync.getOutput());

        ProcessData processResultDataSync = client.runProcessSync(request);
        log.info("Process : {}", processResultDataSync);
        log.info("Sync output: {}", processResultDataSync.getOutput());

        JsonNode resultsGet = client.invokeApiMappingGet("testing", "api-call-001", "api-call-crid-001", ApiCallType.SYNC);
        log.info("API Call output: {}", resultsGet);

        JsonNode resultsPost = client.invokeApiMappingPost("testing", "api-call-001", "api-call-crid-001", Map.of("myInput", "input1"), ApiCallType.SYNC);
        log.info("API Call output: {}", resultsPost);

        JsonNode resultsGetAsync = client.invokeApiMappingGet("testing", "api-call-001", "api-call-crid-001", ApiCallType.ASYNC);
        log.info("API Call output: {}", resultsGetAsync);

        JsonNode resultsPostAsync = client.invokeApiMappingPost("testing", "api-call-001", "api-call-crid-001", Map.of("myInput", "input1"), ApiCallType.ASYNC);
        log.info("API Call output: {}", resultsPostAsync);

        printProcessData(client.getProcessData(resultsPost.path("processId").asLong(), true));
        printProcessData(client.getProcessData(resultsGet.path("processId").asLong(), true));
        printProcessData(client.getProcessData(resultsGetAsync.path("processId").asLong(), true));
        printProcessData(client.getProcessData(resultsPostAsync.path("processId").asLong(), true));
        printProcessData(client.getProcessData(processResultDataSync.getProcessId(), true));
        printProcessData(client.getProcessData(processResultDataAsync.getProcessId(), true));

        List<ProcessData> processData = client.searchProcessExecutions(ProcessSearchRequest.builder().build());
        log.info("Search process executions: {}", processData);

    }


    private static void printProcessData(ProcessData processData) {
        log.info("Process data from sync call: {}", processData);
        log.info("Process data output from sync call: {}", processData.getOutput());
    }

}
