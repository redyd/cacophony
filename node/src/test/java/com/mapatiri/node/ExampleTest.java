package com.mapatiri.node;

import com.mapatiri.grammar.Example;
import org.junit.jupiter.api.Test;

public class ExampleTest {

    @Test
    public void test() {
        System.out.println("test");
    }

    @Test
    public void testGrammar() {
        System.out.println(Example.helloFromGrammar());
    }

}
