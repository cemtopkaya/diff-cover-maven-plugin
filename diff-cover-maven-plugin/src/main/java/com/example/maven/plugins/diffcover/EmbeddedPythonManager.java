package com.example.maven.plugins.diffcover;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Embedded Python yöneticisi - JAR içinde gömülü Python binary'leri kullanır
 */
public class EmbeddedPythonManager {
    
    private final Log log;
    private final File workDir;
    private final String osName;
    private final String osArch;
    
    private static final String PYTHON_VERSION = "3.11.6";
    private static final String DIFF_COVER_VERSION = "7.7.0";
    
    public EmbeddedPythonManager(Log log, File baseDir) {
        this.log = log;
        this.workDir = new File(baseDir, ".diff-cover-plugin");
        this.osName = System.getProperty("os.name").toLowerCase();
        this.osArch = System.getProperty("os.arch").toLowerCase();
        
        // Plugin çalışma dizinini oluştur
        if (!workDir.exists()) {
            workDir.mkdirs();
        }
    }
    
    /**
     * Embedded Python'u hazırlar ve executable path'ini döner
     */
    public String setupEmbeddedPython() throws MojoExecutionException {
        try {
            // Platform kontrolü
            String platformKey = detectPlatform();
            log.info("Detected platform: " + platformKey);
            
            // Python executable path
            File pythonDir = new File(workDir, "python-" + platformKey);
            File pythonExecutable = findPythonExecutableRecursively(pythonDir);
            
            // Python already extracted?
            if (pythonExecutable.exists() && pythonExecutable.canExecute()) {
                log.info("Embedded Python already available: " + pythonExecutable.getAbsolutePath());
                return pythonExecutable.getAbsolutePath();
            }

            // Clean up existing directory and extract again
            if (pythonDir.exists()) {
                FileUtils.deleteDirectory(pythonDir);
            }
            
            // Extract Python from JAR
            extractEmbeddedPython(platformKey);
            
            // Re-find the executable
            pythonExecutable = findPythonExecutableRecursively(pythonDir);

            if (!pythonExecutable.exists()) {
                throw new MojoExecutionException("Python executable not found after extraction: " + pythonExecutable.getAbsolutePath());
            }

            // Set executable permission
            if (!pythonExecutable.canExecute()) {
                log.info("Attempting to set executable permission for: " + pythonExecutable.getAbsolutePath());
                boolean success = pythonExecutable.setExecutable(true, false);
                if (!success) {
                    throw new MojoExecutionException("Failed to set executable permission for: " + pythonExecutable.getAbsolutePath());
                } else {
                    log.info("Executable permission set successfully.");
                }
            }
            
            // Install diff-cover
            installDiffCover(pythonExecutable.getAbsolutePath());
            
            log.info("Embedded Python ready: " + pythonExecutable.getAbsolutePath());
            return pythonExecutable.getAbsolutePath();
            
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to setup embedded Python", e);
        }
    }
    
    private String detectPlatform() throws MojoExecutionException {
        boolean isLinux = osName.contains("linux");
        boolean isMacOS = osName.contains("mac") || osName.contains("darwin");
        boolean isArm = osArch.contains("aarch64") || osArch.contains("arm64");
        boolean isX64 = osArch.contains("x86_64") || osArch.contains("amd64");
        
        if (isLinux && isX64) return "linux-x64";
        if (isLinux && isArm) return "linux-arm64";
        if (isMacOS && isX64) return "macos-x64";
        if (isMacOS && isArm) return "macos-arm64";
        
        throw new MojoExecutionException("Unsupported platform: " + osName + "/" + osArch + 
                                       " (only Linux x64/ARM64 and macOS x64/ARM64 are supported)");
    }
    
