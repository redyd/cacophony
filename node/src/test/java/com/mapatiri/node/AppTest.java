package com.mapatiri.node;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppTest {

    @Test
    void mainThrowsWhenMissingArguments() {
        assertThrows(IllegalArgumentException.class, () -> App.main(new String[0]));
    }

    @Test
    void resolveResourceReturnsExpectedPath() throws Exception {
        Method method = App.class.getDeclaredMethod("resolveResource", String.class);
        method.setAccessible(true);

        Path result = (Path) method.invoke(null, "node-config.json");

        assertEquals(Path.of("src", "main", "resources", "node-config.json"), result);
    }
}
