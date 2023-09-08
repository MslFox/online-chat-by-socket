import connection.TCPConnection;
import connection.TCPConnectionHandler;
import logger.ChatLogger;

import java.io.IOException;

import static constant.ConstantHolder.*;


public class Client implements TCPConnectionHandler {
    private final ClientJFrame clientJFrame;
    private final String authorizationString;
    private final ChatLogger clientLogger;

    private Client() throws IOException {
        this.clientLogger = new ChatLogger(
                PATH_CLIENT_LOGS + this.hashCode() + "ChatLog.txt",
                PATH_CLIENT_LOGS + this.hashCode() + "ServiceLog.txt");
        this.clientJFrame = new ClientJFrame(Client.this);
        this.authorizationString = clientJFrame.getAuthorizationNickname();
        getClientLogger().writeServiceLog("Успешная авторизция <authorizationString = " + getAuthorizationString() + ">");
        TCPConnection tcpConnection = null;
        try {
            tcpConnection = new TCPConnection(Client.this, HOST, PORT);
        } catch (IOException e) {
            clientJFrame.showInfoMessageJFrame("Ошибка связи с свервером!\n" +
                    "Перезапустите серевер!\n" +
                    "Перезапустите программу входа в чат!");
            getClientLogger().writeServiceLog("Ошибка связи с сервером <authorizationString = " + getAuthorizationString() + ">");
            System.exit(0);
        }
        tcpConnection.runConnection();
    }

    public static void main(String[] args) throws IOException {
        new Client();
    }

    public String getAuthorizationString() {
        return authorizationString;
    }

    public ChatLogger getClientLogger() {
        return clientLogger;
    }

    public void sendOutputMessage(TCPConnection tcpConnection, String message) {
        tcpConnection.sendMessage(message);
    }

    @Override
    public String onGetAuthorizationString(TCPConnection tcpConnection) {
        return getAuthorizationString();
    }

    @Override
    public synchronized void onConnection(TCPConnection tcpConnection) {
        clientJFrame.showChatJFrame(tcpConnection);
        tcpConnection.sendMessage(tcpConnection.getAuthorizationString());
        clientJFrame.addToChatLog("Вы присоединились к чату!");
        getClientLogger().writeServiceLog("Клиент установил соединение");
    }

    @Override
    public void onInputMessage(TCPConnection tcpConnection, String message) {
        if (message == null || message.equals(EXIT_STRING)) onDisconnect(tcpConnection);
        else clientJFrame.addToChatLog(message);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        tcpConnection.disconnect();
        getClientLogger().writeServiceLog("Клиент разорвал соединение");
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        onDisconnect(tcpConnection);
        getClientLogger().writeServiceLog(e.getMessage());
        clientJFrame.showInfoMessageJFrame(
                "В работе приложения произошла ошибка!\n" +
                        e.getMessage() +
                        "\nПриложение будет закрыто!");
        System.exit(0);
    }
}