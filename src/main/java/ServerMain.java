import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ServerMain
{
    public static void main(String[] args) throws IOException
    {
        InetSocketAddress addr = new InetSocketAddress(8082);
        HttpServer server = HttpServer.create(addr, 0);

        String url= "/";

        //设置资源url
        server.createContext(url, new MyHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        Logger logger = LoggerFactory.getLogger(ServerMain.class);
        logger.debug("Server is Starting");
        logger.info("Server is listening on port {}",addr.getPort());

//        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//        StatusPrinter.print(lc);
    }
}
