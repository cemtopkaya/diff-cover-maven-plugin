#!/bin/bash

# diff-cover-maven-plugin proje oluşturucu
set -e

PLUGIN_NAME="diff-cover-maven-plugin"
PLUGIN_VERSION="1.0.0"
GROUP_ID="com.example.maven.plugins"
ARTIFACT_ID="diff-cover-maven-plugin"

echo "🚀 Creating Diff-Cover Maven Plugin Project..."
echo "================================================"

# Proje dizinini oluştur
echo "📁 Creating project directory: $PLUGIN_NAME"
mkdir -p "$PLUGIN_NAME"
cd "$PLUGIN_NAME"

# Maven dizin yapısını oluştur
echo "📂 Setting up Maven directory structure..."
mkdir -p src/main/java/com/example/maven/plugins/diffcover
mkdir -p src/main/resources/python
mkdir -p src/test/java
mkdir -p src/test/resources

# pom.xml oluştur
echo "📄 Creating pom.xml..."
cat > pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.example.maven.plugins</groupId>
    <artifactId>diff-cover-maven-plugin</artifactId>
    <version>1.0.0</version>
    <packaging>maven-plugin</packaging>
    
    <name>Diff-Cover Maven Plugin</name>
    <description>Maven plugin for diff-cover integration with embedded Python environment</description>
    
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.version>3.6.0</maven.version>
    </properties>
    
    <dependencies>
        <!-- Maven Plugin API -->
        <dependency>
            <groupId>org.apache.maven</groupId>
            <artifactId>maven-plugin-api</artifactId>
            <version>${maven.version}</version>
        </dependency>
        
        <!-- Maven Plugin Annotations -->
        <dependency>
            <groupId>org.apache.maven.plugin-tools</groupId>
            <artifactId>maven-plugin-annotations</artifactId>
            <version>3.6.0</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- Maven Core -->
        <dependency>
            <groupId>org.apache.maven</groupId>
            <artifactId>maven-core</artifactId>
            <version>${maven.version}</version>
        </dependency>
        
        <!-- Maven Project -->
        <dependency>
            <groupId>org.apache.maven</groupId>
            <artifactId>maven-project</artifactId>
            <version>2.2.1</version>
        </dependency>
        
        <!-- Commons IO for file operations -->
        <dependency>
            <groupId>commons-io</groupId>
            <artifactId>commons-io</artifactId>
            <version>2.11.0</version>
        </dependency>
        
        <!-- Commons Compress for tar.gz extraction -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-compress</artifactId>
            <version>1.21</version>
        </dependency>
        
        <!-- Jackson for JSON parsing -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.2</version>
        </dependency>
        
        <!-- JUnit for testing -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <!-- Maven Plugin Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-plugin-plugin</artifactId>
                <version>3.6.0</version>
                <configuration>
                    <goalPrefix>diff-cover</goalPrefix>
                </configuration>
            </plugin>
            
            <!-- Maven Compiler Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>8</source>
                    <target>8</target>
                </configuration>
            </plugin>
            
            <!-- Download Plugin - Python ve diff-cover indirmek için -->
            <plugin>
                <groupId>com.googlecode.maven-download-plugin</groupId>
                <artifactId>download-maven-plugin</artifactId>
                <version>1.6.8</version>
                <executions>
                    <!-- Linux x64 Python -->
                    <execution>
                        <id>download-python-linux-x64</id>
                        <phase>generate-resources</phase>
                        <goals>
                            <goal>wget</goal>
                        </goals>
                        <configuration>
                            <url>https://github.com/indygreg/python-build-standalone/releases/download/20231002/cpython-3.11.6+20231002-x86_64-unknown-linux-gnu-install_only.tar.gz</url>
                            <outputFileName>python-linux-x64.tar.gz</outputFileName>
                            <outputDirectory>${project.build.outputDirectory}/python</outputDirectory>
                        </configuration>
                    </execution>
                    
                    <!-- Linux ARM64 Python -->
                    <execution>
                        <id>download-python-linux-arm64</id>
                        <phase>generate-resources</phase>
                        <goals>
                            <goal>wget</goal>
                        </goals>
                        <configuration>
                            <url>https://github.com/indygreg/python-build-standalone/releases/download/20231002/cpython-3.11.6+20231002-aarch64-unknown-linux-gnu-install_only.tar.gz</url>
                            <outputFileName>python-linux-arm64.tar.gz</outputFileName>
                            <outputDirectory>${project.build.outputDirectory}/python</outputDirectory>
                        </configuration>
                    </execution>
                    
                    <!-- macOS x64 Python -->
                    <execution>
                        <id>download-python-macos-x64</id>
                        <phase>generate-resources</phase>
                        <goals>
                            <goal>wget</goal>
                        </goals>
                        <configuration>
                            <url>https://github.com/indygreg/python-build-standalone/releases/download/20231002/cpython-3.11.6+20231002-x86_64-apple-darwin-install_only.tar.gz</url>
                            <outputFileName>python-macos-x64.tar.gz</outputFileName>
                            <outputDirectory>${project.build.outputDirectory}/python</outputDirectory>
                        </configuration>
                    </execution>
                    
                    <!-- macOS ARM64 Python -->
                    <execution>
                        <id>download-python-macos-arm64</id>
                        <phase>generate-resources</phase>
                        <goals>
                            <goal>wget</goal>
                        </goals>
                        <configuration>
                            <url>https://github.com/indygreg/python-build-standalone/releases/download/20231002/cpython-3.11.6+20231002-aarch64-apple-darwin-install_only.tar.gz</url>
                            <outputFileName>python-macos-arm64.tar.gz</outputFileName>
                            <outputDirectory>${project.build.outputDirectory}/python</outputDirectory>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
