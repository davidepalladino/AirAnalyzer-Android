package it.davidepalladino.airanalyzer.model;

import java.io.Serializable;

public class Login implements Serializable {
    private String username;
    private String password;
    private String isActive;

    public Login() {
        this.isActive = "1";
    }

    public Login(String username, String password) {
        this.username = username;
        this.password = password;
        this.isActive = "1";
    }

    public String getUsername() {
        return username;
    }



    public class Response {
        private String token;

        public Response(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }
}