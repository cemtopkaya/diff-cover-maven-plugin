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
