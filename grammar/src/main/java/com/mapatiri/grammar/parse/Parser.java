package com.mapatiri.grammar.parse;

import java.util.function.Function;
import java.util.regex.Matcher;

public record Parser<T extends ParsedCommand>(String key, Function<Matcher, T> factory)  {
}
