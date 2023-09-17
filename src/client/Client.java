package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String username;

    public static void main(String[] args) throws UnknownHostException, IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your username: ");
        String username = sc.nextLine();

        Socket socket = new Socket("localhost", 1234);
        Client client = new Client(socket, username);
        client.receiveMessage();
        client.sendMessage();
    }

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    public void sendMessage() {
        try {
            writer.write(username);
            writer.newLine();
            writer.flush();

            Scanner sc = new Scanner(System.in);

            while (socket.isConnected()) {
                String message = sc.nextLine();
                writer.write(username + ": " + message);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    public void receiveMessage() {
        new Thread(() -> {
            String messageReceived;

            while (socket.isConnected()) {
                try {
                    messageReceived = reader.readLine();
                    System.out.println(messageReceived);
                } catch (IOException e) {
                    closeEverything(socket, reader, writer);
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader reader, BufferedWriter writer) {
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
