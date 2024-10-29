import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LimitedConcurrentQueue<T> {
    private final Queue<T> queue = new ConcurrentLinkedQueue<>();
    private final int maxCapacity;

    public LimitedConcurrentQueue(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public boolean offer(T item) {
        if (queue.size() < maxCapacity) {
            return queue.offer(item);
        } else {
            System.out.println("Queue is full. Item rejected.");
            return false;
        }
    }

    public T poll() {
        return queue.poll();
    }

    // Additional queue operations can be added here as needed
}

// Usage example
public class Main {
    public static void main(String[] args) {
        LimitedConcurrentQueue<String> queue = new LimitedConcurrentQueue<>(1000);
        queue.offer("task"); // Adds task if within limit
    }
}
