package it.davidepalladino.airanalyzer.model;

public class Signup {
    private String id;
    private String username;
    private String password;
    private String email;
    private String name;
    private String surname;
    private String question1;
    private String question2;
    private String question3;
    private String answer1;
    private String answer2;
    private String answer3;

    public Signup(String id, String username, String password, String email, String name, String surname, String question1, String question2, String question3, String answer1, String answer2, String answer3) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.question1 = question1;
        this.question2 = question2;
        this.question3 = question3;
        this.answer1 = answer1;
        this.answer2 = answer2;
        this.answer3 = answer3;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public class NoResponse {
        public NoResponse() {
        }
    }
}
