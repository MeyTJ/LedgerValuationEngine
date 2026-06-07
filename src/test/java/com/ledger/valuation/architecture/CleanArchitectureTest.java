package com.ledger.valuation.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class CleanArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter().importPackages("com.ledger.valuation");

    @Test
    void domainMustNotDependOnFrameworks() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta..",
                        "org.apache.kafka..",
                        "com.fasterxml.jackson..",
                        "com.github.benmanes.caffeine.."
                );
        rule.check(classes);
    }

    @Test
    void applicationMustNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
        rule.check(classes);
    }

    @Test
    void interfacesMustNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..interfaces..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
        rule.check(classes);
    }

    @Test
    void domainMustNotDependOnApplication() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..application..");
        rule.check(classes);
    }

    @Test
    void applicationAndDomainMustNotUseSpringStereotypes() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("..domain..", "..application..")
                .should().beAnnotatedWith(Component.class)
                .orShould().beAnnotatedWith(Service.class);
        rule.check(classes);
    }
}
