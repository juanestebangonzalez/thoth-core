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

    // === Reglas del dominio ===

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

    // === Reglas de ubicación de clases ===

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

    @Test
    void entitiesMustResideInAdapterOutPersistenceEntity() {
        ArchRule rule = classes()
            .that().areAnnotatedWith(jakarta.persistence.Entity.class)
            .should().resideInAPackage("com.thoth.adapter.out.persistence.entity");

        rule.check(classes);
    }

    // === Reglas de capas ===

    @Test
    void equipmentUseCasesMustNotDependOnJpaRepositoriesDirectly() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.thoth.application.usecase..")
            .should().dependOnClassesThat().resideInAPackage("com.thoth.adapter.out.persistence.repository..");

        rule.check(classes);
    }

    @Test
    void servicesMustNotDependOnControllers() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.thoth.application..")
            .should().dependOnClassesThat().resideInAPackage("com.thoth.adapter.in.rest.controller..");

        rule.check(classes);
    }

    @Test
    void adapterOutMustNotDependOnAdapterIn() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("com.thoth.adapter.out..")
            .should().dependOnClassesThat().resideInAPackage("com.thoth.adapter.in..");

        rule.check(classes);
    }

    // === DT-22: Reglas de convenciones ===

    @Test
    void requestDtosMustBeRecords() {
        ArchRule rule = classes()
            .that().resideInAPackage("com.thoth.adapter.in.rest.dto.request..")
            .and().haveSimpleNameEndingWith("Request")
            .should().beRecords();

        rule.check(classes);
    }

    @Test
    void scheduledJobsMustResideInSchedulerPackage() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("ScheduledJob")
            .should().resideInAPackage("com.thoth.adapter.out.scheduler");

        rule.check(classes);
    }

    @Test
    void configClassesMustResideInConfigPackage() {
        ArchRule rule = classes()
            .that().areAnnotatedWith(org.springframework.context.annotation.Configuration.class)
            .should().resideInAPackage("com.thoth.config..");

        rule.check(classes);
    }
}
