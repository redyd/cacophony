package com.mapatiri.grammar.parse;

public record Post (String id, String sender , String receiver, String content) implements ParsedCommand {
}
