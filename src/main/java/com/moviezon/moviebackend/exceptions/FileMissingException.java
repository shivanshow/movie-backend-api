package com.moviezon.moviebackend.exceptions;

public class FileMissingException extends RuntimeException {
    public FileMissingException(String message) {
        super(message);
    }
}
