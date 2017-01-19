package com.jomofisher.cmakeify;

import com.google.common.io.Files;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static com.google.common.truth.Truth.assertThat;

public class TestCmakeify {

    private static String main(String... args) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        new Cmakeify(ps).go(args);
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    @Test
    public void testVersion() throws IOException {
        assertThat(main("--version")).contains(BuildInfo.PROJECT_VERSION);
    }

    @Test
    public void missingConfigurationFile() throws IOException {
        assertThat(main()).contains("configuration file");
    }

    @Test
    public void workingFolderFlag() throws IOException {
        assertThat(main("--working-folder", "non-existing-blah")).contains("non-existing-blah");
    }

    @Test
    public void wfFlag() throws IOException {
        assertThat(main("-wf", "non-existing-blah")).contains("non-existing-blah");
    }

    @Test
    public void simpleConfiguration() throws IOException {
        File yaml = new File("test-files/simpleConfiguration/.cmakeify.yml");
        yaml.getParentFile().mkdirs();
        Files.write("target: [windows, linux, android]\n" +
                "compiler: [gcc, clang]\n" +
                "gcc:\n" +
                "  versions: [4.9.0, 6.3.0]\n",
                yaml, StandardCharsets.UTF_8);
        String result = main("-wf", yaml.getParent(), "--dump");
        assertThat(result).contains("target: windows, linux, android");
        assertThat(result).contains("compiler: gcc, clang");
        assertThat(result).contains("gcc:");
        assertThat(result).contains("  versions: '4.9.0', '6.3.0'");
    }
}