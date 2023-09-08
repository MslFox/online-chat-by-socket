import connection.TCPConnection;
import connection.TCPConnectionHandler;
import logger.ChatLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import static constant.ConstantHolder.*;

public class Server implements TCPConnectionHandler {
    private final HashMap<TCPConnection, String> connectionsMap;
    private final ServerJFrame serverJFrame;
    private final ChatLogger serverChatLogger;

    private Server() throws IOException {
        this.connectionsMap = new HashMap<>();
        this.serverChatLogger = new ChatLogger(
                PATH_SERVER_LOGS + "serverChatLog.txt",
                PATH_SERVER_LOGS + "serverServiceLog.txt");
        this.serverJFrame = new ServerJFrame(this);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            serverJFrame.writeServiceLog("Server running... Server wait TCPConnections...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        TCPConnection tcpConnection = new TCPConnection(this, clientSocket);
                        tcpConnection.runConnection();
                    } catch (IOException e) {
                        serverJFrame.writeServiceLog("\nException in TCPConnection constructor : " + e);
                    }
                }).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // This constructor For testing ONLY!
    public Server(ChatLogger chatLogger, ServerJFrame serverJFrame, HashMap<TCPConnection, String> connectionsMap) {
        this.connectionsMap = connectionsMap;
        this.serverChatLogger = chatLogger;
        this.serverJFrame = serverJFrame;

    }

    public static void main(String[] args) throws IOException {
        new Server();
    }

    public final HashMap<TCPConnection, String> getConnectionsMap() {
        return connectionsMap;
    }

    public final ChatLogger getServerChatLogger() {
        return serverChatLogger;
    }

    @Override
    public final String onGetAuthorizationString(TCPConnection tcpConnection) {
        return tcpConnection.readMessage();
    }

    @Override
    public final synchronized void onConnection(TCPConnection tcpConnection) {
        String authorizationString = tcpConnection.getAuthorizationString();
        if (authorizationString != null
                && !authorizationString.equalsIgnoreCase(EXIT_STRING)
                && !authorizationString.isEmpty()) {
            connectionsMap.put(tcpConnection, authorizationString);
            serverJFrame.writeServiceLog(
                    String.format("New connection %s<nickname = %s>  is accepted!",
                            tcpConnection,
                            authorizationString));
            onInputMessage(tcpConnection, authorizationString + "> Присоединил(ся/ась) к чату!");
        } else {
            tcpConnection.disconnect();
        }
    }

    @Override
    public final synchronized void onInputMessage(TCPConnection tcpConnection, String message) {
        if (message == null ||
                message.equalsIgnoreCase(EXIT_STRING)) {
            onDisconnect(tcpConnection);
            return;
        }
        if (!message.isEmpty()) {
            connectionsMap.keySet().stream().
                    filter(connection -> !connection.equals(tcpConnection)).
                    forEach(connection -> connection.sendMessage(message));
            serverJFrame.writeToChatAndLog(message);
        }
    }

    @Override
    public final synchronized void onDisconnect(TCPConnection tcpConnection) {
        if (connectionsMap.containsKey(tcpConnection)) {
            onInputMessage(tcpConnection, tcpConnection.getAuthorizationString() + "> Покинул(а) чат!");
            serverJFrame.writeServiceLog(String.format("%s<nickname = %s> disconnected",
                    tcpConnection,
                    tcpConnection.getAuthorizationString()));
            connectionsMap.remove(tcpConnection);
        }
        tcpConnection.disconnect();
    }

    @Override
    public final synchronized void onException(TCPConnection tcpConnection, Exception exception) {
        onDisconnect(tcpConnection);
        serverJFrame.writeServiceLog(tcpConnection + "\nException: " + exception);
    }
}