package com.example.server.core;

import com.example.server.exceptions.EntityAlreadyExistsException;
import com.example.server.exceptions.EntityNotFoundException;
import com.example.server.services.ChatroomsService;
import com.example.server.services.MessagesService;
import com.example.server.services.UsersService;
import com.google.gson.Gson;
import com.example.server.config.SocketsApplicationConfig;
import com.example.server.models.entities.Chatroom;
import com.example.server.models.entities.Message;
import com.example.server.models.messages.ClientStatus;
import com.example.server.models.messages.MessageFromServer;
import com.example.server.models.messages.MessageToServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

public class ServerCore extends Thread {
    static final Map<Integer, String> AUTHORITY_MENU_MAP = new HashMap<>();

    static {
        AUTHORITY_MENU_MAP.put(1, "Sign In");
        AUTHORITY_MENU_MAP.put(2, "Sign Up");
        AUTHORITY_MENU_MAP.put(3, "Exit");
    }

    static final Map<Integer, String> ROOMS_MENU_MAP = new HashMap<>();

    static {
        ROOMS_MENU_MAP.put(1, "Create room");
        ROOMS_MENU_MAP.put(2, "Choose room");
        ROOMS_MENU_MAP.put(3, "Exit");
    }

    static final String name = "server";
    static final List<ServerEntity> serverEntities = new ArrayList<>();
    static final BlockingDeque<ServerEntity> toHandleClientMessagesDeque = new LinkedBlockingDeque<>();
    private final ServerSocket serverSocket;
    private final BufferedReader consoleReader;
    private final Gson gson;
    static Boolean isServerEnabled;
    static UsersService usersService = null;
    static ChatroomsService chatroomsService = null;
    static MessagesService messagesService = null;
    Thread commandScanner;
    Thread newConnectionsHandler;
    Thread messagesDequeHandler;

