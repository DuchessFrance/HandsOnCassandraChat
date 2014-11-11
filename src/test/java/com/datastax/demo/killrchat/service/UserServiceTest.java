package com.datastax.demo.killrchat.service;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.insertInto;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.demo.killrchat.entity.Schema.USERS;
import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.demo.killrchat.exceptions.UserAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.UserNotFoundException;
import com.datastax.demo.killrchat.exceptions.WrongLoginPasswordException;
import com.datastax.demo.killrchat.model.UserModel;
import info.archinnov.achilles.exception.AchillesBeanValidationException;
import info.archinnov.achilles.exception.AchillesLightWeightTransactionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.demo.killrchat.entity.User;
import info.archinnov.achilles.junit.AchillesResource;
import info.archinnov.achilles.junit.AchillesResourceBuilder;
import info.archinnov.achilles.persistence.PersistenceManager;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Rule
    public AchillesResource resource = AchillesResourceBuilder
            .withEntityPackages(User.class.getPackage().getName())
            .withKeyspaceName(KEYSPACE)
            .withBeanValidation()
            .tablesToTruncate(USERS)
            .truncateBeforeAndAfterTest().build();

    private PersistenceManager manager = resource.getPersistenceManager();

    private Session session = resource.getNativeSession();

    private UserService service = new UserService();

    @Before
    public void setUp() {
        service.manager = this.manager;
    }

    @Test
    public void should_create_user() throws Exception {
        //Given
        final UserModel model = new UserModel("emc²", "a.einstein", "Albert", "EINSTEIN", "smartGuy", "a.einstein@smart.com", "I am THE Genius");

        //When
        service.createUser(model);

        //Then
        final Row row = session.execute(select().from(USERS).where(eq("login", "emc²"))).one();

        assertThat(row).isNotNull();
        assertThat(row.getString("login")).isEqualTo("emc²");
        assertThat(row.getString("firstname")).isEqualTo("Albert");
        assertThat(row.getString("lastname")).isEqualTo("EINSTEIN");
        assertThat(row.getString("nickname")).isEqualTo("smartGuy");
        assertThat(row.getString("email")).isEqualTo("a.einstein@smart.com");
        assertThat(row.getString("bio")).isEqualTo("I am THE Genius");
    }

    @Test
    public void should_find_user_by_login() throws Exception {
        //Given
        final Insert insert = insertInto(USERS).value("login", "emc²").value("pass","a.einstein").value("firstname", "Albert").value("lastname", "EINSTEIN");
        session.execute(insert);

        //When
        final UserModel foundUser = service.findByLogin("emc²");

        //Then
        assertThat(foundUser.getFirstname()).isEqualTo("Albert");
        assertThat(foundUser.getLastname()).isEqualTo("EINSTEIN");
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void should_fail_creating_user_if_already_exist() throws Exception {
        //Given
        final UserModel model = new UserModel("emc²", "a.einstein", "Albert", "EINSTEIN", "smartGuy", "a.einstein@smart.com", "I am THE Genius");

        service.createUser(model);

        //When
        service.createUser(model);
    }

    @Test(expected = AchillesBeanValidationException.class)
    public void should_exception_if_password_not_set() throws Exception {
        //Given
        final UserModel model = new UserModel("emc²", "", "Albert", "EINSTEIN", "smartGuy", "a.einstein@smart.com", "I am THE Genius");

        //When
        service.createUser(model);

        //Then

    }

    @Test(expected = WrongLoginPasswordException.class)
    public void should_exception_if_incorrect_password() throws Exception {
        //Given
        final Insert insert = insertInto(USERS).value("login", "emc²").value("pass","a.einstein").value("firstname", "Albert").value("lastname", "EINSTEIN");
        session.execute(insert);

        //When
        service.validatePasswordForUser("emc²", "wrong_password");

        //Then

    }

    @Test
    public void should_update_user() throws Exception {
        //Given
        final Insert insert = insertInto(USERS).value("login", "emc²").value("pass","a.einstein").value("firstname", "Albert").value("lastname", "EINSTEIN");
        session.execute(insert);

        UserModel userModel = new UserModel("emc²", "", "David", "EINSTEIN", "veryCleverGuy", "david.einstein@smart.com", "I am THE Genius");

        //When
        service.updateUser(userModel);

        //Then
        final Row row = session.execute(select().from(USERS).where(eq("login", "emc²"))).one();
        assertThat(row.getString("login")).isEqualTo("emc²");
        assertThat(row.getString("firstname")).isEqualTo("David");
        assertThat(row.getString("lastname")).isEqualTo("EINSTEIN");
        assertThat(row.getString("nickname")).isEqualTo("veryCleverGuy");
        assertThat(row.getString("email")).isEqualTo("david.einstein@smart.com");
        assertThat(row.getString("bio")).isEqualTo("I am THE Genius");
    }

    @Test(expected = UserNotFoundException.class)
    public void should_fail_updating_not_existing_user() throws Exception {
        //Given
        UserModel userModel = new UserModel("emc²", "", "David", "EINSTEIN", "veryCleverGuy", "david.einstein@smart.com", "I am THE Genius");

        //When
        service.updateUser(userModel);
    }
}
