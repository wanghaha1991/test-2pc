package com.twopc.coordinator;

import com.twopc.bank_a.ServerA;
import com.twopc.bank_b.ServerB;
import com.twopc.bank_c.ServerC;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import resource.AccountTransfer;
import resource.utils.ErrorUtil;

public class CoordinatorHandler extends AbstractHandler {



    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request,
        HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        int stage = 1;
        long id = getDealId();
        if (target.equalsIgnoreCase("/transfer")) {

            // pre commit
            if (stage == 1) {
                CyclicBarrier cyclicBarrier = new CyclicBarrier(4);
                try {
                    //send 3 message to server A B C
                    //a to b: 1.a to c, 2.c to b, 3.check
                    SendSocketThread sendServerA = new SendSocketThread(
                        new AccountTransfer(id,"a", "a", "UP", 500, 1,
                            ServerA.PORT_A), cyclicBarrier);
                    SendSocketThread sendServerB = new SendSocketThread(
                        new AccountTransfer(id,"b", "UP", "b", 500, 1,
                            ServerB.PORT_B), cyclicBarrier);
                    SendSocketThread sendServerC = new SendSocketThread(
                        new AccountTransfer(id,"UP", "a", "b", 500, 1,
                            ServerC.PORT_C), cyclicBarrier);
                    Coordinator.executor.execute(sendServerA);
                    Coordinator.executor.execute(sendServerB);
                    Coordinator.executor.execute(sendServerC);
                    cyclicBarrier.await(3000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
                    System.out.println("pre commit failed, do roll back");
                    // do rollback
                    ErrorUtil.doRollBack();
                }
                if (cyclicBarrier.getNumberWaiting() == 0 && Coordinator.portSet.isEmpty()) {
                    stage++;
                    System.out.println("pre-commit done");
                }
            }
            // do commit
            if (stage == 2) {
                CyclicBarrier cyclicBarrier = new CyclicBarrier(4);
                try {
                    //send 3 message to server A B C
                    //a to b: 1.a to c, 2.c to b, 3.check
                    SendSocketThread sendServerA = new SendSocketThread(
                        new AccountTransfer(id,"a", "a", "c", 500, 2,
                            ServerA.PORT_A), cyclicBarrier);
                    SendSocketThread sendServerB = new SendSocketThread(
                        new AccountTransfer(id,"b", "c", "b", 500, 2,
                            ServerB.PORT_B), cyclicBarrier);
                    SendSocketThread sendServerC = new SendSocketThread(
                        new AccountTransfer(id,"c", "a", "b", 500, 2,
                            ServerC.PORT_C), cyclicBarrier);
                    sendServerA.start();
                    sendServerB.start();
                    sendServerC.start();

                    cyclicBarrier.await(3000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
                    System.out.println("do commit failed, retry it");
                    //do rollback
                    ErrorUtil.doRollBack();
                }
                if (cyclicBarrier.getNumberWaiting() == 0 && Coordinator.portSet.isEmpty()) {
                    stage++;
                    System.out.println("do-commit done");
                }
            }
        }
    }
    private static long getDealId(){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return Long.parseLong(dateFormat.format(date));
    }
}
