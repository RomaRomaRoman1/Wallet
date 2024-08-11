package org.example.exception;

public class AlreadyExistWIthThisEmail extends Exception{
    public AlreadyExistWIthThisEmail(String email) {
        super("Account with email: " + email + " already exist!");
    }
}
