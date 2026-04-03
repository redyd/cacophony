package com.mapatiri.speaker;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    @Test
    void resolveResourceReturnsExpectedPath() throws Exception {
        Method method = App.class.getDeclaredMethod("resolveResource", String.class);
        method.setAccessible(true);

        Path result = (Path) method.invoke(null, "speaker-config.json");

        assertEquals(Path.of("src", "main", "resources", "speaker-config.json"), result);
    }
}
