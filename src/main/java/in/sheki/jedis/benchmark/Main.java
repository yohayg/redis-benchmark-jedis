package in.sheki.jedis.benchmark;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        CommandLineArgs cla = Benchmark.getCommandLineArgs(args);
        System.out.println("\nSET\n");
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(cla.noOps);
        Benchmark benchmark = new Benchmark(cla.noOps, cla.noThreads, cla.noConnections, cla.host, cla.port, cla.dataSize, cla.sentinel, cla.auth, queue);
        benchmark.test();
        System.out.println("In progress");
        benchmark.performBenchmark(0);
        System.out.println("\n\nDone");
        benchmark.printStats();

        System.out.println("\nGET\n");
        benchmark = new Benchmark(cla.noOps, cla.noThreads, cla.noConnections, cla.host, cla.port, cla.dataSize, cla.sentinel, cla.auth, queue);
        benchmark.test();
        System.out.println("In progress");
        benchmark.performBenchmark(1);
        System.out.println("\n\nDone");
        benchmark.printStats();

        System.out.println("\nGET\n");
        benchmark = new Benchmark(cla.noOps, cla.noThreads, cla.noConnections, cla.host, cla.port, cla.dataSize, cla.sentinel, cla.auth, queue);
        benchmark.test();
        System.out.println("In progress");
        benchmark.performBenchmark(2);
        System.out.println("\n\nDone");
        benchmark.printStats();

        System.out.println("Queue size: " + queue.size());
    }
}
