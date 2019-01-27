package resource;

public class AccountTransfer {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private String user;
    private String from;
    private String to;
    private int money;
    private StatusCode message;
    private int stage;
    private int port;
    private String info;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public AccountTransfer(long id, String user, String from, String to, int money,
        int stage, int port) {
        this.id = id;
        this.user = user;
        this.from = from;
        this.to = to;
        this.money = money;
        this.stage = stage;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public AccountTransfer() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public StatusCode getMessage() {
        return message;
    }

    public void setMessage(StatusCode message) {
        this.message = message;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }
}
