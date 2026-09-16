package com.evolutionary.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 结构性判据：领域 / 应用层不得 import Spring 或 JPA。
 * [[patterns/domain-purity-is-structural]]
 */
class DomainFrameworkFreeTest {

    private static final Path MAIN = Path.of("src/main/java/com/evolutionary");

    private static final String[] FORBIDDEN_PREFIXES = {
        "import org.springframework",
        "import jakarta.persistence",
        "import org.hibernate",
    };

    @Test
    @DisplayName("domain 与 application 不 import Spring / JPA / Hibernate")
    void domainAndApplicationStayFrameworkFree() throws IOException {
        List<String> violations = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(MAIN)) {
            walk.filter(p -> p.toString().endsWith(".java"))
                    .filter(this::isDomainOrApplication)
                    .forEach(p -> checkFile(p, violations));
        }
        if (!violations.isEmpty()) {
            fail("Framework leaked into domain/application:\n" + String.join("\n", violations));
        }
        assertTrue(violations.isEmpty());
    }

    private boolean isDomainOrApplication(Path path) {
        String s = path.toString().replace('\\', '/');
        return s.contains("/domain/") || s.contains("/application/");
    }

    private void checkFile(Path path, List<String> violations) {
        try {
            List<String> lines = Files.readAllLines(path);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                for (String prefix : FORBIDDEN_PREFIXES) {
                    if (line.startsWith(prefix)) {
                        violations.add(path + ":" + (i + 1) + " " + line);
                    }
                }
            }
        } catch (IOException e) {
            violations.add(path + ": " + e.getMessage());
        }
    }
}
