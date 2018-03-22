package in.sheki.jedis.benchmark;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        CommandLineArgs cla = Benchmark.getCommandLineArgs(args);
        System.out.println("\nTest method: SET\n");
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(cla.noOps);
        Benchmark benchmark = new Benchmark(cla.noOps, cla.noThreads, cla.noConnections, cla.host, cla.port, cla.dataSize, cla.sentinel, cla.auth, queue);
        benchmark.test();
        benchmark.performBenchmark(0);
        System.out.println("\n\nStatistics:");
        benchmark.printStats();

        System.out.println("\nTest method: GET\n");
        benchmark = new Benchmark(cla.noOps, cla.noThreads, cla.noConnections, cla.host, cla.port, cla.dataSize, cla.sentinel, cla.auth, queue);
        benchmark.test();
        benchmark.performBenchmark(1);
        System.out.println("\n\nStatistics:");
        benchmark.printStats();

        System.out.println("Queue size: " + queue.size());
    }
}
