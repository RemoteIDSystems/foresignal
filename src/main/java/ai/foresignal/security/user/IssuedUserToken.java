package ai.foresignal.security.user;

/**
 * A freshly issued activation or reset token: the plaintext, returned exactly once, and its stored row.
 *
 * @param plaintext the token to put in an email link or show an admin once; never stored
 * @param token the stored row (hash and prefix only)
 */
public record IssuedUserToken(String plaintext, UserTokenEntity token) {

    @Override
    public String toString() {
        return "IssuedUserToken[prefix=" + token.getTokenPrefix() + "]";
    }
}
