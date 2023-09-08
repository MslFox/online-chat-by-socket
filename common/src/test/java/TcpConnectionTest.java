import connection.TCPConnection;
import connection.TCPConnectionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TcpConnectionTest {
    private final Socket socketMock = mock(Socket.class, RETURNS_DEEP_STUBS);
    private final TCPConnectionHandler tcpConnectionHandlerMock = mock(TCPConnectionHandler.class);
    private final BufferedReader inMock = mock(BufferedReader.class);
    private final BufferedWriter outMock = mock(BufferedWriter.class, RETURNS_DEEP_STUBS);
    private TCPConnection tcpConnectionMockedFields = new TCPConnection(tcpConnectionHandlerMock, socketMock, inMock, outMock);

    @Test
    public void constructorTestInputStreamException() throws IOException {
        when(socketMock.getInputStream()).thenThrow(new IOException());
        assertThrows(IOException.class, () -> new TCPConnection(tcpConnectionHandlerMock, socketMock));
    }

    @Test
    public void constructorTestOutStreamException() throws IOException {
        when(socketMock.getOutputStream()).thenThrow(new IOException());
        assertThrows(IOException.class, () -> new TCPConnection(tcpConnectionHandlerMock, socketMock));
    }


    @Test
    public void runConnectionDisconnectTest() throws IOException {
        tcpConnectionMockedFields.disconnect();
        tcpConnectionMockedFields.runConnection();
        verify(tcpConnectionHandlerMock).onConnection(tcpConnectionMockedFields);
        verify(tcpConnectionHandlerMock, times(0)).onInputMessage(tcpConnectionMockedFields, inMock.readLine());
    }

    @Test
    public void getAuthorizationStringTest() throws IOException {
        when(tcpConnectionHandlerMock.onGetAuthorizationString(any(TCPConnection.class)))
                .thenReturn("message");
        tcpConnectionMockedFields = new TCPConnection(tcpConnectionHandlerMock, socketMock);
        assertEquals(
                tcpConnectionMockedFields.getAuthorizationString(), "message");
    }

    @Test
    public void sendMessageTest() throws IOException {
        tcpConnectionMockedFields.sendMessage("message");
        verify(outMock).write("message" + System.lineSeparator());
        verify(outMock).flush();
    }

    @ParameterizedTest
    @CsvSource({
            ",",
            "/exit,/exit",
            "'',''",
            "null,null",
            "anyString,anyString"
    })
    public void readMessageTest(String expected, String actual) throws IOException {
        when(inMock.readLine()).thenReturn(expected);
        assertEquals(tcpConnectionMockedFields.readMessage(), actual);
    }

    @Test
    public void readMessageExceptionTest() throws IOException {
        IOException e = new IOException();
        when(inMock.readLine()).thenThrow(e);
        tcpConnectionMockedFields.readMessage();
        verify(tcpConnectionHandlerMock).onException(tcpConnectionMockedFields, e);

    }

    @Test
    public void disconnectTest() throws IOException {
        tcpConnectionMockedFields.disconnect();
        tcpConnectionMockedFields.disconnect();

        verify(inMock, times(1)).close();
        verify(outMock, times(1)).close();
        verify(socketMock, times(1)).close();
    }
}



