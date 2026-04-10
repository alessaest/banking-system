package com.bank.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;

@QuarkusTest
public abstract class BaseServiceTest {

    @Inject
    EntityManager em;

    @BeforeEach
    @Transactional
    public void cleanupDatabase() {
        // Clear all test data before each test
        em.createNativeQuery("DELETE FROM transaction").executeUpdate();
        em.createNativeQuery("DELETE FROM account").executeUpdate();
        em.createNativeQuery("DELETE FROM \"user\"").executeUpdate();
    }
}
