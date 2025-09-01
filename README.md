# Java SDK Workers Example

This project demonstrates how to use the Unmeshed Java SDK to create and run workers. The provided `Main` class serves as the entry point for the application.

## Prerequisites

Before running the project, ensure that you have the following installed and configured:

1. **Java Development Kit (JDK) (Java 21 or above required, if you are running an older JDK version, get in touch and we can help out)**
2. **Maven or Gradle**: For managing dependencies.
3. **Unmeshed SDK dependency**: (Check build.gradle for example configuration)
4. **Environment Variables**: Set the required environment variables:
    - `UNMESHED_AUTH_ID`: Your Unmeshed authentication ID.
    - `UNMESHED_AUTH_TOKEN`: Your Unmeshed authentication token.
    - `UNMESHED_BASE_URL`: Base URL for the Unmeshed server. Defaults to `http://localhost` if not provided.
    - `UNMESHED_BASE_URL_PORT`: Port - defaults to 443

## Setup Instructions

1. **Clone the Repository**:
   ```bash
   git clone <this-repo>
   cd <this-repo>
   
   Run Main.java

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

## How can you get additional context such as step id or process id in your worker?

```
WorkRequest workRequest = WorkContext.currentWorkRequest();
```
Check the `HelloWorldWorker.java` for example usage


Visit [https://unmeshed.io](https://unmeshed.io) to get started with using the platform

