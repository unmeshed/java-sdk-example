package io.unmeshed.sdk.samples.workers;

import io.unmeshed.client.workers.WorkerMethod;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ArithmeticWorker {

    @WorkerMethod("arithmetic_operation")
    public Map<String, Object> performOperation(final Map<String, Object> input) {
        String operation = (String) input.get("operation");
        int num1 = (int) input.get("num1");
        int num2 = (int) input.get("num2");

        log.info("Performing {} operation on {} and {}", operation, num1, num2);

        switch (operation.toLowerCase()) {
            case "add":
                return Map.of("result", num1 + num2);
            case "subtract":
                return Map.of("result", num1 - num2);
            case "multiply":
                return Map.of("result", num1 * num2);
            case "divide":
                if (num2 == 0) {
                    throw new RuntimeException("Division by zero is not allowed"); //This would mark the worker as FAILED
                }
                return Map.of("result", num1 / (double) num2);
            default:
                throw new RuntimeException("Invalid operation"); //This would mark the worker as FAILED
        }
    }
}
