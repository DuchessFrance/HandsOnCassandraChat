package com.datastax.demo.killrchat.service;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.demo.killrchat.entity.Schema.USERS;
import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.demo.killrchat.exceptions.UserAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.UserNotFoundException;
import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.demo.killrchat.security.authority.CustomUserDetails;
import com.google.common.collect.Sets;
import info.archinnov.achilles.exception.AchillesBeanValidationException;
import info.archinnov.achilles.script.ScriptExecutor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.demo.killrchat.entity.UserEntity;
import info.archinnov.achilles.junit.AchillesResource;
import info.archinnov.achilles.junit.AchillesResourceBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Rule
    public AchillesResource resource = AchillesResourceBuilder
            .withEntityPackages(UserEntity.class.getPackage().getName())
            .withKeyspaceName(KEYSPACE)
            .withBeanValidation()
            .tablesToTruncate(USERS)
            .truncateBeforeAndAfterTest().build();

    private Session session = resource.getNativeSession();

    private ScriptExecutor scriptExecutor = resource.getScriptExecutor();

    private UserService service = new UserService();

    @Before
    public void setUp() {
        service.manager = resource.getPersistenceManager();
    }

    @Test
    public void should_create_user() throws Exception {
        //Given
        final UserModel model = new UserModel("emc²", "a.einstein", "Albert", "EINSTEIN", "a.einstein@smart.com", "I am THE Genius");

        //When
        service.createUser(model);

        //Then
        final Row row = session.execute(select().from(USERS).where(eq("login", "emc²"))).one();

        assertThat(row).isNotNull();
        assertThat(row.getString("login")).isEqualTo("emc²");
        assertThat(row.getString("firstname")).isEqualTo("Albert");
        assertThat(row.getString("lastname")).isEqualTo("EINSTEIN");
        assertThat(row.getString("email")).isEqualTo("a.einstein@smart.com");
        assertThat(row.getString("bio")).isEqualTo("I am THE Genius");
    }


    @Test
    public void should_find_user_by_login() throws Exception {
        //Given
        scriptExecutor.executeScript("should_find_user_by_login.cql");

        //When
        final UserEntity foundUser = service.findByLogin("emc²");

        //Then
        assertThat(foundUser.getFirstname()).isEqualTo("Albert");
        assertThat(foundUser.getLastname()).isEqualTo("EINSTEIN");
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void should_fail_creating_user_if_already_exist() throws Exception {
        //Given
        final UserModel model = new UserModel("emc²", "a.einstein", "Albert", "EINSTEIN", "a.einstein@smart.com", "I am THE Genius");

        service.createUser(model);

        //When
        service.createUser(model);
    }

    @Test(expected = AchillesBeanValidationException.class)
    public void should_exception_if_password_not_set() throws Exception {
        //Given
        final UserModel model = new UserModel("emc²", "", "Albert", "EINSTEIN", "a.einstein@smart.com", "I am THE Genius");

        //When
        service.createUser(model);

        //Then
    }

    @Test
    public void should_fetch_remember_me_user() throws Exception {
        //Given
        final Authentication authentication = new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return "emc²";
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

            }

            @Override
            public String getName() {
                return "emc²";
            }
        };
        SecurityContextHolder.getContext().setAuthentication(authentication);
        scriptExecutor.executeScript("should_find_user_by_login.cql");

        //When
        final UserModel userModel = service.fetchRememberMeUser();

        //Then
        assertThat(userModel.getLogin()).isEqualTo("emc²");
        assertThat(userModel.getFirstname()).isEqualTo("Albert");
        assertThat(userModel.getLastname()).isEqualTo("EINSTEIN");
    }
}
