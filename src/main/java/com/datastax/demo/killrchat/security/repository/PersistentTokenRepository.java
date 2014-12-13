package com.datastax.demo.killrchat.security.repository;

import com.datastax.demo.killrchat.entity.PersistentToken;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.OptionsBuilder;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

@Repository
public class PersistentTokenRepository {

    @Inject
    private PersistenceManager manager;

    public static final int TOKEN_VALIDITY_DAYS = 31;

    public static final int TOKEN_VALIDITY_SECONDS = 60 * 60 * 24 * TOKEN_VALIDITY_DAYS;

    public void insert(PersistentToken token) {
        manager.insert(token, OptionsBuilder.withTtl(TOKEN_VALIDITY_SECONDS));
    }

    public void deleteById(String series) {
        manager.deleteById(PersistentToken.class, series);

    }

    public PersistentToken findById(String presentedSeries) {
        return manager.find(PersistentToken.class, presentedSeries);
    }

    public void update(PersistentToken token) {
        manager.update(token);
    }
}
