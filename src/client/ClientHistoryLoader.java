package client;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayDeque;

public class ClientHistoryLoader {
    private String login;
    private JTextArea chatTextArea;


    private ClientHistoryService clientHistory;

    public ClientHistoryLoader(ClientHistoryService clientHistory, JTextArea chatTextArea, String login){
        this.clientHistory = clientHistory;
        this.login = login;
        this.chatTextArea = chatTextArea;
        try {
            if (!clientHistory.prepare(login)) {
                return;
            };
            String line;
            //Используем очередь
            ArrayDeque<String> queue= new ArrayDeque<>();
            while ((line = clientHistory.loadHistoryRow()) != null) {
                queue.addLast(line);
                if (queue.size() > 100) {
                    queue.removeFirst();
                }
            }

            while(!queue.isEmpty()) {
                chatTextArea.append(queue.pollFirst());
                chatTextArea.append("\n");
            }
        }
        catch(IOException e){
            throw new RuntimeException("Ошибка загрузки истории из чата", e);
        }
    }



}
