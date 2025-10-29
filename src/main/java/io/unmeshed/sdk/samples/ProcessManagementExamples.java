package io.unmeshed.sdk.samples;

import io.unmeshed.api.common.ProcessDefinition;
import io.unmeshed.api.common.StepDefinition;
import io.unmeshed.api.common.UnmeshedConstants;
import io.unmeshed.client.ClientConfig;
import io.unmeshed.client.UnmeshedClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class ProcessManagementExamples {

    private static boolean isBlank(String param) {
        return param == null || param.isBlank();
    }

    @SneakyThrows
    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");

        final String authId = Optional.ofNullable(System.getenv("UNMESHED_AUTH_ID")).orElse("<< Unmeshed Client Id >>");
        final String authToken = Optional.ofNullable(System.getenv("UNMESHED_AUTH_TOKEN")).orElse("<< Unmeshed Auth Token >>");
        final String baseUrl = Optional.ofNullable(System.getenv("UNMESHED_ENGINE_HOST")).orElse("http://localhost");
        final String port = Optional.ofNullable(System.getenv("UNMESHED_ENGINE_PORT")).orElse("8080");

        if (isBlank(authId) || isBlank(authToken) || isBlank(baseUrl)) {
            log.warn("""
                    Required parameters have not been provided. Please ensure you have the following environment variables set:
                      * UNMESHED_AUTH_ID
                      * UNMESHED_AUTH_TOKEN
                      * UNMESHED_ENGINE_HOST
                      * UNMESHED_ENGINE_PORT""");
            System.exit(-1);
        }

        ClientConfig clientConfig = ClientConfig.builder(authId, authToken)
                .baseUrl(baseUrl)
                .workRequestBatchSize(2000)
                .initialDelayMillis(50)
                .responseSubmitBatchSize(2000)
                .stepTimeoutMillis(30000)
                .port(Integer.parseInt(port))
                .build();


        UnmeshedClient client = new UnmeshedClient(clientConfig);
        ProcessDefinition processDefinition = client.createNewProcessDefinition(createTestProcessDefinition());
        log.info("Created process definition : {}", processDefinition);

        processDefinition = client.getProcessDefinitionLatestOrVersion("default", "test_process", null);
        log.info("Fetched process definition latest or version : {}", processDefinition);

        log.info("Fetched all process definition : {}", client.getAllProcessDefinitions());

        processDefinition = client.updateProcessDefinition(createUpdatedTestProcessDefinition());
        log.info("Updated process definition : {}", processDefinition);

        Object response = client.deleteProcessDefinitions(List.of(processDefinition), null);
        log.info("Deleted process definition : {}", response);
    }

    private static ProcessDefinition createTestProcessDefinition() {
        return ProcessDefinition.builder()
                .version(1)
                .type(UnmeshedConstants.ProcessType.API_ORCHESTRATION)
                .name("test_process")
                .namespace("default")
                .description("test unmeshed process created by Java SDK")
                .steps(List.of(getTestStepDefinition()))
                .build();
    }

    private static ProcessDefinition createUpdatedTestProcessDefinition() {
        return ProcessDefinition.builder()
                .version(2)
                .type(UnmeshedConstants.ProcessType.API_ORCHESTRATION)
                .name("test_process")
                .namespace("default")
                .description("test updated unmeshed process created by Java SDK")
                .steps(List.of(getTestStepDefinition(), getTestStepDefinitionUpdated()))
                .build();
    }

    private static StepDefinition getTestStepDefinitionUpdated() {
        return StepDefinition.builder()
                .name("test_noop_2")
                .ref("test_noop_ref_2")
                .type(UnmeshedConstants.StepType.NOOP)
                .namespace("default")
                .input(Map.of("key2", "val2"))
                .build();
    }

    private static StepDefinition getTestStepDefinition() {
        return StepDefinition.builder()
                .name("test_noop")
                .ref("test_noop_ref")
                .type(UnmeshedConstants.StepType.NOOP)
                .namespace("default")
                .input(Map.of("key1", "val1"))
                .build();
    }
}
