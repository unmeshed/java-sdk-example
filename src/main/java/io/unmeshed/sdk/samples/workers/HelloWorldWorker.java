package io.unmeshed.sdk.samples.workers;

import io.unmeshed.api.sdk.WorkRequest;
import io.unmeshed.client.WorkContext;
import io.unmeshed.client.workers.WorkerFunction;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class HelloWorldWorker {

    @WorkerFunction(name = "hello_world")
    public Map<String, Object> doSomething(final Map<String, String> input) {
        log.info("Input is : {}", input);
        WorkRequest workRequest = WorkContext.currentWorkRequest();
        return Map.of(
                "message", "Hello World! This is Unmeshed, ready to run your workloads :)",
                "input", input,
                "stepName", workRequest.getStepName(),
                "stepRef", workRequest.getStepRef(),
                "stepId", workRequest.getStepId()
        );
    }
}
