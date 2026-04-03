package com.mapatiri.grammar.parse;

public sealed interface ParsedCommand permits Msg, Post, Send, Subscribe, Leave, Challenge, ErrorProtocole, Response, UserAction, Welcome, Onboarding,Ok, Advert
        {
}
