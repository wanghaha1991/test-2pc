package com.twopc.coordinator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import resource.AccountTransfer;
import resource.utils.JsonUtil;
import resource.StatusCode;

public class SendSocketThread extends Thread {

    private AccountTransfer accountTransfer;
    private CyclicBarrier barrier;

    public SendSocketThread(AccountTransfer transfer, CyclicBarrier barrier) {
        this.accountTransfer = transfer;
        this.barrier = barrier;
    }

    @Override
    public void run() {
        try {
            String info = transfer();
            // pre commit
            if (accountTransfer.getStage() == 1) {
                AccountTransfer transfer = JsonUtil.fromJson(info);
                if (transfer.getMessage().equals(StatusCode.PRE_COMMIT_FAILED)) {
                    abort();
                    return;
                } else {
                    transfer.setStage(transfer.getStage() + 1);
                }
            }
            if (accountTransfer.getStage() == -1) {
                AccountTransfer transfer = JsonUtil.fromJson(info);
                if (transfer.getMessage().equals(StatusCode.ROLLBACK_SUCCESS)){
                    System.out.println(transfer.getUser() + " do rollback");
                }
            }
            if (barrier != null){
                barrier.await(3000, TimeUnit.MILLISECONDS);
            }
        } catch (TimeoutException | InterruptedException | BrokenBarrierException e) {
            System.out.println(e.getMessage());
            // do abort
            abort();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // do abort
            abort();
        }

    }


    private void abort() {
        // record the failed client
        Coordinator.portSet.add(accountTransfer);
    }

    //socket transfer info
    private String transfer() throws Exception {
        Socket socket = null;
        OutputStream outputStream = null;
        PrintWriter printWriter = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        StringBuilder info = null;
        try {
            socket = new Socket("localhost", accountTransfer.getPort());
            socket.setSoTimeout(2000);
            outputStream = socket.getOutputStream();
            printWriter = new PrintWriter(outputStream);
            printWriter.print(JsonUtil.toJson(accountTransfer));
            printWriter.flush();
            socket.shutdownOutput();

            inputStream = socket.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            info = new StringBuilder();
            String temp = null;
            while ((temp = bufferedReader.readLine()) != null) {
                info.append(temp);
                System.out.println("client accept info：" + info.toString());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            //close
            try {
                bufferedReader.close();
                inputStream.close();
                printWriter.close();
                outputStream.close();
                socket.close();
            } catch (Exception e) {
                // do nothing
            }
        }
        return info.toString();
    }
}
