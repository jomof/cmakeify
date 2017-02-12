package com.jomofisher.cmakeify;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.jomofisher.cmakeify.model.Configuration;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static com.google.common.truth.Truth.assertThat;

public class TestCmakeify {

    private static String main(String... args) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        new CMakeify(ps).go(args);
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    @Test
    public void testVersion() throws IOException {
        assertThat(main("--version")).contains(BuildInfo.PROJECT_VERSION);
    }

    @Test
    public void missingConfigurationFile() throws IOException {
        new File("test-files/empty-folder").mkdirs();
        assertThat(main("-wf", "test-files/empty-folder")).contains("configuration file");
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
    public void testScript() throws IOException {
        File yaml = new File("test-files/simpleConfiguration/cmakeify.yml");
        yaml.getParentFile().mkdirs();
        Files.write("includes: [extra-includes]\n" +
                        "android:\n" +
                        "  lib: libbob.a\n" +
                        "  ndk:\n" +
                        "    platforms: [21, 22]\n",
            yaml, StandardCharsets.UTF_8);
        main("-wf", yaml.getParent(),
            "--host", "Linux",
            "--group-id", "my-group-id",
            "--artifact-id", "my-artifact-id",
            "--target-version", "my-target-version",
            "--cmake-flags", "-DBOOST_ROOT=boost/");
        File scriptFile = new File(".cmakeify/build.sh");
        String script = Joiner.on("\n").join(Files.readLines(scriptFile, Charsets.UTF_8));
        assertThat(script).contains("cmake-3.7.2-Linux-x86_64.tar.gz");
        assertThat(script).contains("groupId: my-group-id");
        assertThat(script).contains("artifactId: my-artifact-id");
        assertThat(script).contains("version: my-target-version");
        assertThat(script).contains("BOOST_ROOT=");
    }

    @Test
    public void weirdHost() throws IOException {
        File yaml = new File("test-files/simpleConfiguration/cmakeify.yml");
        Files.write("",
            yaml, StandardCharsets.UTF_8);
        assertThat(main("-wf", yaml.getParent(), "--host", "Joebob"))
                .contains("Joebob");
    }

    @Test
    public void dumpIsSelfHost() throws IOException {
        Configuration config = new Configuration();
        System.out.printf(new Yaml().dump(config));
        File yaml = new File("test-files/simpleConfiguration/cmakeify.yml");
        yaml.getParentFile().mkdirs();
        Files.write("", yaml, StandardCharsets.UTF_8);
        String result1 = main("-wf", yaml.getParent(), "--dump");
        yaml.delete();
        Files.write(result1, yaml, StandardCharsets.UTF_8);
        System.out.print(result1);
        String result2 = main("-wf", yaml.getParent(), "--dump");
        assertThat(result2).isEqualTo(result1);


    }
}