package org.bahmni.module.notification.connection;

import org.bahmni.module.notification.properties.NotificationProperties;

public class ConnectionDetails {
    private static String AUTH_URI;
    private static String OPENMRS_USER;
    private static String OPENMRS_PASSWORD;
    private static int OPENMRS_WEBCLIENT_CONNECT_TIMEOUT;
    private static int OPENMRS_WEBCLIENT_READ_TIMEOUT;

    public static org.bahmni.webclients.ConnectionDetails get() {
        return new org.bahmni.webclients.ConnectionDetails(
                AUTH_URI,
                OPENMRS_USER,
                OPENMRS_PASSWORD,
                OPENMRS_WEBCLIENT_CONNECT_TIMEOUT,
                OPENMRS_WEBCLIENT_READ_TIMEOUT);
    }

    public void setAuthUri(String authURINonstatic) {
        if (NotificationProperties.properties != null) {
            String uriProperty = NotificationProperties.properties.getProperty("openmrs.auth.uri");
            if (uriProperty != null && !uriProperty.isEmpty()) {
                ConnectionDetails.AUTH_URI = uriProperty;
                return;
            }
        }
        ConnectionDetails.AUTH_URI = authURINonstatic;
    }

    public void setOpenmrsUser(String openmrsUserNonstatic) {
        if (NotificationProperties.properties != null) {
            String openmrsUserProperty = NotificationProperties.getProperty("openmrs.user");
            if (openmrsUserProperty != null || !openmrsUserProperty.isEmpty()) {
                ConnectionDetails.OPENMRS_USER = openmrsUserProperty;
                return;
            }
        }
        ConnectionDetails.OPENMRS_USER = openmrsUserNonstatic;
    }

    public void setOpenmrsPassword(String openmrsPasswordNonstatic) {
        if (NotificationProperties.properties != null) {
            String openmrsPasswordProperty = NotificationProperties.getProperty("openmrs.password");
            if (openmrsPasswordProperty != null || !openmrsPasswordProperty.isEmpty()) {
                ConnectionDetails.OPENMRS_PASSWORD = openmrsPasswordProperty;
                return;
            }
        }
        ConnectionDetails.OPENMRS_PASSWORD = openmrsPasswordNonstatic;
    }

    public void setOpenmrsWebclientReadTimeout(int connectionTimeNonstatic) {
        if (NotificationProperties.properties != null) {
            String connectionTimeoutProperty = NotificationProperties.getProperty("openmrs.connectionTimeoutInMilliseconds");
            if (connectionTimeoutProperty != null || !connectionTimeoutProperty.isEmpty()) {
                ConnectionDetails.OPENMRS_WEBCLIENT_CONNECT_TIMEOUT = Integer.parseInt(connectionTimeoutProperty);
                return;
            }
        }
        ConnectionDetails.OPENMRS_WEBCLIENT_CONNECT_TIMEOUT = connectionTimeNonstatic;
    }

    public void setOpenmrsWebclientConnectTimeout(int replyTimeNonstatic) {
        if (NotificationProperties.properties != null) {
            String replyTimeNonstaticProperty = NotificationProperties.getProperty("openmrs.replyTimeoutInMilliseconds");
            if (replyTimeNonstaticProperty != null || !replyTimeNonstaticProperty.isEmpty()) {
                ConnectionDetails.OPENMRS_WEBCLIENT_READ_TIMEOUT = Integer.parseInt(replyTimeNonstaticProperty);
                return;
            }
        }
        ConnectionDetails.OPENMRS_WEBCLIENT_READ_TIMEOUT = replyTimeNonstatic;
    }
}
