package com.example.maven.plugins.diffcover;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Maven plugin for running diff-cover with embedded Python environment.
 * 
 * This plugin provides a complete diff-cover integration with zero external dependencies.
 * It includes embedded Python binaries for Linux and macOS (both x64 and ARM64).
 * 
 * @author Claude
 * @since 1.0.0
 */
@Mojo(name = "diff-coverage", defaultPhase = LifecyclePhase.VERIFY, requiresProject = true)
public class DiffCoverMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Base branch to compare against (e.g., origin/main, origin/develop, main)
     * Can be overridden with -Ddiff-cover.branch=branch_name
     */
    @Parameter(property = "diff-cover.branch", defaultValue = "origin/main")
    private String branch;

    /**
     * Minimum coverage percentage required for diff-cover to pass.
     * Can be overridden with -Ddiff-cover.failUnder=75
     */
    @Parameter(property = "diff-cover.failUnder", defaultValue = "80")
    private int failUnder;

    /**
     * Report formats to generate: html, console, json (comma-separated).
     * Can be overridden with -Ddiff-cover.reportFormats=html,console
     */
    @Parameter(property = "diff-cover.reportFormats", defaultValue = "html,console")
    private String reportFormats;

    /**
     * Skip diff-cover execution.
     * Can be overridden with -Ddiff-cover.skip=true
     */
    @Parameter(property = "diff-cover.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Custom Python executable path (if specified, embedded Python will be skipped).
     * Can be overridden with -Ddiff-cover.pythonExecutable=/path/to/python3
     */
    @Parameter(property = "diff-cover.pythonExecutable")
    private String pythonExecutable;

    /**
     * Additional diff-cover arguments (space-separated).
     * Can be overridden with -Ddiff-cover.additionalArgs="--ignore-staged --ignore-unstaged"
     */
    @Parameter(property = "diff-cover.additionalArgs")
    private String additionalArgs;

    /**
     * Include only files matching these patterns (comma-separated glob patterns).
     * Can be overridden with -Ddiff-cover.includePatterns="*.java,*.kt"
     */
    @Parameter(property = "diff-cover.includePatterns")
    private String includePatterns;

    /**
     * Exclude files matching these patterns (comma-separated glob patterns).
     * Can be overridden with -Ddiff-cover.excludePatterns="**\/target\/**,**\/*.generated.java"
     */
    @Parameter(property = "diff-cover.excludePatterns")
    private String excludePatterns;

    /**
     * Custom output directory for reports (defaults to target/).
     * Can be overridden with -Ddiff-cover.outputDirectory=reports/
     */
    @Parameter(property = "diff-cover.outputDirectory", defaultValue = "${project.build.directory}")
    private String outputDirectory;

    /**
     * Timeout for diff-cover execution in minutes.
     * Can be overridden with -Ddiff-cover.timeoutMinutes=10
     */
    @Parameter(property = "diff-cover.timeoutMinutes", defaultValue = "5")
    private int timeoutMinutes;

    /**
     * Verbose output from diff-cover.
     * Can be overridden with -Ddiff-cover.verbose=true
     */
    @Parameter(property = "diff-cover.verbose", defaultValue = "false")
    private boolean verbose;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Diff-cover execution skipped");
            return;
        }

        logConfiguration();

        try {
            // 1. Python ortamını hazırla
            String pythonCmd = setupPythonEnvironment();
            
            // 2. Jacoco raporlarını bul
            List<File> jacocoReports = findJacocoReports();
            
            if (jacocoReports.isEmpty()) {
                getLog().warn("No Jacoco reports found. Make sure tests are run and Jacoco plugin is configured.");
                getLog().warn("Expected locations:");
                getLog().warn("  - " + project.getBuild().getDirectory() + "/site/jacoco/jacoco.xml");
                if (project.getModules() != null) {
                    for (String module : project.getModules()) {
                        getLog().warn("  - " + module + "/target/site/jacoco/jacoco.xml");
                    }
                }
                return;
            }
            
            // 3. diff-cover komutunu çalıştır
            runDiffCover(pythonCmd, jacocoReports);
            
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to execute diff-cover", e);
        }
    }

    private void logConfiguration() {
        if (getLog().isInfoEnabled()) {
            getLog().info("=== Diff-Cover Configuration ===");
            getLog().info("Branch: " + branch);
            getLog().info("Fail Under: " + failUnder + "%");
            getLog().info("Report Formats: " + reportFormats);
            getLog().info("Output Directory: " + outputDirectory);
            getLog().info("Timeout: " + timeoutMinutes + " minutes");
            if (pythonExecutable != null) {
                getLog().info("Custom Python: " + pythonExecutable);
            } else {
                getLog().info("Using embedded Python");
            }
            if (additionalArgs != null) {
                getLog().info("Additional Args: " + additionalArgs);
            }
            if (includePatterns != null) {
                getLog().info("Include Patterns: " + includePatterns);
            }
            if (excludePatterns != null) {
                getLog().info("Exclude Patterns: " + excludePatterns);
            }
            getLog().info("================================");
        }
    }

    private String setupPythonEnvironment() throws MojoExecutionException {
        if (pythonExecutable != null && !pythonExecutable.trim().isEmpty()) {
            getLog().info("Using custom Python executable: " + pythonExecutable);
            
            // Custom Python'un çalıştığını doğrula
            try {
                ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "--version");
                Process process = pb.start();
                int exitCode = process.waitFor();
                
                if (exitCode != 0) {
                    throw new MojoExecutionException("Custom Python executable failed: " + pythonExecutable);
                }
                
                String version = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
                getLog().info("Custom Python version: " + version.trim());
                
            } catch (IOException | InterruptedException e) {
                throw new MojoExecutionException("Failed to verify custom Python executable", e);
            }
            
            return pythonExecutable;
        } else {
            getLog().info("Setting up embedded Python environment...");
            EmbeddedPythonManager pythonManager = new EmbeddedPythonManager(getLog(), project.getBasedir());
            
            if (getLog().isDebugEnabled()) {
                getLog().debug(pythonManager.getEmbeddedPythonInfo());
            }
            
            return pythonManager.setupEmbeddedPython();
        }
    }

    private List<File> findJacocoReports() {
        List<File> reports = new ArrayList<>();
        
        // Ana proje için jacoco raporunu bul
        File mainReport = new File(project.getBuild().getDirectory(), "site/jacoco/jacoco.xml");
        if (mainReport.exists()) {
            reports.add(mainReport);
            getLog().info("Found Jacoco report: " + mainReport.getAbsolutePath());
        }
        
        // Alt modüller için jacoco raporlarını bul (pom.xml'deki modules)
        if (project.getModules() != null && !project.getModules().isEmpty()) {
            for (String module : project.getModules()) {
                File moduleReport = new File(project.getBasedir(), module + "/target/site/jacoco/jacoco.xml");
                if (moduleReport.exists()) {
                    reports.add(moduleReport);
                    getLog().info("Found module Jacoco report: " + moduleReport.getAbsolutePath());
                }
            }
        }
        
        // Reaktör projelerinde de ara (Maven session'daki tüm projeler)
        if (project.getCollectedProjects() != null) {
            for (MavenProject collectedProject : project.getCollectedProjects()) {
                File collectedReport = new File(collectedProject.getBuild().getDirectory(), "site/jacoco/jacoco.xml");
                if (collectedReport.exists() && !reports.contains(collectedReport)) {
                    reports.add(collectedReport);
                    getLog().info("Found collected project Jacoco report: " + collectedReport.getAbsolutePath());
                }
            }
        }
        
        // Parent project'in execution root'unu kontrol et
        if (project.isExecutionRoot() && project.hasParent()) {
            // Multi-module parent project durumunda alt dizinleri tara
            File[] subdirs = project.getBasedir().listFiles(File::isDirectory);
            if (subdirs != null) {
                for (File subdir : subdirs) {
                    File subdirReport = new File(subdir, "target/site/jacoco/jacoco.xml");
                    if (subdirReport.exists() && !reports.contains(subdirReport)) {
                        reports.add(subdirReport);
                        getLog().info("Found subproject Jacoco report: " + subdirReport.getAbsolutePath());
                    }
                }
            }
        }
        
        return reports;
    }

    private void runDiffCover(String pythonCmd, List<File> jacocoReports) 
                throws MojoExecutionException, MojoFailureException {
        try {
            List<String> command = buildDiffCoverCommand(pythonCmd, jacocoReports);
            
            getLog().info("Running diff-cover command:");
            getLog().info("  " + String.join(" ", command));
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(project.getBasedir());
            pb.redirectErrorStream(true);
            
            if (verbose) {
                pb.inheritIO(); // Show output in real-time
            }
            
            Process process = pb.start();
            boolean finished = process.waitFor(timeoutMinutes, TimeUnit.MINUTES);
            
            if (!finished) {
                process.destroyForcibly();
                throw new MojoExecutionException("diff-cover process timed out after " + timeoutMinutes + " minutes");
            }
            
            int exitCode = process.exitValue();
            
            // Output'u oku (verbose mode değilse)
            if (!verbose) {
                String output = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
                if (!output.isEmpty()) {
                    getLog().info("diff-cover output:");
                    getLog().info(output);
                }
            }
            
            handleDiffCoverExitCode(exitCode);
            
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException("Failed to run diff-cover", e);
        }
    }

    private List<String> buildDiffCoverCommand(String pythonCmd, List<File> jacocoReports) {
        List<String> command = new ArrayList<>();
        
        // Base command
        command.add(pythonCmd);
        command.add("-m");
        command.add("diff_cover");
        
        // Coverage raporları ekle
        for (File report : jacocoReports) {
            command.add("--coverage-report");
            command.add(report.getAbsolutePath());
        }
        
        // Base branch
        command.add("--compare-branch");
        command.add(branch);
        
        // Fail threshold
        command.add("--fail-under");
        command.add(String.valueOf(failUnder));
        
        // Report formatları
        List<String> formats = Arrays.asList(reportFormats.toLowerCase().split(","));
        File outputDir = new File(outputDirectory);
        
        for (String format : formats) {
            format = format.trim();
            switch (format) {
                case "html":
                    File htmlReport = new File(outputDir, "diff-cover-report.html");
                    command.add("--html-report");
                    command.add(htmlReport.getAbsolutePath());
                    getLog().info("HTML report will be generated: " + htmlReport.getAbsolutePath());
                    break;
                    
                case "json":
                    File jsonReport = new File(outputDir, "diff-cover-report.json");
                    command.add("--json-report");
                    command.add(jsonReport.getAbsolutePath());
                    getLog().info("JSON report will be generated: " + jsonReport.getAbsolutePath());
                    break;
                    
                case "console":
                    // Console output is default
                    break;
                    
                default:
                    getLog().warn("Unknown report format: " + format + ". Supported: html, json, console");
            }
        }
        
        // Include patterns
        if (includePatterns != null && !includePatterns.trim().isEmpty()) {
            String[] patterns = includePatterns.split(",");
            for (String pattern : patterns) {
                command.add("--include");
                command.add(pattern.trim());
            }
        }
        
        // Exclude patterns
        if (excludePatterns != null && !excludePatterns.trim().isEmpty()) {
            String[] patterns = excludePatterns.split(",");
            for (String pattern : patterns) {
                command.add("--exclude");
                command.add(pattern.trim());
            }
        }
        
        // Verbose mode
        if (verbose) {
            command.add("--verbose");
        }
        
        // Ek argumentlar
        if (additionalArgs != null && !additionalArgs.trim().isEmpty()) {
            String[] extraArgs = additionalArgs.trim().split("\\s+");
            command.addAll(Arrays.asList(extraArgs));
        }
        
        return command;
    }

    private void handleDiffCoverExitCode(int exitCode) throws MojoExecutionException, MojoFailureException {
        switch (exitCode) {
            case 0:
                getLog().info("✅ diff-cover completed successfully - coverage requirements met");
                break;
                
            case 1:
                // diff-cover failed due to coverage threshold
                String message = String.format("❌ diff-cover failed: Coverage is below %d%% threshold", failUnder);
                getLog().error(message);
                getLog().error("To fix this:");
                getLog().error("  1. Add more tests for the changed code");
                getLog().error("  2. Lower the threshold with -Ddiff-cover.failUnder=<number>");
                getLog().error("  3. Check the HTML report for details: " + outputDirectory + "/diff-cover-report.html");
                throw new MojoFailureException(message);
                
            case 2:
                throw new MojoExecutionException("diff-cover failed: Invalid arguments or configuration error");
                
            default:
                throw new MojoExecutionException("diff-cover failed with exit code: " + exitCode);
        }
    }
}