    public ServerCore(int port) {
        try {
            ApplicationContext context = new AnnotationConfigApplicationContext(SocketsApplicationConfig.class);
            usersService = context.getBean("usersService", UsersService.class);
            chatroomsService = context.getBean("chatroomsService", ChatroomsService.class);
            messagesService = context.getBean("messagesService", MessagesService.class);
            if (usersService == null || chatroomsService == null) {
                throw new RuntimeException("One or more services is not available");
            }
            serverSocket = new ServerSocket(port);
            consoleReader = new BufferedReader(new InputStreamReader(System.in));
            gson = new Gson();
            isServerEnabled = true;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        try {
            try {
                commandScanner = new Thread(() -> {
                    try {
                        while (isServerEnabled) {
                            String command = consoleReader.readLine();
                            if (command.equalsIgnoreCase("exit")) {
                                isServerEnabled = false;
                                serverEntities.forEach(e -> {
                                    try {
                                        e.socketReader.close();
                                        e.socketWriter.close();
                                        e.socket.close();
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                });
                                serverEntities.forEach(e -> {
                                    try {
                                        e.socketReader.close();
                                        e.socketWriter.close();
                                        e.socket.close();
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                });
                                serverSocket.close();
                            }
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });

                newConnectionsHandler = new Thread(() -> {
                    try {
                        while (isServerEnabled) {
                            Socket clientSocket = serverSocket.accept();
                            ServerCore.serverEntities.add(new ServerEntity(clientSocket));
                        }
                    } catch (SocketException ex) {
                        System.out.println("Sockets closing");
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });

                messagesDequeHandler = new Thread(() -> {
                    try {
                        while (isServerEnabled) {
                            while (!toHandleClientMessagesDeque.isEmpty()) {
                                ServerEntity handledserverEntity = toHandleClientMessagesDeque.poll();
                                MessageToServer handledMessage = handledserverEntity.messageToHandle;

                                if (handledserverEntity.status == ClientStatus.JOINED_TO_SERVER) {
                                    int chosenItem = 0;
                                    try {
                                        chosenItem = Integer.parseInt(handledMessage.message);
                                    } catch (Exception ignored) {
                                    }
                                    if (AUTHORITY_MENU_MAP.get(chosenItem) == null) {
                                        assert true;
                                    } else if (AUTHORITY_MENU_MAP.get(chosenItem).equalsIgnoreCase("Sign In")) {
                                        handledserverEntity.status = ClientStatus.ENTERING_EXISTS_LOGIN;
                                    } else if (AUTHORITY_MENU_MAP.get(chosenItem).equalsIgnoreCase("Sign Up")) {
                                        handledserverEntity.status = ClientStatus.ENTERING_NEW_LOGIN;
                                    } else if (AUTHORITY_MENU_MAP.get(chosenItem).equalsIgnoreCase("Exit")) {
                                        handledserverEntity.status = ClientStatus.OUT_OF_SERVER;
                                    }
                                } else if (handledserverEntity.status == ClientStatus.ENTERING_EXISTS_LOGIN) {
                                    handledserverEntity.login = handledMessage.message;
                                    handledserverEntity.status = ClientStatus.ENTERING_EXISTS_PASSWORD;
                                } else if (handledserverEntity.status == ClientStatus.ENTERING_EXISTS_PASSWORD) {
                                    MessageFromServer messageContainer = new MessageFromServer();
                                    messageContainer.author = ServerCore.name;
                                    try {
                                        if (usersService.signIn(handledserverEntity.login, handledMessage.message)) {
                                            handledserverEntity.status = ClientStatus.LOGGED_ON;
                                            messageContainer.message = "Login successfully";
                                            messageContainer.attachedId = handledserverEntity.attachedId =
                                                    usersService.getIdByLogin(handledserverEntity.login);
                                        } else {
                                            handledserverEntity.status = ClientStatus.JOINED_TO_SERVER;
                                            messageContainer.message = "Wrong password";
                                        }
                                    } catch (EntityNotFoundException ex) {
                                        messageContainer.message = ex.getMessage();
                                        handledserverEntity.status = ClientStatus.JOINED_TO_SERVER;
                                    }
                                    handledserverEntity.send(gson.toJson(messageContainer));
                                } else if (handledserverEntity.status == ClientStatus.ENTERING_NEW_LOGIN) {
                                    handledserverEntity.login = handledMessage.message;
                                    handledserverEntity.status = ClientStatus.ENTERING_NEW_PASSWORD;
                                } else if (handledserverEntity.status == ClientStatus.ENTERING_NEW_PASSWORD) {
                                    MessageFromServer messageContainer = new MessageFromServer();
                                    messageContainer.author = ServerCore.name;
                                    try {
                                        usersService.signUp(handledserverEntity.login, handledMessage.message);
                                        messageContainer.message = "New user successfully created";
                                    } catch (EntityAlreadyExistsException ex) {
                                        messageContainer.message = ex.getMessage();
                                    }
                                    handledserverEntity.send(gson.toJson(messageContainer));
                                    handledserverEntity.status = ClientStatus.JOINED_TO_SERVER;
                                } else if (handledserverEntity.status == ClientStatus.LOGGED_ON) {
                                    int chosenItem = 0;
                                    try {
                                        chosenItem = Integer.parseInt(handledMessage.message);
                                    } catch (Exception ignored) {
                                    }
                                    if (ROOMS_MENU_MAP.get(chosenItem) == null) {
                                        assert true;
                                    } else if (ROOMS_MENU_MAP.get(chosenItem).equalsIgnoreCase("Create room")) {
                                        handledserverEntity.status = ClientStatus.CREATING_ROOM;
                                    } else if (ROOMS_MENU_MAP.get(chosenItem).equalsIgnoreCase("Choose room")) {
                                        handledserverEntity.status = ClientStatus.CHOOSING_ROOM;
                                    } else if (ROOMS_MENU_MAP.get(chosenItem).equalsIgnoreCase("Exit")) {
                                        handledserverEntity.status = ClientStatus.JOINED_TO_SERVER;
                                    }
                                } else if (handledserverEntity.status == ClientStatus.CREATING_ROOM) {
                                    chatroomsService.save(
                                            new Chatroom(null, handledMessage.message, handledserverEntity.attachedId, null, null));
                                    handledserverEntity.status = ClientStatus.LOGGED_ON;
                                } else if (handledserverEntity.status == ClientStatus.CHOOSING_ROOM) {
                                    try {
                                        int chosenItem = Integer.parseInt(handledMessage.message);
                                        if (chosenItem == handledserverEntity.availableRooms.size() + 1) {
                                            handledserverEntity.status = ClientStatus.LOGGED_ON;
                                        } else {
                                            Chatroom roomToJoin = handledserverEntity.availableRooms.get(chosenItem - 1);
                                            handledserverEntity.attachedRoomId = roomToJoin.getId();
                                            MessageFromServer messageContainer = new MessageFromServer();
                                            messageContainer.author = ServerCore.name;
                                            messageContainer.attacheRoomId = handledserverEntity.attachedRoomId;
                                            messageContainer.message = "You are joined to room " + roomToJoin.getName();
                                            handledserverEntity.send(gson.toJson(messageContainer));
                                            handledserverEntity.status = ClientStatus.WAITING_FOR_JOIN_TO_ROOM;
                                        }
                                    } catch (Exception ex) {
                                        System.out.println("try again!");
                                    }
                                } else if (handledserverEntity.status == ClientStatus.JOINED_TO_ROOM) {
                                    if (handledMessage.message.equalsIgnoreCase("exit")) {
                                        handledserverEntity.status = ClientStatus.LOGGED_ON;
                                    } else {
                                        MessageFromServer messageContainer = new MessageFromServer();
                                        messageContainer.author = handledserverEntity.login;
                                        messageContainer.message = handledMessage.message;
                                        messagesService.save(new Message(null, handledserverEntity.attachedId, handledserverEntity.attachedRoomId, handledMessage.message, null));
                                        for (ServerEntity serverEntity : serverEntities) {
                                            if ((serverEntity.status == ClientStatus.JOINED_TO_ROOM) &&
                                                    (serverEntity.attachedRoomId.equals(handledserverEntity.attachedRoomId))) {
                                                serverEntity.send(gson.toJson(messageContainer));
                                            }

                                        }
                                        System.out.println(gson.toJson(messageContainer));
                                    }
                                }
                                handledserverEntity.makeServerServiceReaction();
                            }
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });

                commandScanner.start();
                newConnectionsHandler.start();
                messagesDequeHandler.start();

                commandScanner.join();
                newConnectionsHandler.join();
                messagesDequeHandler.join();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                serverEntities.forEach(e -> {
                    try {
                        e.socketReader.close();
                        e.socketWriter.close();
                        e.socket.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                serverSocket.close();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

class ServerEntity extends Thread {
    final Socket socket;
    MessageToServer messageToHandle;
    List<Chatroom> availableRooms;
    String login;
    Long attachedId;
    Long attachedRoomId;
    ClientStatus status;
    final BufferedReader socketReader;
    final PrintWriter socketWriter;
    private final Gson gson;

    public ServerEntity(Socket socket) {
        try {
            this.socket = socket;
            socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            gson = new Gson();
            status = ClientStatus.JOINED_TO_SERVER;
            makeServerServiceReaction();
            start();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        try {
            while (ServerCore.isServerEnabled) {
                String received = null;
                if (!socket.isClosed()) {
                    received = socketReader.readLine();
                }
                if (received != null) {
                    messageToHandle = gson.fromJson(received, MessageToServer.class);
                    System.out.println("Joined client : " + messageToHandle.message);
                    System.out.println("Current status : " + status);
                    ServerCore.toHandleClientMessagesDeque.addLast(this);
                } else {
                    status = ClientStatus.OUT_OF_SERVER;
                    socketReader.close();
                    socketWriter.close();
                    socket.close();
                }
            }
            socketReader.close();
            socketWriter.close();
            socket.close();
        } catch (Exception ex) {
            System.out.println("Socket closed");
        }
    }

    void makeServerServiceReaction() {
        MessageFromServer messageContainer = new MessageFromServer();
        if (status == ClientStatus.OUT_OF_SERVER) {
            socketWriter.close();
            try {
                socketReader.close();
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (status == ClientStatus.JOINED_TO_SERVER) {
            messageContainer.author = ServerCore.name;
            messageContainer.menuItems = ServerCore.AUTHORITY_MENU_MAP;
            messageContainer.message = "Hello from Server!";
        } else if (status == ClientStatus.ENTERING_EXISTS_LOGIN || status == ClientStatus.ENTERING_NEW_LOGIN) {
            messageContainer.author = ServerCore.name;
            messageContainer.message = "Enter username:";
        } else if (status == ClientStatus.ENTERING_EXISTS_PASSWORD || status == ClientStatus.ENTERING_NEW_PASSWORD) {
            messageContainer.author = ServerCore.name;
            messageContainer.message = "Enter password";
        } else if (status == ClientStatus.LOGGED_ON) {
            messageContainer.author = ServerCore.name;
            messageContainer.menuItems = ServerCore.ROOMS_MENU_MAP;
            messageContainer.message = "Select action";
        } else if (status == ClientStatus.CREATING_ROOM) {
            messageContainer.author = ServerCore.name;
            messageContainer.message = "Enter room name:";
        } else if (status == ClientStatus.CHOOSING_ROOM) {
            messageContainer.author = ServerCore.name;
            messageContainer.menuItems = new HashMap<>();
            availableRooms = ServerCore.chatroomsService.getAll();
            System.out.println(availableRooms);
            for (int i = 0; i < availableRooms.size(); i++) {
                messageContainer.menuItems.put(i + 1, availableRooms.get(i).getName());
            }
            messageContainer.menuItems.put(availableRooms.size() + 1, "Exit");
            messageContainer.message = "Rooms:";
        } else if (status == ClientStatus.WAITING_FOR_JOIN_TO_ROOM) {
            messageContainer.author = ServerCore.name;
            List<Message> roomLastMessagesList = ServerCore.messagesService.getLastMessages(attachedRoomId, 30L);
            messageContainer.messagesHistory = roomLastMessagesList.stream().map(m -> {
                MessageFromServer message = new MessageFromServer();
                message.author = ServerCore.usersService.getLoginById(m.getAuthor());
                message.message = m.getText();
                return message;
            }).collect(Collectors.toList());
            messageContainer.message = "Message history of room " +
                    ServerCore.usersService.getLoginById(ServerCore.chatroomsService.getCreatorIdByRoomId(attachedRoomId)) + ":";
            status = ClientStatus.JOINED_TO_ROOM;
        }
        if (messageContainer.author != null) {
            send(gson.toJson(messageContainer));
        }

    }

    public void send(String messageContainer) {
        socketWriter.println(messageContainer);
    }
}
