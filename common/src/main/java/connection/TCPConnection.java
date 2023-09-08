package connection;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPConnection {
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final TCPConnectionHandler tcpConnectionHandler;
    private final String authorizationString;
    private boolean isDisconnected;

    public TCPConnection(TCPConnectionHandler tcpConnectionHandler, String host, int port) throws IOException {
        this(tcpConnectionHandler, new Socket(host, port));
    }

    public TCPConnection(TCPConnectionHandler tcpConnectionHandler, Socket socket) throws IOException {
        this(tcpConnectionHandler,socket,
                new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)),
                new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)));
    }
    public TCPConnection(TCPConnectionHandler tcpConnectionHandler, Socket socket, BufferedReader in, BufferedWriter out ) {
        this.tcpConnectionHandler = tcpConnectionHandler;
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.authorizationString = tcpConnectionHandler.onGetAuthorizationString(this);

    }

    public void runConnection() {
        tcpConnectionHandler.onConnection(this);
        while (!isDisconnected) {
            tcpConnectionHandler.onInputMessage(this, readMessage());
        }
    }

    public String getAuthorizationString() {
        return authorizationString;
    }

    public void sendMessage(String message) {
        try {
            out.write(message + System.lineSeparator());
            out.flush();
        } catch (IOException e) {
            tcpConnectionHandler.onException(this, e);
        }

    }

    public String readMessage() {
        try {
            return in.readLine();
        } catch (IOException e) {
            tcpConnectionHandler.onException(this, e);
        }
        return null;
    }

    public void disconnect() {
        if (!isDisconnected) {
            isDisconnected = true;
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                tcpConnectionHandler.onException(this, e);
            }

        }
    }
}




