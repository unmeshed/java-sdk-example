package io.unmeshed.sdk.samples.workers;

import io.unmeshed.api.sdk.WorkRequest;
import io.unmeshed.client.WorkContext;
import io.unmeshed.client.workers.WorkerFunction;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class BalanceTransferWorker {

    @WorkerFunction(name = "transfer_cash")
    public Map<String, Object> doSomething(final Map<String, String> input) {
        log.info("Input is : {}", input);
        WorkRequest workRequest = WorkContext.currentWorkRequest();
        return Map.of(
                "message", "Transfer is successful",
                "input", input,
                "stepName", workRequest.getStepName(),
                "stepRef", workRequest.getStepRef(),
                "stepId", workRequest.getStepId()
        );
    }
}
