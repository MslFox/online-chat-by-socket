package constant;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.time.format.DateTimeFormatter;

public class ConstantHolder {
    public final static String PATH_INITIAL_FILE ="common/init/initial.ini";
    public final static String PATH_FILE_IMAGE = "common/image/Chat_Image.png";
    public final static String PATH_SERVER_LOGS ="server/src/logs/";
    public final static String PATH_CLIENT_LOGS ="client/src/logs/";
    public final static String EXIT_STRING = "/exit";
    public final static DateTimeFormatter CHAT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d/MM/yy HH:mm:ss");
    public final static int FRAME_X_POS = 700;
    public final static int FRAME_Y_POS = 300;
    public final static int FRAME_WIGHT = 600;
    public final static int FRAME_HEIGHT = 500;
    public final static Image ICON;

    static {
        try {
            ICON = ImageIO.read(new File(PATH_FILE_IMAGE));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static final BufferedReader initReader;

    static {
        try {
            initReader = new BufferedReader(new FileReader(PATH_INITIAL_FILE));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public final static String HOST;
    public final static int PORT;
    static {
        try {
            HOST = initReader.readLine().split("server.host=")[1];
            PORT = Integer.parseInt(initReader.readLine().split("server.port=")[1]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
