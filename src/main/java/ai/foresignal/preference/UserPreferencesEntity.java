package ai.foresignal.preference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * The stored part of a user's preferences ({@code user_preferences}, prompt R2-012): locale, theme and the free-form
 * UI bag. The timezone lives on the user's principal and the notification switches on the user's notification
 * preferences ({@link UserPreferencesService}). The row is deleted by the database with the user.
 */
@Entity
@Table(name = "user_preferences")
public class UserPreferencesEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "locale", length = 35)
    private String locale;

    @Column(name = "theme", length = 16)
    private String theme;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ui", nullable = false)
    private Map<String, Object> ui = new LinkedHashMap<>();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected UserPreferencesEntity() {}

    /**
     * An empty row for {@code userId}.
     *
     * @param userId the user
     * @param now when it is written
     */
    public UserPreferencesEntity(UUID userId, Instant now) {
        this.userId = userId;
        this.updatedAt = now;
    }

    /** The user. */
    public UUID getUserId() {
        return userId;
    }

    /** The BCP 47 locale, or null for the default. */
    public String getLocale() {
        return locale;
    }

    /** Sets the locale. */
    public void setLocale(String value) {
        this.locale = value;
    }

    /** The theme wire value, or null for the default. */
    public String getTheme() {
        return theme;
    }

    /** Sets the theme. */
    public void setTheme(String value) {
        this.theme = value;
    }

    /** The free-form UI preferences, never null. */
    public Map<String, Object> getUi() {
        return ui == null ? new LinkedHashMap<>() : ui;
    }

    /** Sets the UI preferences. */
    public void setUi(Map<String, Object> value) {
        this.ui = value == null ? new LinkedHashMap<>() : value;
    }

    /** When it was last written. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets when it was last written. */
    public void setUpdatedAt(Instant value) {
        this.updatedAt = value;
    }
}
