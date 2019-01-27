package resource.utils;

import com.twopc.coordinator.Coordinator;
import com.twopc.coordinator.SendSocketThread;
import java.util.Set;
import resource.AccountTransfer;

public class ErrorUtil {
    public static void doRollBack(){
        Set<AccountTransfer> needRollBackSet = Coordinator.portSet;
        needRollBackSet.stream().forEach(at->at.setStage(-1));
        for (AccountTransfer at: needRollBackSet){
            Coordinator.executor.execute(new SendSocketThread(at, null));
        }
    }

}
