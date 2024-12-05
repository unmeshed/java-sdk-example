package io.unmeshed.sdk.samples;

import io.unmeshed.client.ClientConfig;
import io.unmeshed.client.UnmeshedClient;
import lombok.extern.slf4j.Slf4j;

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
        final String baseUrl = System.getenv("UNMESHED_BASE_URL") != null
                ? System.getenv("UNMESHED_BASE_URL")
                : "http://localhost";

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
                .port(8080) //Optional for https uri's
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
    }
}
