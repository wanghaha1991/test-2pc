package com.twopc.bank_b;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import resource.dao.DbUtils;
import resource.ServerSocketThread;

public class ServerB {

    public static final int PORT_B = 12001;
    public static Connection conn = DbUtils.getConn(DbUtils.MARIADB_DRIVER, DbUtils.MARIADB_B_URL, "123");

    /**
     * Socket coordinator
     */
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT_B);
            System.out.println("server B started，waiting..");
            //one request one conn

            while (true) {
                Socket socket = serverSocket.accept();
                if (conn.isClosed()){
                    resetConn();
                }
                ServerSocketThread socketThread = new ServerSocketThread(socket, PORT_B, conn);
                socketThread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetConn(){
        conn = DbUtils.getConn(DbUtils.MARIADB_DRIVER, DbUtils.MARIADB_B_URL, "123");
    }

}