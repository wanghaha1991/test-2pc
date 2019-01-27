package com.twopc.bank_c;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import resource.dao.DbUtils;
import resource.ServerSocketThread;

public class ServerC {

    public static final int PORT_C = 12002;
    public static Connection conn = DbUtils.getConn(DbUtils.MARIADB_DRIVER, DbUtils.MARIADB_C_URL, "123");

    /**
     * Socket coordinator
     */
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT_C);
            System.out.println("server C started，waiting..");
            while (true) {
                Socket socket = serverSocket.accept();
                if (conn.isClosed()){
                    resetConn();
                }
                ServerSocketThread socketThread = new ServerSocketThread(socket, PORT_C, conn);
                socketThread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetConn(){
        conn = DbUtils.getConn(DbUtils.MARIADB_DRIVER, DbUtils.MARIADB_C_URL, "123");
    }

}