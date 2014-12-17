package com.datastax.demo.killrchat.configuration;

import com.datastax.demo.killrchat.entity.Schema;
import com.datastax.demo.killrchat.entity.User;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import info.archinnov.achilles.embedded.CassandraEmbeddedServerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

@Configuration
public class CassandraConfiguration {

    private static final String CLUSTER_NAME = "killrchat";

    @Inject
    private Environment env;

    @Profile(Profiles.SPRING_PROFILE_DEVELOPMENT)
    @Bean
    public Cluster cassandraNativeSessionDev() {
        final Cluster cluster = CassandraEmbeddedServerBuilder
                .noEntityPackages()
                .cleanDataFilesAtStartup(Boolean.parseBoolean(env.getProperty("cassandra.test.clean.files")))
                .withClusterName(CLUSTER_NAME)
                .buildNativeClusterOnly();

        String keyspaceCreation = "CREATE KEYSPACE IF NOT EXISTS "+Schema.KEYSPACE+" WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}";

        cluster.connect().execute(keyspaceCreation);

        return cluster;
    }

    @Profile(Profiles.SPRING_PROFILE_PRODUCTION)
    @Bean
    public Cluster cassandraNativeSessionProduction() {

        return Cluster.builder()
                .addContactPoints(env.getProperty("cassandra.host"))
                .withPort(Integer.parseInt(env.getProperty("cassandra.cql.port")))
                .withClusterName(CLUSTER_NAME)
                .build();
    }
}
