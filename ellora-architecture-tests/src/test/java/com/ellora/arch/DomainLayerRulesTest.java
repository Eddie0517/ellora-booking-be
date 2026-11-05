package com.ellora.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DomainLayerRulesTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.ellora");
    }

    @Test
    @DisplayName("Domain layer must not depend on Spring Framework")
    void domainMustNotDependOnSpring() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("org.springframework..")
                .as("Domain classes must not depend on Spring Framework")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Domain layer must not depend on Jakarta or JavaX Persistence")
    void domainMustNotDependOnPersistence() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("jakarta.persistence..", "javax.persistence..")
                .as("Domain classes must not depend on JPA / Persistence annotations or APIs")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Domain layer must not depend on Infrastructure layer")
    void domainMustNotDependOnInfrastructure() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..infrastructure..")
                .as("Domain classes must not depend on the Infrastructure layer")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Domain layer must not depend on Application layer")
    void domainMustNotDependOnApplication() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..application..")
                .as("Domain classes must not depend on the Application layer")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Domain layer must not depend on API layer")
    void domainMustNotDependOnApi() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..api..")
                .as("Domain classes must not depend on the API layer")
                .check(importedClasses);
    }
}
