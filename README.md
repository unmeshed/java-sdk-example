# Java SDK Workers Example

This project demonstrates how to use the Unmeshed Java SDK to create and run workers, as well as manage process definitions. The provided `Main` class serves as the entry point for the application.

## Prerequisites

Before running the project, ensure that you have the following installed and configured:

1. **Java Development Kit (JDK) (Java 21 or above required, if you are running an older JDK version, get in touch and we can help out)**
2. **Maven or Gradle**: For managing dependencies.
3. **Unmeshed SDK dependency**: (Check build.gradle for example configuration)
4. **Environment Variables**: Set the required environment variables:
   - `UNMESHED_AUTH_ID`: Your Unmeshed authentication ID.
   - `UNMESHED_AUTH_TOKEN`: Your Unmeshed authentication token.
   - `UNMESHED_ENGINE_HOST`: Base URL for the Unmeshed server. Defaults to `http://localhost` if not provided.
   - `UNMESHED_ENGINE_PORT`: Port - defaults to 8080

## Setup Instructions

1. **Clone the Repository**:
```bash
   git clone <this-repo>
   cd <this-repo>
```

2. **Run Main.java**

## Write your own worker

1. Check example workers in the `io.unmeshed.sdk.samples.workers` package for examples, such as:
   - `ArithmeticWorker`
   - `HelloWorldWorker`

2. Create a new worker class, or add a new method in an existing worker class.

3. Annotate the method with `@WorkerMethod('<worker_name>')` where `<worker_name>` is the name of the worker method you are creating.

For example, in a new worker class, or an existing class, you can write:
```java
import io.unmeshed.client.workers.WorkerMethod;

public class YourClass {

    @WorkerFunction(name = "exampleWorker")
    public WorkerOutput exampleMethod(WorkerInput input) { //Input is optional
       // Your worker method logic here
       // Return worker's output
    }
    
    // WorkerOutput is your class, which is a typesafe output for your worker
    // WorkerInput is your class, which is a typesafe input for your worker
    //      This is populated by an object mapper with values that match your input by the field name
    //      Note that if there is a type mismatch, this will fail to convert
}
```

### Register inline (Lambda) workers
```java
// Register Inline Workers (Lambda)
Function<Map<String, Object>, Object> lambdaWorkerFunction = (Map<String, Object> input) -> {
    Map<String, Object> output = new HashMap<>();

    output.put("status", "success");
    output.put("receivedKeys", input.keySet());
    output.put("message", "Processed successfully");

    return output;
};

client.registerWorkerFunction(lambdaWorkerFunction, "default", "lamba-worker-function", 200, false);
```

## How can you get additional context such as step id or process id in your worker?
```java
WorkRequest workRequest = WorkContext.currentWorkRequest();
```
Check the `HelloWorldWorker.java` for example usage

## Process Management

The SDK provides comprehensive process definition management capabilities. See `ProcessManagementExamples.java` for complete examples.

### Create a Process Definition
```java
ProcessDefinition processDefinition = ProcessDefinition.builder()
    .version(1)
    .type(UnmeshedConstants.ProcessType.API_ORCHESTRATION)
    .name("test_process")
    .namespace("default")
    .description("test unmeshed process created by Java SDK")
    .steps(List.of(getTestStepDefinition()))
    .build();

ProcessDefinition created = client.createNewProcessDefinition(processDefinition);
log.info("Created process definition: {}", created);
```

### Create a Step Definition
```java
private static StepDefinition getTestStepDefinition() {
    return StepDefinition.builder()
        .name("test_noop")
        .ref("test_noop_ref")
        .type(UnmeshedConstants.StepType.NOOP)
        .namespace("default")
        .input(Map.of("key1", "val1"))
        .build();
}
```

### Get Process Definition (Latest or Specific Version)
```java
// Get latest version
ProcessDefinition latest = client.getProcessDefinitionLatestOrVersion(
    "default",      // namespace
    "test_process", // process name
    null            // version (null for latest)
);

// Get specific version
ProcessDefinition v1 = client.getProcessDefinitionLatestOrVersion(
    "default",
    "test_process",
    1               // specific version number
);
```

### Get All Process Definitions
```java
List<ProcessDefinition> allProcesses = client.getAllProcessDefinitions();
log.info("All process definitions: {}", allProcesses);
```

### Update a Process Definition
```java
ProcessDefinition updated = ProcessDefinition.builder()
    .version(2)
    .type(UnmeshedConstants.ProcessType.API_ORCHESTRATION)
    .name("test_process")
    .namespace("default")
    .description("test updated unmeshed process created by Java SDK")
    .steps(List.of(
        getTestStepDefinition(),
        getTestStepDefinitionUpdated()
    ))
    .build();

ProcessDefinition result = client.updateProcessDefinition(updated);
log.info("Updated process definition: {}", result);
```

### Delete Process Definitions
```java
// Delete specific process definitions
Object response = client.deleteProcessDefinitions(
    List.of(processDefinition),
    null  // optional parameters
);
log.info("Deleted process definition: {}", response);
```

### Complete Process Management Example

For a complete working example, see `ProcessManagementExamples.java` which demonstrates:
- Creating a process definition with steps
- Fetching the latest version of a process
- Retrieving all process definitions
- Updating an existing process definition
- Deleting process definitions

---

Visit [https://unmeshed.io](https://unmeshed.io) to get started with using the platform