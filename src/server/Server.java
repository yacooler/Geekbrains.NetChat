package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {




    public static final int PORT = 8082;


    private AuthService authService;
    private Set<ClientHandler> clientHandlers;
    private UserDBService userDBService;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public Server() {
        this(PORT);
    }

    public Server(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            initSQLServer();
            initDatabase();

            authService = new AuthServiceDBImpl(DataBaseConnection.getConnection());
            System.out.println("Auth is started up");

            clientHandlers = new HashSet<>();

            while (true) {
                System.out.println("Waiting for a connection...");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket);
                new ClientHandler(this, socket, executorService);
            }
        }

        catch (ClassNotFoundException | SQLException | IOException exception){
            //Все эксепшены в кучу, обрабатывать я их всё равно пока не умею
            exception.printStackTrace();
        }

        finally {
            //Закрываем коннект
            DataBaseConnection.closeConnection();
            executorService.shutdown();
        }
    }


    public AuthService getAuthService() {
        System.out.println(authService);
        return authService;
    }

    public synchronized boolean isOccupied(AuthService.Record record) {
        for (ClientHandler ch : clientHandlers) {
            if (ch.getRecord().equals(record)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void subscribe(ClientHandler ch) {
        clientHandlers.add(ch);
    }

    public synchronized void unsubscribe(ClientHandler ch) {
        clientHandlers.remove(ch);
    }

    /**
     *Сообщение от ClientHandlerа
     */
    public synchronized void handleMessage(ChatMessage message) throws SQLException, IOException {

        switch (message.getContent()){
            case ChatMessage.CONT_MESSAGE, ChatMessage.CONT_SERVER_MESSAGE:{
                broadcastMessage(message);
                break;
            }
            case ChatMessage.CONT_WISP_MESSAGE, ChatMessage.CONT_ERROR:{
                sendPrivateMessage(message);
                break;
            }
            case ChatMessage.CONT_RENAME:{
                renameClient(message);
                break;
            }

        }

    }


    private void broadcastMessage(ChatMessage message) throws IOException{
        for (ClientHandler ch : clientHandlers){
            ch.sendChatMessageToClient(message);
        }
    }

    private void sendPrivateMessage(ChatMessage message)  throws IOException{
        ClientHandler sender = getClientHandlerByName(message.getSender());
        sender.sendChatMessageToClient(message);

        ClientHandler recipient = getClientHandlerByName(message.getRecipient());

        if (recipient != null) {
            recipient.sendChatMessageToClient(message);
        } else {
            //Если адресата не существует - отправим сообщение отправителю
            sender.sendChatMessageToClient(new ChatMessage(ChatMessage.CONT_ERROR, String.format("Unknown username:%s", message.getRecipient())));
        }
    }

    /**
     * Переименование клиента
     */
    private synchronized void renameClient(ChatMessage message) throws SQLException, IOException {
        ClientHandler sender = getClientHandlerByName(message.getSender());
        if (sender == null){
            throw new NullPointerException("Не получилось найти пользователя для переименования!");
        }

        //Проверка, свободен ли ник
        String checkName = userDBService.getUserByName(message.getMessage()).getName();

        //Уже занят
        if (checkName != null){
            sender.sendChatMessageToClient(new ChatMessage(ChatMessage.CONT_ERROR, String.format("Name %s is already occupied", message.getMessage())));
            return;
        }
        String oldName = sender.getRecord().getName();
        userDBService.setUserNameById(message.getMessage(), sender.getRecord().getId());
        sender.sendChatMessageToClient(new ChatMessage(ChatMessage.CONT_RENAME_DONE, message.getMessage()));
        sender.getRecord().setName(message.getMessage());
        broadcastMessage(new ChatMessage(ChatMessage.CONT_SERVER_MESSAGE, String.format("%s now known as %s", oldName, sender.getRecord().getName())));
    }

    private ClientHandler getClientHandlerByName(String name){
        for (ClientHandler ch : clientHandlers){
            if (ch.getRecord().getName().toLowerCase().equals(name.toLowerCase())){
                return ch;
            }
        }
        return null;
    }

    public void initSQLServer() throws SQLException, ClassNotFoundException {
        userDBService = new UserDBService(DataBaseConnection.getConnection());
    }

    /**
     * Создаем таблицы в базе данных и наполняем юзерами, если их не было
     */
    public void initDatabase() throws SQLException{
        userDBService.initDatabase();
    }
}
