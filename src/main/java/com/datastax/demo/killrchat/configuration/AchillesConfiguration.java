package com.datastax.demo.killrchat.configuration;

import com.datastax.demo.killrchat.entity.Schema;
import com.datastax.demo.killrchat.entity.User;
import com.datastax.driver.core.Cluster;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.persistence.PersistenceManagerFactory;
import info.archinnov.achilles.type.ConsistencyLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.List;

import static info.archinnov.achilles.persistence.PersistenceManagerFactory.PersistenceManagerFactoryBuilder;

@Configuration
public class AchillesConfiguration {

    @Inject
    private Cluster cluster;

    @Inject
    private Environment env;

    @Bean
    public PersistenceManager getPersistenceManager() {


        final List<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        boolean isProduction = activeProfiles.contains(Profiles.SPRING_PROFILE_PRODUCTION);
        PersistenceManagerFactory pmFactory = PersistenceManagerFactoryBuilder
                .builder(cluster)
                .withEntityPackages(User.class.getPackage().getName())
                .withDefaultReadConsistency(ConsistencyLevel.ONE)
                .withDefaultWriteConsistency(ConsistencyLevel.ONE)
                .withKeyspaceName(Schema.KEYSPACE)
                .withExecutorServiceMinThreadCount(5)
                .withExecutorServiceMaxThreadCount(10)
                .forceTableCreation(isProduction ? false : true)
                .build();

        return pmFactory.createPersistenceManager();
    }
}
