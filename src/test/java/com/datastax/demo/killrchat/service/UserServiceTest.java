package com.datastax.demo.killrchat.service;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.insertInto;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.demo.killrchat.entity.Schema.USERS;
import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.demo.killrchat.exceptions.IncorrectOldPasswordException;
import com.datastax.demo.killrchat.exceptions.UserAlreadyExistsException;
import com.datastax.demo.killrchat.model.UserModel;
import info.archinnov.achilles.exception.AchillesBeanValidationException;
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

import java.util.List;

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
    public void should_list_users() throws Exception {
        //Given
        final Insert einstein = insertInto(USERS).value("login", "emc²").value("pass","a.einstein").value("firstname", "Albert").value("lastname", "EINSTEIN");
        final Insert maxPlank = insertInto(USERS).value("login", "maximo").value("pass","max.plank").value("firstname", "Max").value("lastname", "PLANK");
        final Insert newton = insertInto(USERS).value("login", "newton").value("pass","isaac.newton").value("firstname", "Isaac").value("lastname", "NEWTON");
        final Insert galileo = insertInto(USERS).value("login", "galileo").value("pass","galileo.galilei").value("firstname", "Galileo").value("lastname", "GALILEI");
        final Insert descartes = insertInto(USERS).value("login", "descartes").value("pass","rene.descartes").value("firstname", "René").value("lastname", "DESCARTES");

        session.execute(einstein);
        session.execute(maxPlank);
        session.execute(newton);
        session.execute(galileo);
        session.execute(descartes);

        //When
        final List<UserModel> users = service.listUsers(null, 3);

        //Then
        assertThat(users).hasSize(3);
        assertThat(users.get(0).getLogin()).isEqualTo("emc²");
        assertThat(users.get(1).getLogin()).isEqualTo("maximo");
        assertThat(users.get(2).getLogin()).isEqualTo("descartes");
    }

    @Test
    public void should_list_users_from_lower_bound() throws Exception {
        //Given
        final Insert einstein = insertInto(USERS).value("login", "emc²").value("pass","a.einstein").value("firstname", "Albert").value("lastname", "EINSTEIN");
        final Insert maxPlank = insertInto(USERS).value("login", "maximo").value("pass","max.plank").value("firstname", "Max").value("lastname", "PLANK");
        final Insert newton = insertInto(USERS).value("login", "newton").value("pass","isaac.newton").value("firstname", "Isaac").value("lastname", "NEWTON");
        final Insert galileo = insertInto(USERS).value("login", "galileo").value("pass","galileo.galilei").value("firstname", "Galileo").value("lastname", "GALILEI");
        final Insert descartes = insertInto(USERS).value("login", "descartes").value("pass","rene.descartes").value("firstname", "René").value("lastname", "DESCARTES");

        session.execute(einstein);
        session.execute(maxPlank);
        session.execute(newton);
        session.execute(galileo);
        session.execute(descartes);

        //When
        final List<UserModel> users = service.listUsers("maximo", 10);

        //Then
        assertThat(users).hasSize(3);
        assertThat(users.get(0).getLogin()).isEqualTo("descartes");
        assertThat(users.get(1).getLogin()).isEqualTo("newton");
        assertThat(users.get(2).getLogin()).isEqualTo("galileo");
    }

    @Test
    public void should_find_user_by_login() throws Exception {
        //Given
        final Insert insert = insertInto(USERS).value("login", "emc²").value("pass","a.einstein").value("firstname", "Albert").value("lastname", "EINSTEIN");
        session.execute(insert);

        //When
        final User foundUser = service.findByLogin("emc²");

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

    @Test
    public void should_change_user_password() throws Exception {
        //Given
        final Insert insert = insertInto(USERS).value("login", "emc²").value("pass","a.einstein").value("firstname", "Albert").value("lastname", "EINSTEIN");
        session.execute(insert);

        //When
        service.changeUserPassword("emc²","a.einstein","new_password");

        //Then
        final Row row = session.execute(select("pass").from(KEYSPACE, USERS).where(eq("login", "emc²"))).one();
        assertThat(row).isNotNull();
        assertThat(row.getString("pass")).isEqualTo("new_password");
    }

    @Test(expected = IncorrectOldPasswordException.class)
    public void should_exception_if_old_password_does_not_match() throws Exception {
        //Given
        final Insert insert = insertInto(USERS).value("login", "emc²").value("pass","a.einstein").value("firstname", "Albert").value("lastname", "EINSTEIN");
        session.execute(insert);

        //When
        service.changeUserPassword("emc²","wrong_old_password","new_password");


        //Then

    }
}
