package com.thoth.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Guards the hexagonal boundaries the codebase already respects, so future
 * changes can't silently reintroduce coupling the FASE 1 audit flagged
 * (domain leaking framework/persistence/adapter concerns).
 */
class ArchitectureTest {

    private static com.tngtech.archunit.core.domain.JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.thoth");
    }

    @Test
    void domainMustNotDependOnAdapterOrApplication() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.thoth.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.thoth.adapter..", "com.thoth.application..", "com.thoth.config..");

        rule.check(classes);
    }

    @Test
    void domainMustNotDependOnSpringFramework() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.thoth.domain..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..");

        rule.check(classes);
    }

    @Test
    void domainMustNotDependOnPersistenceFramework() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.thoth.domain..")
            .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.hibernate..");

        rule.check(classes);
    }

    @Test
    void controllersMustResideInAdapterInPackage() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Controller")
            .and().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .should().resideInAPackage("com.thoth.adapter.in.rest.controller");

        rule.check(classes);
    }

    @Test
    void repositoriesMustResideInAdapterOutPackage() {
        ArchRule rule = classes()
            .that().areInterfaces()
            .and().haveSimpleNameEndingWith("JpaRepository")
            .should().resideInAPackage("com.thoth.adapter.out.persistence.repository");

        rule.check(classes);
    }

    /**
     * Equipment use cases must depend only on EquipmentRepositoryPort, never
     * reach directly into the JPA repository - that's the adapter's job.
     * Fixed as part of the architecture cleanup; this pins it down so it
     * can't silently regress.
     */
    @Test
    void equipmentUseCasesMustNotDependOnJpaRepositoriesDirectly() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.thoth.application.usecase..")
            .should().dependOnClassesThat().resideInAPackage("com.thoth.adapter.out.persistence.repository..");

        rule.check(classes);
    }
}
