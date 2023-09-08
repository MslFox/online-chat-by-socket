import connection.TCPConnection;
import logger.ChatLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServerTest {

    private final HashMap<TCPConnection, String> connectionsMap = new HashMap<>();
    private final ChatLogger chatLoggerMock = mock(ChatLogger.class);
    private final ServerJFrame serverJFrameMock = mock(ServerJFrame.class);
    private final HashMap<TCPConnection, String> connectionsMapMock = mock(HashMap.class);
    private final TCPConnection tcpConnectionMock = mock(TCPConnection.class);

    Server serverMockedFields = new Server(chatLoggerMock, serverJFrameMock, connectionsMapMock);

    @Test
    public void getServerChatLoggerTest() {
        Server serverMockedFields = new Server(chatLoggerMock, serverJFrameMock, connectionsMap);
        assertEquals(serverMockedFields.getServerChatLogger(), chatLoggerMock);
    }

    @ParameterizedTest
    @CsvSource({
            ",        ",
            "/exit,   ",
            "'',      ",
            "null",
            "anyString"
    })
    public void onGetAuthorizationStringTest(String incomeString) {
        when(tcpConnectionMock.readMessage()).thenReturn(incomeString);

        assertEquals(serverMockedFields.onGetAuthorizationString(tcpConnectionMock), incomeString);
    }

    @ParameterizedTest
    @CsvSource({
            ",         false,  0,  1",
            "/exit,    false,  0,  1",
            "'',       false,  0,  1",
            "null,      true,  1,  0",
            "anyString, true,  1,  0"
    })
    public void onConnectionTest(String authorizationString, boolean result, int addServiceLogTimes, int disconnectTimes) {
        Server serverMockedFieldsPartly = new Server(chatLoggerMock, serverJFrameMock, new HashMap<>());

        when(tcpConnectionMock.getAuthorizationString()).thenReturn(authorizationString);
        serverMockedFieldsPartly.onConnection(tcpConnectionMock);

        assertEquals(serverMockedFieldsPartly.getConnectionsMap().containsKey(tcpConnectionMock), result);

        verify(serverJFrameMock, times(addServiceLogTimes))
                .writeServiceLog(String.format("New connection %s<nickname = %s>  is accepted!",
                        tcpConnectionMock, tcpConnectionMock.getAuthorizationString()));

        verify(tcpConnectionMock, times(disconnectTimes)).disconnect();

    }

    @ParameterizedTest
    @CsvSource({
            ",          0,  1",
            "/exit,     0,  1",
            "'',        0,  0",
            "null,      1,  0",
            "anyString, 1,  0"
    })
    public void onInputMessageTest(String message, int keySetAndAddToChatLogTimes, int disconnectTimes) {
        serverMockedFields.onInputMessage(tcpConnectionMock, message);

        verify(connectionsMapMock, times(keySetAndAddToChatLogTimes)).keySet();
        verify(serverJFrameMock, times(keySetAndAddToChatLogTimes)).writeToChatAndLog(message);
        verify(tcpConnectionMock, times(disconnectTimes)).disconnect();
    }

    @ParameterizedTest
    @CsvSource({
            "true,      1",
            "false,     0",
    })
    public void onDisconnectTest(boolean isContainsKey, int addToServiceLogAndRemoveTimes) {

        when(connectionsMapMock.containsKey(tcpConnectionMock)).thenReturn(isContainsKey);
        serverMockedFields.onDisconnect(tcpConnectionMock);

        verify(serverJFrameMock, times(addToServiceLogAndRemoveTimes)).writeServiceLog(String.format("%s<nickname = %s> disconnected",
                tcpConnectionMock,
                tcpConnectionMock.getAuthorizationString()));
        verify(connectionsMapMock, times(addToServiceLogAndRemoveTimes)).remove(tcpConnectionMock);
        verify(tcpConnectionMock).disconnect();
    }

    @Test
    public void onExceptionTest() {
        IOException exception = new IOException();
        serverMockedFields.onException(tcpConnectionMock, exception);

        verify(connectionsMapMock).containsKey(tcpConnectionMock);
        verify(tcpConnectionMock).disconnect();
        verify(serverJFrameMock).writeServiceLog(tcpConnectionMock + "\nException: " + exception);
    }
}
