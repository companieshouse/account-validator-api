package uk.gov.companieshouse.account.validator.security;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import java.text.MessageFormat;

@ExtendWith(MockitoExtension.class)
class AuthenticationHelperTest {
    public static final String AUTHORISED_ROLES = "ERIC-Authorised-Roles";
    public static final String AUTHORISED_USER = "ERIC-Authorised-User";
    public static final String USER_FORMAT = "{0};forename={1};surname={2}";
    public static final String AUTHORISED_KEY_ROLES = "ERIC-Authorised-Key-Roles";
    public static final String ROLE_1_ROLE_2 = "role-1 role-2";
    public static final String ROLE_1 = "role-1";
    public static final String ROLE_2 = "role-2";
    private static final String USER_EMAIL = "qschaden@somewhere.email.com";
    private static final String USER_FORENAME = "Quentin";
    private static final String USER_SURNAME = "Schaden";
    private AuthenticationHelper testHelper;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        testHelper = new AuthenticationHelper();
    }

    @Test
    void getAuthorisedIdentityWhenRequestNull() {

        assertThat(testHelper.getAuthorisedIdentity(null), is(nullValue()));
    }

    @Test
    void getAuthorisedIdentityWhenRequestNotNull() {
        String expected = "identity";

        when(request.getHeader("ERIC-Identity")).thenReturn(expected);

        assertThat(testHelper.getAuthorisedIdentity(request), is(expected));
    }

    @Test
    void getAuthorisedIdentityType() {
        String expected = "identity-type";

        when(request.getHeader("ERIC-Identity-Type")).thenReturn(expected);

        assertThat(testHelper.getAuthorisedIdentityType(request), is(expected));
    }

    @Test
    void isApiKeyIdentityTypeWhenItIs() {
        assertThat(testHelper.isApiKeyIdentityType("key"), is(true));
    }

    @Test
    void isApiKeyIdentityTypeWhenItIsNot() {
        assertThat(testHelper.isApiKeyIdentityType("KEY"), is(false));
    }

    @Test
    void isOauth2IdentityTypeWhenItIs() {
        assertThat(testHelper.isOauth2IdentityType("oauth2"), is(true));
    }

    @Test
    void isOauth2IdentityTypeWhenItIsNot() {
        assertThat(testHelper.isOauth2IdentityType("Oauth2"), is(false));
    }

    @Test
    void getAuthorisedUser() {
        String expected = "authorised-user";

        when(request.getHeader(AUTHORISED_USER)).thenReturn(expected);

        assertThat(testHelper.getAuthorisedUser(request), is(expected));
    }

    @Test
    void getAuthorisedUserEmail() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn(
                MessageFormat.format(USER_FORMAT, USER_EMAIL, USER_FORENAME, USER_SURNAME));

        assertThat(testHelper.getAuthorisedUserEmail(request), is(USER_EMAIL));
    }

    @Test
    void getAuthorisedUserEmailWhenUserNul() {
        assertThat(testHelper.getAuthorisedUserEmail(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserEmailWhenUserMissing() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn("");

        assertThat(testHelper.getAuthorisedUserEmail(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserEmailWhenEmpty() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn(";");

        assertThat(testHelper.getAuthorisedUserEmail(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserEmailWhenNull() {
        assertThat(testHelper.getAuthorisedUserEmail(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserForename() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn(
                MessageFormat.format(USER_FORMAT, USER_EMAIL, USER_FORENAME, USER_SURNAME));

        assertThat(testHelper.getAuthorisedUserForename(request), is(USER_FORENAME));
    }

    @Test
    void getAuthorisedUserForenameWhenUserNull() {
        assertThat(testHelper.getAuthorisedUserForename(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserForenameWhenUserEmpty() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn("");

        assertThat(testHelper.getAuthorisedUserForename(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserForenameWhenMissing() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn(MessageFormat.format("{0}", USER_EMAIL));

        assertThat(testHelper.getAuthorisedUserForename(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserForenameWhenUnnamed() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn(
                MessageFormat.format("{0};{1}", USER_EMAIL, USER_FORENAME));

        assertThat(testHelper.getAuthorisedUserForename(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserSurname() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn(
                MessageFormat.format(USER_FORMAT, USER_EMAIL, USER_FORENAME, USER_SURNAME));

        assertThat(testHelper.getAuthorisedUserSurname(request), is(USER_SURNAME));
    }

    @Test
    void getAuthorisedUserSurnameWhenMissing() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn(
                MessageFormat.format("{0};forename={1}", USER_EMAIL, USER_FORENAME));

        assertThat(testHelper.getAuthorisedUserSurname(request), is(nullValue()));
    }

    @Test
    void getAuthorisedUserSurnameWhenUnnamed() {
        when(request.getHeader(AUTHORISED_USER)).thenReturn(
                MessageFormat.format("{0};forename={1};{2}", USER_EMAIL, USER_FORENAME, USER_SURNAME));

        assertThat(testHelper.getAuthorisedUserSurname(request), is(nullValue()));
    }

    @Test
    void getAuthorisedScope() {
        String expected = "authorised-scope";

        when(request.getHeader("ERIC-Authorised-Scope")).thenReturn(expected);

        assertThat(testHelper.getAuthorisedScope(request), is(expected));
    }

    @Test
    void getAuthorisedRoles() {
        String expected = "authorised-roles";

        when(request.getHeader(AUTHORISED_ROLES)).thenReturn(expected);

        assertThat(testHelper.getAuthorisedRoles(request), is(expected));
    }

    @Test
    void getAuthorisedRolesArray() {
        String[] expected = new String[]{ROLE_1, ROLE_2};

        when(request.getHeader(AUTHORISED_ROLES)).thenReturn(ROLE_1_ROLE_2);

        assertThat(testHelper.getAuthorisedRolesArray(request), is(expected));
    }

    @Test
    void getAuthorisedRolesArrayWhenRolesNull() {
        String[] expected = new String[]{};

        when(request.getHeader(AUTHORISED_ROLES)).thenReturn(null);

        assertThat(testHelper.getAuthorisedRolesArray(request), is(expected));
    }

    @Test
    void getAuthorisedRolesArrayWhenRolesEmpty() {
        String[] expected = new String[]{};

        when(request.getHeader(AUTHORISED_ROLES)).thenReturn("");

        assertThat(testHelper.getAuthorisedRolesArray(request), is(expected));
    }

    @Test
    void isRoleAuthorisedWhenItIs() {
        when(request.getHeader(AUTHORISED_ROLES)).thenReturn(ROLE_1_ROLE_2);

        assertThat(testHelper.isRoleAuthorised(request, ROLE_1), is(true));
    }

    @Test
    void isRoleAuthorisedWhenItIsNot() {
        when(request.getHeader(AUTHORISED_ROLES)).thenReturn(ROLE_1_ROLE_2);

        assertThat(testHelper.isRoleAuthorised(request, "role-0"), is(false));
    }

    @Test
    void isRoleAuthorisedWhenItIsNull() {
        assertThat(testHelper.isRoleAuthorised(request, null), is(false));
    }

    @Test
    void isRoleAuthorisedWhenItIsEmpty() {
        assertThat(testHelper.isRoleAuthorised(request, ""), is(false));
    }

    @Test
    void isRoleAuthorisedWhenRolesNull() {
        when(request.getHeader(AUTHORISED_ROLES)).thenReturn(null);

        assertThat(testHelper.isRoleAuthorised(request, ROLE_1), is(false));
    }

    @Test
    void isRoleAuthorisedWhenRolesEmpty() {
        when(request.getHeader(AUTHORISED_ROLES)).thenReturn("");

        assertThat(testHelper.isRoleAuthorised(request, ROLE_1), is(false));
    }

    @Test
    void getAuthorisedKeyRoles() {
        String expected = "authorised-key-roles";

        when(request.getHeader(AUTHORISED_KEY_ROLES)).thenReturn(expected);

        assertThat(testHelper.getAuthorisedKeyRoles(request), is(expected));

    }

    @Test
    void isKeyElevatedPrivilegesAuthorisedWhenItIs() {
        when(request.getHeader(AUTHORISED_KEY_ROLES)).thenReturn("*");

        assertThat(testHelper.isKeyElevatedPrivilegesAuthorised(request), is(true));
    }

    @Test
    void isKeyElevatedPrivilegesAuthorisedWhenItIsNot() {
        when(request.getHeader(AUTHORISED_KEY_ROLES)).thenReturn(ROLE_1_ROLE_2);

        assertThat(testHelper.isKeyElevatedPrivilegesAuthorised(request), is(false));
    }
}