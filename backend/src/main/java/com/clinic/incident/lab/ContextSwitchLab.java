package com.clinic.incident.lab;

import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 同一批小活，用两种方式干完，比谁更快。
 * 乱开线程：每个任务 new 一个 Thread，线程又多又短命，CPU 大量时间花在换人干活上。
 * 线程池：固定几个人反复接活，少创建、少切换。
 */
@Component
public class ContextSwitchLab {

    private static final int TASKS = 2000;
    private static final int WORK_LOOPS = 40_000;

    public Comparison run() {
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors());
        RunResult wasteful = runWasteful(TASKS);
        sleepQuiet(150);
        RunResult pooled = runPooled(TASKS, cores);
        String summary = buildSummary(wasteful, pooled, cores);
        return new Comparison(summary, TASKS, cores, wasteful, pooled);
    }

    private RunResult runWasteful(int tasks) {
        AtomicInteger peak = new AtomicInteger();
        AtomicBoolean sampling = new AtomicBoolean(true);
        Thread sampler = startSampler(peak, sampling);
        CountDownLatch done = new CountDownLatch(tasks);
        List<Thread> threads = new ArrayList<>(tasks);
        long start = System.nanoTime();
        for (int i = 0; i < tasks; i++) {
            Thread t = new Thread(() -> {
                burnCpu();
                done.countDown();
            }, "wasteful-" + i);
            threads.add(t);
            t.start();
        }
        await(done);
        for (Thread t : threads) {
            joinQuiet(t);
        }
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        sampling.set(false);
        joinQuiet(sampler);
        return new RunResult(
                "乱开线程",
                tasks,
                tasks,
                elapsedMs,
                peak.get(),
                "每个任务都 new 一个 Thread，干完就扔"
        );
    }

    private RunResult runPooled(int tasks, int poolSize) {
        AtomicInteger peak = new AtomicInteger();
        AtomicBoolean sampling = new AtomicBoolean(true);
        Thread sampler = startSampler(peak, sampling);
        ExecutorService pool = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r);
            t.setName("pool-worker");
            t.setDaemon(true);
            return t;
        });
        CountDownLatch done = new CountDownLatch(tasks);
        long start = System.nanoTime();
        for (int i = 0; i < tasks; i++) {
            pool.execute(() -> {
                burnCpu();
                done.countDown();
            });
        }
        await(done);
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        pool.shutdown();
        try {
            pool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        sampling.set(false);
        joinQuiet(sampler);
        return new RunResult(
                "线程池",
                tasks,
                poolSize,
                elapsedMs,
                peak.get(),
                "固定 " + poolSize + " 个工人反复接活，少创建也少切换"
        );
    }

    private Thread startSampler(AtomicInteger peak, AtomicBoolean sampling) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        Thread sampler = new Thread(() -> {
            while (sampling.get()) {
                peak.updateAndGet(prev -> Math.max(prev, bean.getThreadCount()));
                sleepQuiet(2);
            }
        }, "context-switch-sampler");
        sampler.setDaemon(true);
        sampler.start();
        return sampler;
    }

    /** 故意烧一点 CPU，让线程真的抢处理器，上下文切换才会露出来。 */
    private void burnCpu() {
        long x = 0;
        for (int i = 0; i < WORK_LOOPS; i++) {
            x += (i * 31L) ^ (x >>> 3);
        }
        if (x == Long.MIN_VALUE) {
            throw new IllegalStateException("不可能走到这里");
        }
    }

    private String buildSummary(RunResult wasteful, RunResult pooled, int cores) {
        if (wasteful.elapsedMs() <= 0) {
            return "两边都跑完了。再点一次看时间。";
        }
        double slower = (double) wasteful.elapsedMs() / Math.max(1, pooled.elapsedMs());
        return String.format(
                "同样 %d 件活：乱开线程用了 %d 毫秒（峰值约 %d 个线程），线程池（%d 个工人）用了 %d 毫秒（峰值约 %d 个线程）。乱开大约慢 %.1f 倍。线程不是越多越好，开太多时 CPU 忙着换人，真正干活的时间反而少。",
                TASKS,
                wasteful.elapsedMs(),
                wasteful.peakThreads(),
                cores,
                pooled.elapsedMs(),
                pooled.peakThreads(),
                slower
        );
    }

    private static void await(CountDownLatch done) {
        try {
            if (!done.await(60, TimeUnit.SECONDS)) {
                throw new IllegalStateException("这批活 60 秒还没干完");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("对比被打断了", e);
        }
    }

    private static void joinQuiet(Thread t) {
        try {
            t.join(5_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public record RunResult(
            String mode,
            int taskCount,
            int workerThreads,
            long elapsedMs,
            int peakThreads,
            String note
    ) {
    }

    public record Comparison(
            String summary,
            int taskCount,
            int poolSize,
            RunResult wasteful,
            RunResult pooled
    ) {
    }
}
