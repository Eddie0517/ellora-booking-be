package com.ellora.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ControllerLayerRulesTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.ellora");
    }

    @Test
    @DisplayName("Controllers must not depend directly on JPA @Entity classes")
    void controllersMustNotDependOnJpaEntities() {
        noClasses()
                .that()
                .resideInAPackage("..api.controller..")
                .or()
                .haveSimpleNameEndingWith("Controller")
                .should()
                .dependOnClassesThat()
                .areAnnotatedWith("jakarta.persistence.Entity")
                .as("Controllers must not depend on JPA @Entity classes directly")
                .allowEmptyShould(true)
                .check(importedClasses);
    }

    @Test
    @DisplayName("Controllers must not depend directly on EntityManager")
    void controllersMustNotDependOnEntityManager() {
        noClasses()
                .that()
                .resideInAPackage("..api.controller..")
                .or()
                .haveSimpleNameEndingWith("Controller")
                .should()
                .dependOnClassesThat()
                .haveFullyQualifiedName("jakarta.persistence.EntityManager")
                .as("Controllers must not access EntityManager directly")
                .allowEmptyShould(true)
                .check(importedClasses);
    }

    @Test
    @DisplayName("Controllers must not depend directly on Spring Data JPA repositories")
    void controllersMustNotDependOnJpaRepositories() {
        noClasses()
                .that()
                .resideInAPackage("..api.controller..")
                .or()
                .haveSimpleNameEndingWith("Controller")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..infrastructure.persistence.repository..")
                .as("Controllers must not depend directly on infrastructure persistence repositories")
                .allowEmptyShould(true)
                .check(importedClasses);
    }
}
