// copied and modified from https://www.geeksforgeeks.org/thread-pools-java/

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test
{
    static final int NUM_THREADS = 20;

    public static void main(String[] args)
    {
        ArrayList<String> tasks = new ArrayList<>();
        tasks.add("task 1");
        tasks.add("task 2");
        tasks.add("task 3");
        tasks.add("task 4");
        tasks.add("task 5");

        int upto = 0;

        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);

        while (upto < tasks.size())
        {
            pool.execute(new Task(tasks.get(upto++)));
        }

        pool.shutdown();
    }
}
