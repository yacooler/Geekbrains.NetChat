package client;

import java.io.Closeable;
import java.io.IOException;

public interface ClientHistoryService extends Closeable {
    public String loadHistoryRow() throws IOException;
    public void saveHistoryRow(String row) throws IOException;
    public boolean prepare(String login) throws IOException;
    //close придет из Closeable
}