EOF

# .gitignore oluştur
echo "📄 Creating .gitignore..."
cat > .gitignore << 'EOF'
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties
.mvn/wrapper/maven-wrapper.jar
.DS_Store
*.iml
.idea/
*.log
EOF

# README.md oluştur
echo "📄 Creating README.md..."
cat > README.md << 'EOF'
# Diff-Cover Maven Plugin

Maven plugin for running diff-cover with embedded Python environment.

## Features

- 🐍 **Embedded Python**: No system Python dependency
- 📊 **Multi-module support**: Works with complex Maven projects  
- ⚙️ **Flexible configuration**: Branch comparison, thresholds, formats
- 🎯 **Verify phase integration**: Runs after tests and Jacoco reports
- 🚀 **Zero setup**: Everything bundled in the plugin

## Usage

Add to your pom.xml:

```xml
<profile>
    <id>coverage</id>
    <build>
        <plugins>
            <plugin>
                <groupId>com.example.maven.plugins</groupId>
                <artifactId>diff-cover-maven-plugin</artifactId>
                <version>1.0.0</version>
                <executions>
                    <execution>
                        <id>check-coverage</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>check</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <branch>origin/main</branch>
                    <failUnder>75</failUnder>
                    <reportFormats>html,console</reportFormats>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

Run with:

```bash
mvn clean verify -Pcoverage
```
EOF

echo "🎯 Creating placeholder Java files..."

# Ana Mojo sınıfı için placeholder
cat > src/main/java/com/example/maven/plugins/diffcover/DiffCoverMojo.java << 'EOF'
package com.example.maven.plugins.diffcover;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

// TODO: Bu dosyayı Claude'un verdiği tam kod ile değiştirin
@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY, requiresProject = true)
public class DiffCoverMojo extends AbstractMojo {
    
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("TODO: Implement DiffCoverMojo - replace with Claude's full implementation");
    }
}
EOF

# EmbeddedPythonManager için placeholder
cat > src/main/java/com/example/maven/plugins/diffcover/EmbeddedPythonManager.java << 'EOF'
package com.example.maven.plugins.diffcover;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

// TODO: Bu dosyayı Claude'un verdiği tam kod ile değiştirin
public class EmbeddedPythonManager {
    
    public EmbeddedPythonManager(Log log) {
        // TODO: Implement
    }
    
    public String setupEmbeddedPython() throws MojoExecutionException {
        // TODO: Implement
        return "python3";
    }
}
EOF

echo ""
echo "✅ Plugin project created successfully!"
echo ""
echo "📋 Next Steps:"
echo "=============="
echo "1. cd $PLUGIN_NAME"
echo "2. Replace Java placeholder files with Claude's full implementations"
echo "3. mvn clean install"
echo "4. Use in your projects!"
echo ""
echo "🏗️  Project Structure:"
echo "├── pom.xml                    (Maven configuration)"
echo "├── README.md                  (Documentation)"
echo "├── .gitignore                 (Git ignore rules)"  
echo "└── src/"
echo "    ├── main/java/com/example/maven/plugins/diffcover/"
echo "    │   ├── DiffCoverMojo.java                    (Main plugin class)"
echo "    │   └── EmbeddedPythonManager.java           (Python manager)"
echo "    ├── main/resources/python/                    (Python binaries will be here)"
echo "    └── test/java/                               (Unit tests)"
echo ""
echo "⚡ The plugin will automatically download Python binaries during build!"
echo "📦 Final JAR will be ~30-40MB but completely self-contained."
echo ""
echo "🎉 Ready to implement!"

cd ..
echo "Current directory: $(pwd)"