package resource.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.util.Pair;
import resource.AccountTransfer;
import resource.StatusCode;
import resource.dao.po.DoLog;

public class DaoTransfer {

    //mysql -A
    public static Pair<Connection, AccountTransfer> pre_commitA(Connection conn,
        AccountTransfer transfer) throws Exception {
        conn.setAutoCommit(false);
        //select money
        try {
            int money = getMoneyByName(conn, transfer);
            if (money < transfer.getMoney()) {
                transfer.setMessage(StatusCode.PRE_COMMIT_FAILED);
            }

            // transfer accounts
            if (!transferAccount(conn, transfer)) {
                throw new Exception("can't transfer account");
            }
            //log pre_commit
            saveDoLog(conn, transfer);
        } catch (Exception e) {
            System.out.println("can't transfer account " + transfer.getUser());
            transfer.setMessage(StatusCode.PRE_COMMIT_FAILED);
            transfer.setInfo("can't transfer account");
            return new Pair<>(conn, transfer);
        }
        transfer.setMessage(StatusCode.PRE_COMMIT_SUCCESS);
        transfer.setInfo("can do transfer account");
        return new Pair<>(conn, transfer);
    }

    //mariadb -100 -B
    public static Pair<Connection, AccountTransfer> pre_commitB(Connection conn,
        AccountTransfer transfer) throws Exception {
        conn.setAutoCommit(false);
        try {
            // transfer accounts
            if (!transferAccount(conn, transfer)) {
                throw new Exception("can't transfer account");
            }
            //log pre_commit
            saveDoLog(conn, transfer);
        } catch (Exception e) {
            transfer.setMessage(StatusCode.PRE_COMMIT_FAILED);
            transfer.setInfo("can't transfer account");
            return new Pair<>(conn, transfer);
        }
        transfer.setMessage(StatusCode.PRE_COMMIT_SUCCESS);
        return new Pair<>(conn, transfer);
    }

    //mariadb -101 -UP
    public static Pair<Connection, AccountTransfer> pre_commitC(Connection conn,
        AccountTransfer transfer) throws Exception {
        conn.setAutoCommit(false);
        //log pre_commit
        saveDoLog(conn, transfer);
        transfer.setMessage(StatusCode.PRE_COMMIT_SUCCESS);
        return new Pair<>(conn, transfer);
    }

    private static int getMoneyByName(Connection conn, AccountTransfer transfer) throws Exception {
        PreparedStatement statement = conn
            .prepareStatement("select money from account where name=?");
        statement.setString(1, transfer.getFrom());
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getInt("money");
    }

    private static boolean transferAccount(Connection conn, AccountTransfer transfer)
        throws SQLException {
        PreparedStatement statement = conn
            .prepareStatement("update account set money=money-? where name=?");
        statement.setInt(1, transfer.getMoney());
        statement.setString(2, transfer.getFrom());
        int p1 = statement.executeUpdate();

        statement = conn
            .prepareStatement("update account set money=money+? where name=?");
        statement.setInt(1, transfer.getMoney());
        statement.setString(2, transfer.getTo());
        int p2 = statement.executeUpdate();

        return p1 != 0 && p1 == p2;
    }

    private static void saveDoLog(Connection conn, AccountTransfer transfer)
        throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(
            "insert into doneLog(_from,_to,trans_time,deal_id,stage) values(?,?,?,?,?)");
        preparedStatement.setString(1, transfer.getFrom());
        preparedStatement.setString(2, transfer.getTo());
        preparedStatement.setLong(3, System.currentTimeMillis());
        preparedStatement.setLong(4, transfer.getId());
        preparedStatement.setInt(5, transfer.getStage());
        preparedStatement.executeUpdate();
    }

    public static void updateDoLog(Connection conn, AccountTransfer transfer) {
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(
                "update doneLog set stage=? where deal_id=?");
            preparedStatement.setInt(1, transfer.getStage());
            preparedStatement.setLong(2, transfer.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
