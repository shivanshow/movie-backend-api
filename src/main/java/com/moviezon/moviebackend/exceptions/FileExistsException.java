package com.moviezon.moviebackend.exceptions;

public class FileExistsException extends RuntimeException {

    public FileExistsException(String message) {
        super(message);
    }
}
