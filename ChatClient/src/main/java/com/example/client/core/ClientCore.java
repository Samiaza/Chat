package com.example.client.core;

import com.example.client.models.MessageFromServer;
import com.example.client.models.MessageToServer;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;

public class ClientCore extends Thread {
    private final Socket clientSocket;
    private final BufferedReader consoleReader;
    private final BufferedReader socketReader;
    private final PrintWriter socketWriter;
    private final Gson gson;
    private boolean isConnected;
    private Long attachedId;
    private Long attachedRoomId;

    public ClientCore(int port) {
        try {
            clientSocket = new Socket("localhost", port);
            consoleReader = new BufferedReader(new InputStreamReader(System.in));
            socketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            socketWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())),
                    true);
            gson = new Gson();
            isConnected = true;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        try {
            try {
                Thread receiver = new Thread(() -> {
                    try {
                        while (isConnected && clientSocket.isConnected()) {
                            handleReceived(socketReader.readLine());
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });

                Thread sender = new Thread(() -> {
                    try {
                        while (isConnected && clientSocket.isConnected()) {
                            handleSent(consoleReader.readLine());
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });

                receiver.start();
                sender.start();
                receiver.join();
                sender.join();
            } finally {
                clientSocket.close();
                consoleReader.close();
                socketReader.close();
                socketWriter.close();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    private void handleReceived(String str) {
        if (str == null) {
            isConnected = false;
            return;
        }
        MessageFromServer incomeMessageContainer = gson.fromJson(str, MessageFromServer.class);
        if (incomeMessageContainer.author.equals("server")) {
            if (incomeMessageContainer.message != null) {
                System.out.println(incomeMessageContainer.message);
            }
            if (incomeMessageContainer.menuItems != null) {
                incomeMessageContainer.menuItems.forEach((k, v) -> System.out.format("%s. %s\n", k, v));
            }
            if (incomeMessageContainer.messagesHistory != null) {
                incomeMessageContainer.messagesHistory.forEach(m -> System.out.format("%s: %s\n", m.author, m.message));
            }
            attachedId = incomeMessageContainer.attachedId;
            attachedRoomId = incomeMessageContainer.attacheRoomId;
        } else {
            System.out.format("%s: %s\n", incomeMessageContainer.author, incomeMessageContainer.message);
        }
    }

    private void handleSent(String str) {
        if (!isConnected){
            System.out.println("Press enter to exit...");
            return;
        }
        MessageToServer outcomeMessageContainer = new MessageToServer();
        outcomeMessageContainer.author = attachedId;
        outcomeMessageContainer.room = attachedRoomId;
        outcomeMessageContainer.message = str;
        socketWriter.println(gson.toJson(outcomeMessageContainer));
    }
}
