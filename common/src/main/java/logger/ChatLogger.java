package logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

import static constant.ConstantHolder.CHAT_DATE_TIME_FORMATTER;
public class ChatLogger {
    private final BufferedWriter bufferedWriterChatLog;
    private final BufferedWriter bufferedWriterServiceLog;

    public ChatLogger(String pathChatLogFile, String pathServiceLogFile) throws IOException {
        bufferedWriterChatLog = new BufferedWriter(new FileWriter(pathChatLogFile, true));
        bufferedWriterServiceLog = new BufferedWriter(new FileWriter(pathServiceLogFile, true));
    }

    public void writeChatLog(String message) {
        try {
            bufferedWriterChatLog.write(message);
            bufferedWriterChatLog.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void writeServiceLog(String message) {
        try {
            bufferedWriterServiceLog.write(String.format("[%s] %s%s",
                    LocalDateTime.now().format(CHAT_DATE_TIME_FORMATTER),
                    message,
                    System.lineSeparator()));
            bufferedWriterServiceLog.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized String messageToServiceLogMessage(String message){
        return String.format("[%s] %s%s",
                LocalDateTime.now().format(CHAT_DATE_TIME_FORMATTER),
                message,
                System.lineSeparator());
    }
}
