import connection.TCPConnection;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static constant.ConstantHolder.*;
import static javax.swing.JOptionPane.*;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;

public class ClientJFrame extends JFrame {
    private final Client client;
    private final JFrame jFrame;
    private final JTextArea inputTextArea = new JTextArea();
    private final JTextArea chatLogTextArea = new JTextArea();

    public ClientJFrame(Client client) {
        this.client = client;
        this.jFrame = new JFrame();
        jFrame.setIconImage(ICON);
        jFrame.setBounds(FRAME_X_POS - 240, FRAME_Y_POS - 140, FRAME_WIGHT, FRAME_HEIGHT);

    }

    protected void sendClientMessage(TCPConnection tcpConnection) {
        String message = inputTextArea.getText().strip();
        inputTextArea.setText("");
        if (message.isBlank()) return;
        client.sendOutputMessage(tcpConnection, String.format("%s> %s",
                client.getAuthorizationString(),
                message.replaceAll("\n", "\u0639")));
        if (message.equals(EXIT_STRING)) {
            jFrame.dispose();
            client.onDisconnect(tcpConnection);
            return;
        }
        addToChatLog("Вы> " + message);
    }

    public final synchronized void addToChatLog(String message) {
        chatLogTextArea.append(message + System.lineSeparator());
        client.getClientLogger().writeChatLog(message + System.lineSeparator());
    }


    public void showChatJFrame(TCPConnection tcpConnection) {
        jFrame.setLayout(new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS));
        jFrame.setTitle("Простой сетевой чат. Пользователь: " + client.getAuthorizationString());
        chatLogTextArea.setEnabled(false);
        chatLogTextArea.setWrapStyleWord(true);
        chatLogTextArea.setToolTipText("Чат");
        ((DefaultCaret) chatLogTextArea.getCaret()).
                setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        chatLogTextArea.setDisabledTextColor(Color.BLACK);
        JScrollPane outScrollPane = new JScrollPane(chatLogTextArea);
        outScrollPane.setBorder(
                new CompoundBorder(new TitledBorder("Чат"), new LineBorder(Color.LIGHT_GRAY)));

        inputTextArea.setRows(6);
        inputTextArea.setColumns(1000);
        inputTextArea.setEnabled(true);
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setToolTipText("Ведите сообщение (для отправки Ctrl+Enter)");
        ((DefaultCaret) inputTextArea.getCaret()).
                setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane inScrollPane = new JScrollPane(inputTextArea, VERTICAL_SCROLLBAR_NEVER, HORIZONTAL_SCROLLBAR_NEVER);
        inScrollPane.setMaximumSize(inputTextArea.getPreferredSize());
        inScrollPane.setBorder(
                new CompoundBorder(new TitledBorder("Ведите сообщение (для отправки Ctrl+Enter)"), new LineBorder(Color.LIGHT_GRAY)));

        ImageIcon chatImage = new ImageIcon(ICON);
        JButton sendButton = new JButton(chatImage);
        Dimension imageDimension = new Dimension(chatImage.getIconWidth() + 10, chatImage.getIconHeight() + 10);
        sendButton.setToolTipText("Нажмите для отправки сообщения в чат");
        sendButton.setMinimumSize(imageDimension);
        sendButton.setMaximumSize(imageDimension);
        sendButton.addActionListener((e) -> sendClientMessage(tcpConnection));

        Box boxButtons = Box.createHorizontalBox();
        boxButtons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        boxButtons.add(inScrollPane);
        boxButtons.add(sendButton);


        inputTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown())
                    sendClientMessage(tcpConnection);
            }
        });
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                jFrame.dispose();
                client.sendOutputMessage(tcpConnection, EXIT_STRING);
                client.onDisconnect(tcpConnection);
            }
        });
        jFrame.add(outScrollPane);
        jFrame.add(boxButtons);
        jFrame.setVisible(true);
    }

    public String getAuthorizationNickname() {
        setBasicSimpleJFrame(jFrame);
        jFrame.setIconImage(ICON);
        jFrame.setBounds(FRAME_X_POS - 240, FRAME_Y_POS - 140, FRAME_WIGHT, FRAME_HEIGHT);

        jFrame.setTitle("Авторизация");
        jFrame.setVisible(true);
        String nickname = JOptionPane.showInputDialog(jFrame,
                "Для подключению к чату укажите свой ник:",
                "Авторизация", QUESTION_MESSAGE);
        if (nickname == null || nickname.isEmpty()) {
            jFrame.setTitle("Ошибка авторизации");
            jFrame.setLocationRelativeTo(null);
            jFrame.setBounds(FRAME_X_POS, FRAME_Y_POS, 0, 0);
            int isAgain = JOptionPane.showOptionDialog(jFrame,
                    "Вы не ввели ник!\nПопробовать снова?",
                    "Ошибка авторизации",
                    OK_CANCEL_OPTION,
                    PLAIN_MESSAGE,
                    null, null, null);
            if (isAgain == 0) return getAuthorizationNickname();
            else System.exit(0);
        }
        jFrame.dispose();
        return nickname;
    }

    public void showInfoMessageJFrame(String infoMessage) {
        jFrame.setVisible(true);
        JOptionPane.showMessageDialog(jFrame, infoMessage);
    }

    public JFrame setBasicSimpleJFrame(JFrame jFrame) {
        jFrame.setLocationRelativeTo(null);
        jFrame.setBounds(FRAME_X_POS, FRAME_Y_POS, 0, 0);
        return jFrame;
    }
}
