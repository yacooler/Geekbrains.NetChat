package client;

import java.io.*;

public class ClientHistoryServiceFileImpl implements ClientHistoryService {
    private File fileHistory;
    FileWriter fileWriter;
    FileReader fileReader;
    BufferedReader bufferedReader;

    @Override
    public boolean prepare(String login) throws IOException {
        fileHistory = new File(String.format("res\\%snetChatHistory.txt", login));
        return fileHistory.exists();
    }


    @Override
    public String loadHistoryRow() throws IOException{
        if (fileReader == null) {
            fileReader = new FileReader(fileHistory);
            bufferedReader = new BufferedReader(fileReader);
        }
        return bufferedReader.readLine();
    }

    @Override
    public void saveHistoryRow(String row) throws IOException {
        if (fileWriter == null) {
            fileWriter = new FileWriter(fileHistory, true);
        }
        fileWriter.write(row);
        fileWriter.flush();
    }


    @Override
    public void close() throws IOException {
        if (fileWriter != null) {
            fileWriter.close();
        }
        if (fileReader != null){
            fileReader.close();
        }
    }

}
