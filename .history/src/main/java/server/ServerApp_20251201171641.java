package server;

import common.Payload;
import server.controller.ServerController;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        System.out.println("Starting Homestay Server on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private ServerController controller;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.controller = new ServerController();
        }

        @Override
        public void run() {
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                while (true) {
                    try {
                        Object requestObj = in.readObject();
                        if (requestObj instanceof Payload) {
                            Payload request = (Payload) requestObj;
                            System.out.println("Received request: " + request.getAction());

                            Payload response = controller.handleRequest(request);
                            out.writeObject(response);
                            out.flush();

                            if (request.getAction() == Payload.Action.LOGOUT) {
                                break;
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + socket.getInetAddress());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