    private void extractEmbeddedPython(String platformKey) throws Exception {
        String resourcePath = "/python/python-" + platformKey + ".tar.gz";
        File pythonDir = new File(workDir, "python-" + platformKey);
        
        // Zaten extract edilmiş mi?
        if (pythonDir.exists()) {
            log.debug("Python directory already exists: " + pythonDir.getAbsolutePath());
            return;
        }
        
        log.info("Extracting embedded Python " + PYTHON_VERSION + " for " + platformKey + "...");
        
        // JAR içinden Python arşivini oku
        try (InputStream resourceStream = getClass().getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                throw new MojoExecutionException("Python binary not found in plugin JAR: " + resourcePath);
            }
            
            // Arşivi extract et
            extractTarGzStream(resourceStream, pythonDir);
            
            log.info("Python extracted to: " + pythonDir.getAbsolutePath());
            
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to extract Python from JAR", e);
        }
    }
    
    private void extractTarGzStream(InputStream inputStream, File targetDir) throws IOException {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        
        try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(inputStream);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
            
            TarArchiveEntry entry;
            while ((entry = tarIn.getNextTarEntry()) != null) {
                File outputFile = new File(targetDir, entry.getName());
                
                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    // Parent dizinleri oluştur
                    outputFile.getParentFile().mkdirs();
                    
                    // Dosyayı extract et
                    try (FileOutputStream out = new FileOutputStream(outputFile)) {
                        IOUtils.copy(tarIn, out);
                    }
                    
                    // Unix permissions'ı ayarla
                    if (!osName.contains("windows") && entry.getMode() != 0) {
                        try {
                            Files.setPosixFilePermissions(outputFile.toPath(), 
                                PosixFilePermissions.fromString(getPermissionString(entry.getMode())));
                        } catch (Exception e) {
                            log.debug("Could not set permissions for: " + outputFile.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }
    
    private String getPermissionString(int mode) {
        StringBuilder permissions = new StringBuilder();
        
        // Owner permissions
        permissions.append((mode & 0400) != 0 ? 'r' : '-');
        permissions.append((mode & 0200) != 0 ? 'w' : '-');
        permissions.append((mode & 0100) != 0 ? 'x' : '-');
        
        // Group permissions  
        permissions.append((mode & 0040) != 0 ? 'r' : '-');
        permissions.append((mode & 0020) != 0 ? 'w' : '-');
        permissions.append((mode & 0010) != 0 ? 'x' : '-');
        
        // Others permissions
        permissions.append((mode & 0004) != 0 ? 'r' : '-');
        permissions.append((mode & 0002) != 0 ? 'w' : '-');
        permissions.append((mode & 0001) != 0 ? 'x' : '-');
        
        return permissions.toString();
    }
    
    private File getPythonExecutablePath(String platformKey) {
        File pythonDir = new File(workDir, "python-" + platformKey);
        
        // Python build standalone'ın tipik dizin yapısı
        File[] possiblePaths = {
            new File(pythonDir, "python/install/bin/python3"),
            new File(pythonDir, "python/install/bin/python"),
            new File(pythonDir, "install/bin/python3"),
            new File(pythonDir, "install/bin/python"),
            new File(pythonDir, "bin/python3"),
            new File(pythonDir, "bin/python"),
            new File(pythonDir, "python3"),
            new File(pythonDir, "python")
        };
        
        for (File path : possiblePaths) {
            if (path.exists()) {
                return path;
            }
        }
        
        // Dizin içinde python executable'ı ara
        File found = findPythonExecutableRecursively(pythonDir);
        return found.exists() ? found : possiblePaths[0]; // Fallback
    }
    
    private File findPythonExecutableRecursively(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            return new File("python3-not-found");
        }
        
        File[] files = dir.listFiles();
        if (files == null) {
            return new File("python3-not-found");
        }
        
        // Önce mevcut dizinde python3 ara
        for (File file : files) {
            if (file.isFile() && file.canExecute()) {
                String name = file.getName();
                if ("python3".equals(name) || "python".equals(name)) {
                    return file;
                }
            }
        }
        
        // Alt dizinlerde ara
        for (File file : files) {
            if (file.isDirectory()) {
                File found = findPythonExecutableRecursively(file);
                if (found.exists()) {
                    return found;
                }
            }
        }
        
        return new File("python3-not-found");
    }
    
    /**
     * diff-cover'ı embedded Python ortamına yükler
     */
    private void installDiffCover(String pythonExecutable) throws MojoExecutionException {
        try {
            // diff-cover zaten yüklü mü kontrol et
            ProcessBuilder checkPb = new ProcessBuilder(pythonExecutable, "-m", "diff_cover", "--version");
            Process checkProcess = checkPb.start();
            int exitCode = checkProcess.waitFor();
            
            if (exitCode == 0) {
                String version = IOUtils.toString(checkProcess.getInputStream(), "UTF-8");
                log.info("diff-cover already installed in embedded Python: " + version.trim());
                return;
            }
        } catch (Exception e) {
            log.debug("diff-cover not found, will install: " + e.getMessage());
        }
        
        log.info("Installing diff-cover " + DIFF_COVER_VERSION + " in embedded Python...");
        
        try {
            // pip'i güncelle
            ProcessBuilder pipUpgrade = new ProcessBuilder(pythonExecutable, "-m", "pip", "install", "--upgrade", "pip");
            pipUpgrade.redirectErrorStream(true);
            Process upgradeProcess = pipUpgrade.start();
            upgradeProcess.waitFor();
            
            // diff-cover'ı yükle
            ProcessBuilder installPb = new ProcessBuilder(pythonExecutable, "-m", "pip", "install", 
                                                         "diff-cover==" + DIFF_COVER_VERSION);
            installPb.redirectErrorStream(true);
            installPb.inheritIO(); // Output'u göster
            
            Process installProcess = installPb.start();
            int installExitCode = installProcess.waitFor();
            
            if (installExitCode != 0) {
                throw new MojoExecutionException("Failed to install diff-cover in embedded Python. Exit code: " + installExitCode);
            }
            
            log.info("diff-cover installed successfully in embedded Python environment");
            
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException("Failed to install diff-cover", e);
        }
    }
    
    /**
     * Embedded Python binary'lerinin boyutunu hesaplar
     */
    public String getEmbeddedPythonInfo() {
        try {
            long totalSize = 0;
            String[] platforms = {"linux-x64", "linux-arm64", "macos-x64", "macos-arm64"};
            
            for (String platform : platforms) {
                String resourcePath = "/python/python-" + platform + ".tar.gz";
                try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                    if (is != null) {
                        totalSize += is.available();
                    }
                }
            }
            
            return "Embedded Python binaries: " + FileUtils.byteCountToDisplaySize(totalSize) + 
                   " (4 platforms: Linux x64/ARM64, macOS x64/ARM64)";
                   
        } catch (IOException e) {
            return "Embedded Python binaries: Size calculation failed";
        }
    }
}