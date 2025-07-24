package edu.ohsu.cmp.fhirproxy.model;

public class ClientInfo {
    private String clientId;
    private String serverUrl;
    private String bearerToken;
    private String patientId;
    private String userId;

    public ClientInfo(String clientId, String serverUrl, String bearerToken, String patientId, String userId) {
        this.clientId = clientId;
        this.serverUrl = serverUrl;
        this.bearerToken = bearerToken;
        this.patientId = patientId;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "ClientInfo{" +
                "clientId='" + clientId + '\'' +
                ", serverUrl='" + serverUrl + '\'' +
                ", bearerToken='" + bearerToken + '\'' +
                ", patientId='" + patientId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    public String getClientId() {
        return clientId;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getUserId() {
        return userId;
    }
}
