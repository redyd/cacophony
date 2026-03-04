package com.mapatiri.node.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class Tag {
    private String tag;
    private Set<String> subscribers;
}
