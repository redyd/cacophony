package com.mapatiri.node.model;

public record User(int saltLength, String bycrypt) {
}
