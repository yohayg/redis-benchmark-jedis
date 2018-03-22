package in.sheki.jedis.benchmark;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;

import java.util.List;


public class CommandLineArgs {
    @Parameter(names = "-n", description = "No. of operations")
    public Integer noOps = 100000;
    @Parameter(names = "-t", description = "No. of threads")
    public Integer noThreads = 1;
    @Parameter(names = "-c", description = "No of connections")
    public Integer noConnections = 1;
    @Parameter(names = "-h", description = "Host")
    public String host = "localhost";
    @Parameter(names = "-p", description = "port")
    public Integer port = 6379;
    @Parameter(names = "-s", description = "data size in bytes")
    public Integer dataSize = 100;
    @Parameter(names = "-b", description = "No. of bulk operations")
    public Integer bulkOps = 1;
    @Parameter(names = "-sentinel", description = "use sentinel pool")
    public Integer sentinel = 0;
    @Parameter(names = "-a", description = "Authentication")
    public String auth = "";
    @Parameter
    private List<String> parameters = Lists.newArrayList();

    @Override
    public String toString() {
        return "CommandLineArgs{" +
                "parameters=" + parameters +
                ", noOps=" + noOps +
                ", noThreads=" + noThreads +
                ", noConnections=" + noConnections +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", dataSize=" + dataSize +
                '}';
    }
}
