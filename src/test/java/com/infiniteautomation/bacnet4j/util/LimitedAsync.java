package com.infiniteautomation.bacnet4j.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Accepts a queue of jobs to run and a maximum concurrency number. Runs the jobs is as many threads as is allowed
 * until all jobs are completed.
 * 
 * @author Matthew
 */
public class LimitedAsync<T extends Runnable> {
    private final ExecutorService executorService;
    final List<T> tasks;
    private final int maxConcurrency;

    public LimitedAsync(ExecutorService executorService, List<T> tasks, int maxConcurrency) {
        this.executorService = executorService;
        this.tasks = tasks;
        this.maxConcurrency = maxConcurrency;
    }

    public void executeAndWait() {
        List<Worker> workers = new ArrayList<Worker>(maxConcurrency);

        for (int i = 0; i < maxConcurrency; i++) {
            Worker worker = new Worker();
            workers.add(worker);
            executorService.execute(worker);
        }

        for (Worker worker : workers)
            worker.join();
    }

    class Worker implements Runnable {
        private boolean done = false;

        public void run() {
            while (true) {
                T task = null;
                synchronized (tasks) {
                    if (!tasks.isEmpty())
                        task = tasks.remove(tasks.size() - 1);
                }

                if (task == null)
                    break;

                try {
                    task.run();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            synchronized (this) {
                done = true;
                notify();
            }
        }

        void join() {
            synchronized (this) {
                if (!done) {
                    try {
                        wait();
                    }
                    catch (InterruptedException e) {
                        // no op
                    }
                }
            }
        }
    }
}
