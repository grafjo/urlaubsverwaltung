package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.core.person.Role;

import static org.synyx.urlaubsverwaltung.core.person.Role.API_USER;


/**
 * Test users that can be used to sign in with when test data is created.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
enum TestUser {

    USER("testUser"),
    DEPARTMENT_HEAD("testHead"),
    SECOND_STAGE_AUTHORITY("testManager"),
    BOSS("testBoss"),
    OFFICE("test"),
    API_USER("testApi");

    private String login;

    TestUser(String login) {

        this.login = login;
    }

    String getLogin() {

        return login;
    }


    /**
     * Get the roles of the test user.
     *
     * @return  array of roles of the test user
     */
    Role[] getRoles() {

        switch (this) {
            case DEPARTMENT_HEAD:
                return new Role[] { Role.USER, Role.DEPARTMENT_HEAD };

            case SECOND_STAGE_AUTHORITY:
                return new Role[] { Role.USER, Role.SECOND_STAGE_AUTHORITY };

            case BOSS:
                return new Role[] { Role.USER, Role.BOSS };

            case OFFICE:
                return new Role[] { Role.USER, Role.BOSS, Role.OFFICE };

            case API_USER:
                return new Role[] { Role.USER, Role.API_USER };

            default:
                return new Role[] { Role.USER };
        }
    }
}
