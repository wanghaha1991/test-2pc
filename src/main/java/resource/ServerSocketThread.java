package resource;

import com.twopc.bank_a.ServerA;
import com.twopc.bank_b.ServerB;
import com.twopc.bank_c.ServerC;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import javafx.util.Pair;
import resource.dao.DaoTransfer;
import resource.utils.JsonUtil;


public class ServerSocketThread extends Thread {

    private Socket socket;
    private int port;
    private Connection conn;

    public ServerSocketThread(Socket socket, int port, Connection coon) {
        this.socket = socket;
        this.port = port;
        this.conn = coon;
    }

    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(
                inputStream);
            BufferedReader bufferedReader = new BufferedReader(
                inputStreamReader);
            String temp = null;
            StringBuilder info = new StringBuilder();
            while ((temp = bufferedReader.readLine()) != null) {
                info.append(temp);
                System.out.println("server accept connection");
                System.out.println("client info ：" + info);
            }
            AccountTransfer transfer = JsonUtil.fromJson(info.toString());

            if (transfer.getStage() == 1) {
                // pre-commit
                System.out.println("do pre_commit");
                Pair<Connection, AccountTransfer> pair = pre_commit(conn,
                    transfer);
                conn = pair.getKey();
            }
            if (transfer.getStage() == 2) {
                // do commit
                if (conn != null) {
                    transfer.setMessage(StatusCode.DO_COMMIT_SUCCESS);
                    // do commit success, we should record the done log in mysql
                    DaoTransfer.updateDoLog(conn, transfer);
                    conn.commit();
                    conn.close();
                }
            }
            // do rollback
            if (transfer.getStage() == -1) {
                conn.rollback();
                DaoTransfer.updateDoLog(conn, transfer);
                conn.close();
                transfer.setMessage(StatusCode.ROLLBACK_SUCCESS);
            }

            System.out.println("ready to send to coordinator");
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.print(JsonUtil.toJson(transfer));
            printWriter.flush();
            socket.shutdownOutput();
            System.out.println("send to coordinator done");

            // close
            bufferedReader.close();
            inputStream.close();
            printWriter.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // do pre commit
    private Pair<Connection, AccountTransfer> pre_commit(Connection conn, AccountTransfer transfer)
        throws Exception {
        switch (port) {
            case ServerA.PORT_A:
                return DaoTransfer.pre_commitA(conn, transfer);
            case ServerB.PORT_B:
                return DaoTransfer.pre_commitB(conn, transfer);
            case ServerC.PORT_C:
                return DaoTransfer.pre_commitC(conn, transfer);
            default:
                return new Pair<>(null, null);
        }
    }
}
