package model;

public class User {


    private int saltLenght;
    private String bycrypt;

    public User(int saltLenght, String bycrypt) {
        this.saltLenght = saltLenght;
        this.bycrypt = bycrypt;
    }



}
