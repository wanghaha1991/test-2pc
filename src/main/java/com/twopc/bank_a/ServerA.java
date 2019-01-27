package com.twopc.bank_a;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import resource.dao.DbUtils;
import resource.ServerSocketThread;

public class ServerA {

    public static final int PORT_A = 12000;
    public static Connection conn = DbUtils.getConn(DbUtils.MYSQL_DRIVER, DbUtils.MYSQL_URL, null);



    /**
     * Socket coordinator
     */
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT_A);
            System.out.println("server A started，waiting..");

            while (true) {
                Socket socket = serverSocket.accept();
                if (conn.isClosed()){
                    resetConn();
                }
                ServerSocketThread socketThread = new ServerSocketThread(socket, PORT_A, conn);
                socketThread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void resetConn(){
        conn = DbUtils.getConn(DbUtils.MYSQL_DRIVER, DbUtils.MYSQL_URL, null);
    }

}