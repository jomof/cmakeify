package com.jomofisher.cmakeify;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static com.google.common.truth.Truth.assertThat;

public class TestCmakeify {

    private static String main(String... args) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        new Cmakeify(ps).go(new String[0]);
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    @Test
    public void testVersion() throws IOException {
        assertThat(main("--version")).contains(BuildInfo.PROJECT_VERSION);
    }
}