import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class Connection {

    private final Socket socket;
    private final Thread rxThread;
    private final BufferedReader in;
    private final BufferedWriter out;
    Server server;


    String lastMessage;
    String LOGIN;

    public Connection(Socket socket, Server server) throws IOException {
        this.server = server;

        this.socket = socket;
        socket.setSoTimeout(1000);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),Charset.forName("UTF-8")));
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                server.onConnectionReady(Connection.this);
                while (!rxThread.isInterrupted()) {
                    try {
                        lastMessage = null;
                        lastMessage = in.readLine();
                        if (lastMessage == null)
                        {
                            Connection.this.disconnect();
                            server.onDisconnect(Connection.this);
                            continue;
                        }
                        server.onReceiveString(Connection.this, lastMessage);
                    } catch (SocketTimeoutException e) {
                        sendString("empty", " ");
                    } catch (SocketException e) {
                        Connection.this.disconnect();
                        server.onDisconnect(Connection.this);
                    } catch (IOException e) {
                        System.out.println("Connection exception: " + e);
                    }
                }
            }
        });
        rxThread.start();
    }

    public synchronized void sendString(String KEY, String value){
        try {
            out.write(KEY + "-" + value + "\r\n");
            out.flush();
        }catch (IOException e){
            Connection.this.disconnect();
            server.onDisconnect(Connection.this);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void disconnect(){
        rxThread.interrupt();
        try {
            socket.close();
        }catch (IOException e){
            System.out.println("Connection exception: " + e);
        }
    }

    @Override
    public String toString() {
        return "Connection: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}
