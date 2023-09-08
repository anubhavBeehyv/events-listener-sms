package org.bahmni.module.notification.connection;


import org.bahmni.webclients.ClientCookies;
import org.bahmni.webclients.HttpClient;

import java.net.URI;
import java.net.URISyntaxException;

public class OpenmrsLogin {
    private ClientCookies cookies;

    public void getConnection() {
        HttpClient authenticatedWebClient = WebClientFactory.getClient();
        org.bahmni.webclients.ConnectionDetails connectionDetails = ConnectionDetails.get();
        String authUri = connectionDetails.getAuthUrl();
        getCookiesAfterConnection(authenticatedWebClient, authUri);
    }

    public void getCookiesAfterConnection(HttpClient authenticatedWebClient, String urlString) {
        try {
            this.cookies= authenticatedWebClient.getCookies(new URI(urlString));
        } catch (URISyntaxException e) {
            throw new RuntimeException("Is not a valid URI - " + urlString);
        }
    }

    public ClientCookies getCookies(){
        return cookies;
    }


}
