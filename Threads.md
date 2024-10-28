If you want to call a method after all submitted tasks have completed without shutting down the thread pool, you can achieve this by using `Future` objects returned from the `submitTask` method. This allows you to wait for the completion of each task before calling the `informComplete` method.

Here's how you can implement this:

### Example with Calling `informComplete` After Task Completion

1. **Store Futures Returned from Tasks**: Store the `Future` objects returned by `submitTask`.
2. **Wait for Each Task to Complete**: Use the `get()` method on each `Future` to block until the task is complete.
3. **Call `informComplete` After All Tasks Are Done**.

Here’s the revised implementation:

```java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadPoolService {

    private final ExecutorService executorService;

    // Constructor to initialize the thread pool with a specific size
    public ThreadPoolService(int poolSize) {
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    // Method to submit a task and get a Future object
    public <T> Future<T> submitTask(Callable<T> task) {
        return executorService.submit(task);
    }

    // Method to submit a Runnable task
    public Future<?> submitTask(Runnable task) {
        return executorService.submit(task);
    }

    // Method to shutdown the thread pool
    public void shutdown() {
        executorService.shutdown(); // No new tasks will be accepted
    }

    // Method to wait for all tasks to complete
    public void waitForCompletion(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get(); // Wait for each task to complete
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace(); // Handle exceptions as needed
            }
        }
    }
}

// Custom Runnable class that accepts parameters
class ParameterizedTask implements Runnable {
    private final int id;
    private final String name;

    public ParameterizedTask(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public void run() {
        System.out.println("Executing " + name + " with ID " + id + " in " + Thread.currentThread().getName());
        // Simulate some work
        try {
            Thread.sleep(1000); // Simulate work with sleep
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Preserve interrupt status
        }
    }
}

// Main application class
public class Application {

    public static void main(String[] args) {
        // Initialize the thread pool service with a pool size of 4
        ThreadPoolService threadPoolService = new ThreadPoolService(4);

        // List to hold futures for task completion tracking
        List<Future<?>> futures = new ArrayList<>();

        // Submit ParameterizedTask instances to the thread pool and store futures
        for (int i = 0; i < 10; i++) {
            String taskName = "Task-" + i;
            futures.add(threadPoolService.submitTask(new ParameterizedTask(i, taskName)));
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

1. **Storing Futures**: The main application stores the `Future` objects returned by the `submitTask` method in a list. This allows tracking of each task's completion.

2. **`waitForCompletion` Method**: This method in the `ThreadPoolService` class waits for all tasks to complete by calling `get()` on each `Future`. This will block until each task has finished executing. Any exceptions encountered during execution can be handled as needed.

3. **Calling `informComplete`**: After all tasks have been confirmed as complete, the `informComplete` method is called in the main class, providing the desired functionality of notifying when all tasks have finished.

4. **No Shutdown**: The thread pool remains active, allowing you to submit more tasks if needed after all submitted tasks have completed.

This design pattern maintains clarity and separation of concerns while allowing you to achieve the desired functionality of calling a completion method after all tasks are finished without shutting down the thread pool.
