package com.mapatiri.node.model;

//parametre bcryptSalt pour le message challenge
public record BcryptParams(int rotationNbr, String saltLenght) { }
