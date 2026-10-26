package ai.foresignal.testsupport.security;

import ai.foresignal.security.PrincipalKind;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleEntity;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.security.user.UserStatus;
import java.time.Instant;
import java.util.UUID;

/** Creates users for integration tests of the R2 identity layer, bypassing the management API. */
public final class TestUsers {

    /** The password every user made here has. */
    public static final String PASSWORD = "correct horse battery staple";

    private TestUsers() {}

    /**
     * An active user with {@link #PASSWORD} holding the given built-in roles.
     *
     * @param principals principal creation
     * @param users the user store
     * @param userRoles the role links
     * @param hasher argon2id hashing
     * @param email the email
     * @param roles the roles it holds
     * @return the saved user
     */
    public static UserEntity active(PrincipalService principals, UserRepository users, UserRoleRepository userRoles,
            PasswordHasher hasher, String email, BuiltInRole... roles) {
        Instant now = Instant.now();
        UserEntity user = new UserEntity(UUID.randomUUID(), email, "Test " + email,
                principals.createFor(email, PrincipalKind.PERSON).getId(), null, now);
        user.setPasswordHash(hasher.hash(PASSWORD), now);
        user.setStatus(UserStatus.ACTIVE, now);
        users.saveAndFlush(user);
        for (BuiltInRole role : roles) {
            userRoles.saveAndFlush(new UserRoleEntity(user.getId(), role.id()));
        }
        return user;
    }

    /**
     * The JSON body of a login with {@link #PASSWORD}.
     *
     * @param email the email
     * @return the body
     */
    public static String loginBody(String email) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}";
    }
}
