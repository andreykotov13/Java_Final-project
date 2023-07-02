package searchengine.services;

import org.jsoup.Connection;

public interface NetworkService {
    Connection.Response getResponse(String url);

    boolean checkSiteConnection(String url);

    boolean isAvailable(Connection.Response response);
}
