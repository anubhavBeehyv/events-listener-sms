package org.bahmni.module.notification.connection;

import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpClient;
import org.bahmni.webclients.openmrs.OpenMRSLoginAuthenticator;

public class WebClientFactory {

    public static HttpClient getClient() {
        ConnectionDetails connectionDetails = org.bahmni.module.notification.connection.ConnectionDetails.get();
        return new HttpClient(connectionDetails, getAuthenticator(connectionDetails));
    }


    private static OpenMRSLoginAuthenticator getAuthenticator(ConnectionDetails connectionDetails) {
        return new OpenMRSLoginAuthenticator(connectionDetails);

    }
}
