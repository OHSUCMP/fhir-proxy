package edu.ohsu.cmp.fhirproxy.model;

import java.util.Date;

public class Registration {
    private String accessToken;
    private Date expires;

    public Registration(String accessToken, Date expires) {
        this.accessToken = accessToken;
        this.expires = expires;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }
}
