package ru.bmstu.ORM.Service.Session;

public class SessionFactory {
    private final String connectionString;
    private Session currentSession;

    public SessionFactory(String host, String port, String user, String catalog, String password) {
        this.connectionString = "jdbc:postgresql://" +
                host + ":" +
                port + "/" +
                catalog +
                "?user=" + user +
                "&password=" + password;
    }

    public Session openSession() {
        currentSession = new Session(connectionString);
        currentSession.open();
        return currentSession;
    }

    public Session getCurrentSession() {
        return currentSession;
    }
}
