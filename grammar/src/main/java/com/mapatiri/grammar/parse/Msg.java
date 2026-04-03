package com.mapatiri.grammar.parse;

public record Msg(String sender, String content) implements ParsedCommand {
}
