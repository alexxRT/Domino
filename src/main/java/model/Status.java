package model;

public enum Status {
    OK, // request sutisfied
    AGAIN, // error on client side when requested
    ERROR, // error on server side while processing
    DRAW, // statuses that indicates game end
    VICTORY,
    DEFEAT
}

