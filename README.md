# Diff-Cover Maven Test Projesi Kurulum Rehberi

## 1. Proje Oluşturma

```bash
# Maven archetype ile yeni proje oluştur
mvn archetype:generate \
  -DgroupId=com.example.diffcover \
  -DartifactId=diff-cover-test \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false

# Proje klasörüne geç
cd diff-cover-test
```

## 2. pom.xml Konfigürasyonu

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example.diffcover</groupId>
    <artifactId>diff-cover-test</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Compiler Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>

            <!-- JaCoCo Coverage Plugin -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.8</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Diff Coverage Plugin -->
            <plugin>
                <groupId>com.form3tech</groupId>
                <artifactId>diff-coverage-maven-plugin</artifactId>
                <version>0.9.5</version>
                <configuration>
                    <coverageFile>target/site/jacoco/jacoco.xml</coverageFile>
                    <diffBase>HEAD~1</diffBase>
                    <minimumCoverageRatio>0.50</minimumCoverageRatio>
                    <failOnCoverageLoss>true</failOnCoverageLoss>
                </configuration>
                <executions>
                    <execution>
                        <phase>verify</phase>
                        <goals>
                            <goal>diff-coverage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

## 3. Ana Uygulama Kodu (coverage < %50)

**src/main/java/com/example/diffcover/Calculator.java**
```java
package com.example.diffcover;

/**
 * Basit hesap makinesi sınıfı
 * Bu sınıfta kasıtlı olarak test edilmeyen metodlar var
 */
public class Calculator {
    
    public int add(int a, int b) {
        return a + b;
    }
    
    public int subtract(int a, int b) {
        return a - b;
    }
    
    // Bu metod test edilmeyecek
    public int multiply(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        return a * b;
    }
    
    // Bu metod da test edilmeyecek
    public double divide(double a, double b) {
        if (b == 0) {
            throw new IllegalArgumentException("Sıfıra bölme hatası");
        }
        return a / b;
    }
    
    // Bu metod hiç test edilmeyecek
    public boolean isEven(int number) {
        return number % 2 == 0;
    }
    
    // Bu metod da test edilmeyecek
    public int power(int base, int exponent) {
        if (exponent == 0) {
            return 1;
        }
        int result = 1;
        for (int i = 0; i < exponent; i++) {
            result *= base;
        }
        return result;
    }
    
    // Bu metod test edilmeyecek
    public int factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Negatif sayının faktöriyeli hesaplanamaz");
        }
        if (n == 0 || n == 1) {
            return 1;
        }
        return n * factorial(n - 1);
    }
}
```

**src/main/java/com/example/diffcover/StringUtils.java**
```java
package com.example.diffcover;

/**
 * String işlemleri için yardımcı sınıf
 */
public class StringUtils {
    
    public boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    // Bu metod test edilmeyecek
    public String reverse(String str) {
        if (str == null) {
            return null;
        }
        return new StringBuilder(str).reverse().toString();
    }
    
    // Bu metod test edilmeyecek
    public String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    // Bu metod test edilmeyecek
    public int countWords(String str) {
        if (isEmpty(str)) {
            return 0;
        }
        String[] words = str.trim().split("\\s+");
        return words.length;
    }
}
```

**src/main/java/com/example/diffcover/App.java**
```java
package com.example.diffcover;

/**
 * Ana uygulama sınıfı
 */
public class App {
    public static void main(String[] args) {
        Calculator calc = new Calculator();
        StringUtils stringUtils = new StringUtils();
        
        System.out.println("=== Hesap Makinesi Test ===");
        System.out.println("5 + 3 = " + calc.add(5, 3));
        System.out.println("10 - 4 = " + calc.subtract(10, 4));
        
        System.out.println("\n=== String İşlemleri ===");
        System.out.println("'hello' boş mu? " + stringUtils.isEmpty("hello"));
        System.out.println("'' boş mu? " + stringUtils.isEmpty(""));
        
        // Bu metodlar test edilmediği için coverage düşük olacak
        System.out.println("3 * 4 = " + calc.multiply(3, 4));
        System.out.println("'world' tersten: " + stringUtils.reverse("world"));
    }
}
```

## 4. Test Kodları (sadece bazı metodları test ediyor)

**src/test/java/com/example/diffcover/CalculatorTest.java**
```java
package com.example.diffcover;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Calculator sınıfı için testler
 * Kasıtlı olarak tüm metodları test etmiyoruz
 */
public class CalculatorTest {
    
    private Calculator calculator = new Calculator();
    
    @Test
    public void testAdd() {
        assertEquals(8, calculator.add(5, 3));
        assertEquals(0, calculator.add(-5, 5));
        assertEquals(-8, calculator.add(-3, -5));
    }
    
    @Test
    public void testSubtract() {
        assertEquals(2, calculator.subtract(5, 3));
        assertEquals(-10, calculator.subtract(-5, 5));
        assertEquals(2, calculator.subtract(-3, -5));
    }
    
    // multiply, divide, isEven, power, factorial metodları test edilmiyor
    // Bu durum coverage'ın %50'nin altında olmasına neden olacak
}
```

**src/test/java/com/example/diffcover/StringUtilsTest.java**
```java
package com.example.diffcover;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * StringUtils sınıfı için testler
 */
public class StringUtilsTest {
    
    private StringUtils stringUtils = new StringUtils();
    
    @Test
    public void testIsEmpty() {
        assertTrue(stringUtils.isEmpty(null));
        assertTrue(stringUtils.isEmpty(""));
        assertTrue(stringUtils.isEmpty("   "));
        assertFalse(stringUtils.isEmpty("hello"));
        assertFalse(stringUtils.isEmpty(" hello "));
    }
    
    // reverse, capitalize, countWords metodları test edilmiyor
}
```

