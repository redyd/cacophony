package com.mapatiri.node.model;

public record User(String login, String bcryptHash, int bcryptRound, String bcryptSalt) {
}
