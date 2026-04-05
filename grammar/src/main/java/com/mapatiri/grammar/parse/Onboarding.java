package com.mapatiri.grammar.parse;

public record Onboarding(String user, String saltLength, String bcryptHash) implements ParsedCommand {
}
