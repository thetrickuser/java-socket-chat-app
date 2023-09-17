package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {

    public static List<ClientHandler> clients = new ArrayList<>();
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String username;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = reader.readLine();
            clients.add(this);
            broadcastMessage("SERVER: " + username + " just joined the chat");
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    @Override
    public void run() {
        String clientMessage;

        while (socket.isConnected()) {
            try {
                clientMessage = reader.readLine();
                broadcastMessage(clientMessage);
            } catch (IOException e) {
                e.printStackTrace();
                closeEverything(socket, reader, writer);
                break;
            }
        }
    }

    public void broadcastMessage(String message) {
        for (ClientHandler handler : clients) {
            try {
                if (!handler.username.equals(username)) {
                    handler.writer.write(message);
                    handler.writer.newLine();
                    handler.writer.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, reader, writer);
            }

        }
    }

    public void removeClientHandler() {
        clients.remove(this);
        broadcastMessage("SERVER: " + username + " has left the chat");
    }

    public void closeEverything(Socket socket, BufferedReader reader, BufferedWriter writer) {
        removeClientHandler();
        try {
            if (reader != null)
                reader.close();
            if (writer != null)
                writer.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
