package connection;

public interface TCPConnectionHandler {
    String onGetAuthorizationString(TCPConnection tcpConnection) ;
     void onInputMessage(TCPConnection tcpConnection, String msg);

    void onConnection(TCPConnection tcpConnection);

    void onDisconnect(TCPConnection tcpConnection);

    void onException(TCPConnection tcpConnection, Exception e);


}
