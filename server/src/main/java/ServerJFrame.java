import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static constant.ConstantHolder.*;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

public class ServerJFrame {
    private final JFrame serverJFrame = new JFrame();
    private final JTextArea serverLogTextArea = new JTextArea();
    private final JTextArea chatLogTextArea = new JTextArea();
    private final Server server;

    public ServerJFrame(Server server) {
        this.server = server;
        serverJFrame.setTitle("Server");
        serverJFrame.setIconImage(ICON);
        serverJFrame.setLayout(new GridLayout(2, 1));
        serverJFrame.setBounds(FRAME_X_POS, FRAME_Y_POS, FRAME_WIGHT, FRAME_HEIGHT);

        serverLogTextArea.setEnabled(false);
        serverLogTextArea.setLineWrap(true);
        serverLogTextArea.setWrapStyleWord(true);
        serverLogTextArea.setToolTipText("Server Log");
        serverLogTextArea.setDisabledTextColor(Color.BLACK);
        JScrollPane serverScrollPane = new JScrollPane(serverLogTextArea);
        serverScrollPane.setBorder(
                new CompoundBorder(new TitledBorder("Service Log"), new LineBorder(Color.LIGHT_GRAY)));
        ((DefaultCaret) serverLogTextArea.getCaret()).
                setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        chatLogTextArea.setEnabled(false);
        chatLogTextArea.setLineWrap(true);
        chatLogTextArea.setWrapStyleWord(true);
        chatLogTextArea.setToolTipText("Chat Log");
        chatLogTextArea.setDisabledTextColor(Color.BLACK);
        ((DefaultCaret) chatLogTextArea.getCaret()).
                setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane chatScrollPane = new JScrollPane(chatLogTextArea);
        chatScrollPane.setBorder(
                new CompoundBorder(new TitledBorder("Chat Log"), new LineBorder(Color.LIGHT_GRAY)));

        serverJFrame.add(serverScrollPane);
        serverJFrame.add(chatScrollPane);
        serverJFrame.setVisible(true);
        serverJFrame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        serverJFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                String[] options = new String[2];
                options[0] = "Yes, sure";
                options[1] = "No, not sure";
                int answer = JOptionPane.showOptionDialog(
                        serverJFrame, "All connections will be lost!\nAre you sure?", "Disconnect",
                        JOptionPane.YES_NO_OPTION, QUESTION_MESSAGE, null, options, options[1]);
                if (answer == 0) {
                    serverLogTextArea.setDisabledTextColor(Color.LIGHT_GRAY);
                    serverJFrame.setEnabled(false);
                    writeServiceLog("Server stopped!!! Closing connections...");
                    serverJFrame.dispose();
                    System.exit(0);
                }
            }
        });
    }

    public synchronized void writeServiceLog(String message) {
        serverLogTextArea.append(server
                .getServerChatLogger()
                .messageToServiceLogMessage(message));
        server.getServerChatLogger().writeServiceLog(message);
    }

    public synchronized void writeToChatAndLog(String message) {
        final var logString = server
                .getServerChatLogger()
                .messageToServiceLogMessage(message);
        chatLogTextArea.append(logString);
        server.getServerChatLogger().writeChatLog(logString);
    }
}