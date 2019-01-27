package resource.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import resource.AccountTransfer;

public class JsonUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    public static String toJson(AccountTransfer transfer) throws Exception {
        return mapper.writeValueAsString(transfer);
    }

    public static AccountTransfer fromJson(String json) throws Exception {
        return mapper.readValue(json, AccountTransfer.class);
    }

}
