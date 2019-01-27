package com.twopc.coordinator;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.jetty.server.Server;
import resource.AccountTransfer;

public class Coordinator {
    public static Set<AccountTransfer> portSet= new HashSet<>();
    public static ExecutorService executor = Executors.newFixedThreadPool(5);

    /**
     * Socket client
     */
    public static void main(String[] args) throws Exception{
        Server server = new Server(8888);
        server.setHandler(new CoordinatorHandler());
        server.start();
        server.join();
    }
}

