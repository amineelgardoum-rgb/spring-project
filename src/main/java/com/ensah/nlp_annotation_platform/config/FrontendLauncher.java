package com.ensah.nlp_annotation_platform.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;

@Component
public class FrontendLauncher implements CommandLineRunner {

    private Process frontendProcess;

    @Override
    public void run(String... args) throws Exception {
        File frontendDir = resolveFrontendDir();

        if (frontendDir == null || !frontendDir.exists()) {
            System.err.println("Frontend directory not found. Looked at: " + frontendDir);
            System.err.println("Pass -Dfrontend.path=/absolute/path/to/frontend if needed.");
            return;
        }

        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String npmCmd = isWindows ? "npm.cmd" : "npm";

        ProcessBuilder pb = new ProcessBuilder(npmCmd, "run", "dev");
        pb.directory(frontendDir);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        frontendProcess = pb.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (frontendProcess != null && frontendProcess.isAlive()) {
                frontendProcess.destroy();
            }
        }));
    }

    private File resolveFrontendDir() {
        // 1. Explicit override always wins
        String explicit = System.getProperty("frontend.path");
        if (explicit != null) {
            return new File(explicit);
        }

        // 2. Safely find the directory the jar is running from
        org.springframework.boot.system.ApplicationHome home = new org.springframework.boot.system.ApplicationHome(FrontendLauncher.class);
        File jarDir = home.getDir();

        if (jarDir != null) {
            // 3. Try alongside the jar file (e.g. if jar is in the project root)
            File frontendInJarDir = new File(jarDir, "frontend");
            if (frontendInJarDir.exists()) {
                return frontendInJarDir;
            }

            // 4. Try parent's parent (e.g. if jar is inside target/app.jar)
            File projectRoot = jarDir.getParentFile();
            if (projectRoot != null) {
                File frontendInProjectRoot = new File(projectRoot, "frontend");
                if (frontendInProjectRoot.exists()) {
                    return frontendInProjectRoot;
                }
            }
        }

        return new File(System.getProperty("user.dir"), "frontend"); // fallback to current working directory
    }
}