/**
 * Multi-factor authentication (prompt R2-024): RFC 6238 TOTP through the audited {@code java-otp} library
 * ({@link ai.foresignal.security.mfa.TotpService}), single-use recovery codes stored hashed
 * ({@link ai.foresignal.security.mfa.RecoveryCodeEntity}), and the enrollment, step-up and disable logic
 * ({@link ai.foresignal.security.mfa.MfaService}). The TOTP secret is sealed with the instance key; neither it nor a
 * recovery code is ever stored in clear or logged. MFA is optional for everyone and required for a user holding the
 * built-in Admin or Operator role.
 */
package ai.foresignal.security.mfa;