## 5. Maven Komutları

```bash
# Projeyi derle
mvn clean compile

# Testleri çalıştır ve coverage raporu oluştur
mvn clean test

# JaCoCo coverage raporunu oluştur
mvn jacoco:report

# Diff coverage kontrolü yap
mvn verify

# Sadece diff-coverage plugin'ini çalıştır
mvn diff-coverage:diff-coverage

# Detaylı log ile çalıştır
mvn clean verify -X
```

## 6. Git Kurulumu (diff-coverage için gerekli)

```bash
# Git repository oluştur
git init

# İlk commit
git add .
git commit -m "İlk commit: Düşük coverage'lı proje"

# Kod değişikliği yap ve yeni commit oluştur
# (diff-coverage HEAD~1 ile karşılaştırmak için)
echo "# Test projesi" > README.md
git add README.md
git commit -m "README eklendi"
```

## 7. Beklenen Sonuç

Bu konfigürasyon ile:
- **Calculator** sınıfında 7 metod var, sadece 2'si test ediliyor (~28% coverage)
- **StringUtils** sınıfında 4 metod var, sadece 1'i test ediliyor (~25% coverage)
- **Toplam coverage** yaklaşık %30-40 olacak (minimum %50'nin altında)

Maven verify komutu çalıştırıldığında diff-coverage plugin, coverage'ın minimum %50'yi geçmediği için build'i başarısız yapacaktır.

## 8. Coverage'ı Artırmak İçin

Coverage'ı %50'nin üzerine çıkarmak için eksik testleri ekleyin:

```java
// CalculatorTest.java'ya eklenecek testler
@Test
public void testMultiply() {
    assertEquals(12, calculator.multiply(3, 4));
    assertEquals(0, calculator.multiply(0, 5));
}

@Test
public void testDivide() {
    assertEquals(2.5, calculator.divide(5, 2), 0.001);
}

// StringUtilsTest.java'ya eklenecek testler  
@Test
public void testReverse() {
    assertEquals("dlrow", stringUtils.reverse("world"));
    assertNull(stringUtils.reverse(null));
}
```

🚀 Kullanımı
1. Plugin Projesini Oluştur
```bash
bash# Kabuk betiğini çalıştır
chmod +x create-plugin-project.sh
./create-plugin-project.sh
```

2. Java Kodlarını Yerleştir
```bash
bashcd diff-cover-maven-plugin

# Claude'un verdiği kodları copy-paste et:
# - DiffCoverMojo.java (final_diff_cover_mojo artifact'ından)
# - EmbeddedPythonManager.java (embedded_python_manager artifact'ından)

# Plugin'i build et
mvn clean install
```

3. Projende Kullan
```bash
bash# Coverage profili ile çalıştır
mvn clean verify -Pcoverage

# Farklı branch ile karşılaştır
mvn verify -Pcoverage -Ddiff-cover.branch=origin/develop

# Farklı eşik belirle
mvn verify -Pcoverage -Ddiff-cover.failUnder=85
```

```
=== KULLANIM ÖRNEKLERİ ===

1. Normal coverage kontrolü:
   mvn clean verify -Pcoverage

2. Strict coverage (90% eşik):
   mvn clean verify -Pstrict-coverage

3. Farklı branch ile karşılaştırma:
   mvn clean verify -Pcoverage -Ddiff-cover.branch=origin/develop

4. Farklı eşik değeri:
   mvn clean verify -Pcoverage -Ddiff-cover.failUnder=85

5. Sadece HTML raporu:
   mvn clean verify -Pcoverage -Ddiff-cover.reportFormats=html

6. Verbose mode:
   mvn clean verify -Pcoverage -Ddiff-cover.verbose=true

7. Custom Python kullanımı:
   mvn clean verify -Pcoverage -Ddiff-cover.pythonExecutable=/usr/bin/python3

8. Plugin'i atla:
   mvn clean verify -Pcoverage -Ddiff-cover.skip=true

9. Sadece diff-cover çalıştır (testler zaten yapılmışsa):
   mvn diff-cover:check -Pcoverage

10. JSON raporu da üret:
    mvn clean verify -Pcoverage -Ddiff-cover.reportFormats=html,json,console

11. Timeout artır:
    mvn clean verify -Pcoverage -Ddiff-cover.timeoutMinutes=15

12. Ek diff-cover argümanları:
    mvn clean verify -Pcoverage -Ddiff-cover.additionalArgs="--ignore-whitespace --show-uncovered"

=== RAPOR KONUMLARI ===

- HTML: target/diff-cover/diff-cover-report.html (veya belirtilen outputDirectory)
- JSON: target/diff-cover/diff-cover-report.json
- Console: Maven output'unda görüntülenir

=== ÇOK MODÜLLÜ PROJE YAPISI ===

my-java-project/
├── pom.xml (ana pom - plugin configuration burada)
├── module-a/
│   ├── pom.xml
│   └── target/site/jacoco/jacoco.xml
├── module-b/
│   ├── pom.xml
│   └── target/site/jacoco/jacoco.xml
└── module-c/
    ├── pom.xml
    └── target/site/jacoco/jacoco.xml

Plugin tüm modüllerin Jacoco raporlarını otomatik bulur ve işler.
```
