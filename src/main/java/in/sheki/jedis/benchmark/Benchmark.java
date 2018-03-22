package in.sheki.jedis.benchmark;

import com.beust.jcommander.JCommander;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.Pool;

import java.util.*;
import java.util.concurrent.*;


public class Benchmark {


    private final int numberOfOperations;
    private final BlockingQueue<Long> setRunTimes;
    private final Pool<Jedis> pool;
    private final String auth;
    private final String data;
    private final CountDownLatch shutDownLatch;
    private PausableThreadPoolExecutor executor;
    private BlockingQueue<String> queue;
    private long totalNanoRunTime;
    private final ProgressBar progressBar;

    public Benchmark(final int noOps, final int noThreads, final int noJedisConn, final String host, final int port, int dataSize, int sentinel, String auth, BlockingQueue<String> queue) {
        this.auth = auth;
        this.numberOfOperations = noOps;
        setRunTimes = new ArrayBlockingQueue<>(noOps);
        this.executor = new PausableThreadPoolExecutor(noThreads, noThreads, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        this.queue = queue;

        if (sentinel == 0) {
            final GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setMaxTotal(noJedisConn);
            Set<String> sentinels = new HashSet<>();
            sentinels.add(host + ":" + port);
            this.pool = new JedisSentinelPool("rtpmaster", sentinels, poolConfig);
        } else {
            this.pool = new JedisPool(host, port);
        }
        this.data = RandomStringUtils.random(dataSize);
        shutDownLatch = new CountDownLatch(noOps);
        progressBar = new ProgressBar("Operations", numberOfOperations, ProgressBarStyle.ASCII);


    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting");
        CommandLineArgs cla = getCommandLineArgs(args);
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(cla.noOps);
        start(cla, 0, queue);
    }

    public static CommandLineArgs getCommandLineArgs(String[] args) {
        CommandLineArgs cla = new CommandLineArgs();
        new JCommander(cla, args);
        System.out.println(cla.toString());
        return cla;
    }

    private static void start(CommandLineArgs cla, int type, BlockingQueue<String> queue) throws InterruptedException {
        Benchmark benchmark = new Benchmark(cla.noOps, cla.noThreads, cla.noConnections, cla.host, cla.port, cla.dataSize, cla.sentinel, cla.auth, queue);
        benchmark.test();
        System.out.println("In progress");
        benchmark.performBenchmark(type);
        System.out.println("Done");
        benchmark.printStats();
    }

    public void test() {
        System.out.println("Testing Connection");
        Jedis jedis = pool.getResource();
        if (!auth.isEmpty()) {
            jedis.auth(auth);
        }
        jedis.set(RandomStringUtils.random(15), data);
        pool.returnResource(jedis);
        System.out.println("Connection test OK");
    }

    public void performBenchmark(int type) throws InterruptedException {
        progressBar.start();
        executor.pause();
        for (int i = 0; i < numberOfOperations; i++) {
            executor.submit(new RedisOperationTask(shutDownLatch, type));
        }
        long startTime = System.nanoTime();
        executor.resume();
        executor.shutdown();
        shutDownLatch.await();
        totalNanoRunTime = System.nanoTime() - startTime;
        progressBar.stop();
    }

    public void printStats() {
        List<Long> points = new ArrayList<>();
        setRunTimes.drainTo(points);
        Collections.sort(points);
        long sum = 0;
        for (Long l : points) {
            sum += l;
        }
        System.out.println("Data size :" + data.length());
        System.out.println("Threads : " + executor.getMaximumPoolSize());
        System.out.println("Time Test Ran for (ms) : " + TimeUnit.NANOSECONDS.toMillis(totalNanoRunTime));
        System.out.println("Average : " + TimeUnit.NANOSECONDS.toMicros(sum / points.size()));
        System.out.println("50 % <=" + TimeUnit.NANOSECONDS.toMicros(points.get((points.size() / 2) - 1)));
        System.out.println("90 % <=" + TimeUnit.NANOSECONDS.toMicros(points.get((points.size() * 90 / 100) - 1)));
        System.out.println("95 % <=" + TimeUnit.NANOSECONDS.toMicros(points.get((points.size() * 95 / 100) - 1)));
        System.out.println("99 % <=" + TimeUnit.NANOSECONDS.toMicros(points.get((points.size() * 99 / 100) - 1)));
        System.out.println("99.9 % <=" + TimeUnit.NANOSECONDS.toMicros(points.get((points.size() * 999 / 1000) - 1)));
        System.out.println("100 % <=" + TimeUnit.NANOSECONDS.toMicros(points.get(points.size() - 1)));
        System.out.println((numberOfOperations * 1000 / TimeUnit.NANOSECONDS.toMillis(totalNanoRunTime)) + " Operations per sec");
    }

    class RedisOperationTask implements Runnable {
        private CountDownLatch latch;
        private int type;

        RedisOperationTask(CountDownLatch latch, int type) {
            this.latch = latch;
            this.type = type;
        }

        public void run() {

            Jedis jedis = pool.getResource();
            if (!auth.isEmpty()) {
                jedis.auth(auth);
            }


            Long startTime;
            if (type == 0) {
                String random = RandomStringUtils.random(15);
                try {

                    queue.put(random);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startTime = System.nanoTime();
                jedis.set(random, data);
            } else {
                String key = queue.poll();
                startTime = System.nanoTime();
                jedis.get(key);
            }
            if (!setRunTimes.offer(System.nanoTime() - startTime)) {
                System.out.println("Error. Could not add to run time queue a new time");
            }
//            System.out.print("\r" + (System.nanoTime() - startTime) / 1000);
            progressBar.step();
            pool.returnResource(jedis);
            latch.countDown();
        }
    }


}
