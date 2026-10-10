package ai.elaro.settings;

/**
 * Published after a setting's new value is committed.
 *
 * <p>After commit, not on write, so a listener that reacts by reading the setting back cannot see
 * a value that a rollback then discards.
 *
 * @param key the setting that changed, one of {@link SettingKeys}
 */
public record AppSettingChangedEvent(String key) {}
