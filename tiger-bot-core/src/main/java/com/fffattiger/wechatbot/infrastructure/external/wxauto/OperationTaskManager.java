package com.fffattiger.wechatbot.infrastructure.external.wxauto;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;

@Slf4j
@Component
public class OperationTaskManager {
    
    private final ThreadPoolExecutor operationQueue;
    private final BlockingQueue<Runnable> taskQueue;
    
    public OperationTaskManager() {
        this.taskQueue = new LinkedBlockingQueue<>(1000);
        this.operationQueue = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS, taskQueue,
            r -> new Thread(r, "wx-operation-thread")
        );
    }
    
    public <T> CompletableFuture<T> submitTask(String description, Callable<T> task) {
        UiOperationTask<T> operationTask = new UiOperationTask<>(description, task);
        CompletableFuture<T> future = new CompletableFuture<>();
        
        operationQueue.submit(() -> {
            try {
                
                logPendingTasks();
                T result = operationTask.call();
                future.complete(result);
            } catch (Exception e) {
                
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }
    
    private void logPendingTasks() {
        List<String> pending = getPendingUiOperationTasks();
        if (!pending.isEmpty()) {
            log.info("等待中的任务: {}", pending);
        }
    }
    
    public List<String> getPendingUiOperationTasks() {
        List<String> list = new ArrayList<>();
        for (Runnable r : taskQueue) {
            if (r instanceof UiOperationTask) {
                list.add(r.toString());
            } else {
                list.add(r.getClass().getSimpleName());
            }
        }
        return list;
    }
    
    public void shutdown() {
        operationQueue.shutdown();
        try {
            if (!operationQueue.awaitTermination(5, TimeUnit.SECONDS)) {
                operationQueue.shutdownNow();
            }
        } catch (InterruptedException e) {
            operationQueue.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    public static class UiOperationTask<T> implements Callable<T> {
        private final String description;
        private final Callable<T> task;

        public UiOperationTask(String description, Callable<T> task) {
            this.description = description;
            this.task = task;
        }

        @Override
        public T call() throws Exception {
            return task.call();
        }

        @Override
        public String toString() {
            return "UiOperationTask{description='" + description + "'}";
        }
    }
}
