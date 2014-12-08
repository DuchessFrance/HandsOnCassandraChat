package com.datastax.demo.killrchat.configuration;

import com.datastax.driver.core.Cluster;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.persistence.PersistenceManagerFactory;
import info.archinnov.achilles.type.ConsistencyLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static info.archinnov.achilles.persistence.PersistenceManagerFactory.PersistenceManagerFactoryBuilder;

@Configuration
public class AchillesConfiguration {

    @Bean
    public PersistenceManager getPersistenceManager() {
        PersistenceManagerFactory pmFactory = PersistenceManagerFactoryBuilder.builder(
                Cluster.builder().addContactPoints("127.0.0.1").withPort(9042).withClusterName("achilles").build())
                .withEntityPackages("com.datastax.demo.killrchat.entity")
                .withDefaultReadConsistency(ConsistencyLevel.ONE)
                .withDefaultWriteConsistency(ConsistencyLevel.ONE)
                .withKeyspaceName("killrchat")
                .withExecutorServiceMinThreadCount(5)
                .withExecutorServiceMaxThreadCount(10)
                .forceTableCreation(true)
                .build();

        return pmFactory.createPersistenceManager();
    }
}
