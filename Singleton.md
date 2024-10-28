To make the `ThreadPoolService` a singleton in Java, you can use the **lazy initialization** approach with a **private constructor** and a **static method** to access the instance. Here’s how you can implement a singleton pattern for the `ThreadPoolService`:

### Singleton Implementation for `ThreadPoolService`

1. **Private Constructor**: This prevents instantiation from outside the class.
2. **Static Instance Variable**: Hold the single instance of the class.
3. **Static Method for Access**: Provide a method to access the instance, ensuring it's created only once.

Here's the updated code:

```java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadPoolService {

    private static ThreadPoolService instance; // Static variable to hold the singleton instance
    private final ExecutorService executorService;

    // Private constructor to prevent instantiation from outside
    private ThreadPoolService(int poolSize) {
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    // Method to get the singleton instance
    public static synchronized ThreadPoolService getInstance(int poolSize) {
        if (instance == null) {
            instance = new ThreadPoolService(poolSize);
        }
        return instance;
    }

    // Method to submit a task and get a CompletableFuture object
    public CompletableFuture<Void> submitTask(Runnable task) {
        return CompletableFuture.runAsync(task, executorService);
    }

    // Method to shutdown the thread pool
    public void shutdown() {
        executorService.shutdown(); // No new tasks will be accepted
    }

    // Method to wait for all tasks to complete
    public void waitForCompletion(List<CompletableFuture<Void>> futures) {
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allOf.get(); // Wait for all tasks to complete
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(); // Handle exceptions as needed
        }
    }
}

// Main application class
public class Application {

    public static void main(String[] args) {
        // Initialize the thread pool service as a singleton with a pool size of 4
        ThreadPoolService threadPoolService = ThreadPoolService.getInstance(4);

        // List to hold futures for task completion tracking
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Submit ParameterizedTask instances to the thread pool and store futures
        for (int i = 0; i < 10; i++) {
            final int id = i;
            String taskName = "Task-" + i;
            futures.add(threadPoolService.submitTask(() -> {
                System.out.println("Executing " + taskName + " with ID " + id + " in " + Thread.currentThread().getName());
                // Simulate some work
                try {
                    Thread.sleep(1000); // Simulate work with sleep
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Preserve interrupt status
                }
            }));
        }

        // Wait for all tasks to complete
        threadPoolService.waitForCompletion(futures);

        // Call informComplete after all tasks are finished
        informComplete();

        System.out.println("All tasks have been submitted and completed without shutting down the pool.");
    }

    // Inform method to indicate all tasks are complete
    public static void informComplete() {
        System.out.println("All tasks have been completed.");
    }
}
```

### Key Changes and Explanation

1. **Private Static Instance**: The `instance` variable holds the single instance of the `ThreadPoolService`. It is marked as static to belong to the class rather than to any specific instance.

2. **Private Constructor**: The constructor is private, preventing external classes from creating new instances directly.

3. **Synchronized Method for Instance Access**: The `getInstance` method checks if the instance is `null`. If so, it creates a new instance. The `synchronized` keyword ensures that this method is thread-safe, preventing multiple threads from creating multiple instances simultaneously.

### Thread Safety Note
- The `synchronized` keyword ensures that the `getInstance` method is thread-safe. However, if high performance is required and the singleton is accessed frequently, you might consider using double-checked locking or other patterns, but for most use cases, this implementation will suffice.

### Usage
Now, when you want to access the `ThreadPoolService`, you simply call `ThreadPoolService.getInstance(poolSize)`, ensuring that you get the same instance every time, which is particularly useful in multi-threaded environments.
