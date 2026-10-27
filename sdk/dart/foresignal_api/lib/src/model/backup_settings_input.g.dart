// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'backup_settings_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BackupSettingsInputCWProxy {
  BackupSettingsInput enabled(bool enabled);

  BackupSettingsInput schedule(String? schedule);

  BackupSettingsInput target(String target);

  BackupSettingsInput root(String? root);

  BackupSettingsInput s3(S3Target? s3);

  BackupSettingsInput keepBackups(int? keepBackups);

  BackupSettingsInput includeConfig(bool? includeConfig);

  BackupSettingsInput secrets(ArchiveSettingsSecrets? secrets);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BackupSettingsInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BackupSettingsInput(...).copyWith(id: 12, name: "My name")
  /// ```
  BackupSettingsInput call({
    bool enabled,
    String? schedule,
    String target,
    String? root,
    S3Target? s3,
    int? keepBackups,
    bool? includeConfig,
    ArchiveSettingsSecrets? secrets,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBackupSettingsInput.copyWith(...)` or call `instanceOfBackupSettingsInput.copyWith.fieldName(value)` for a single field.
class _$BackupSettingsInputCWProxyImpl implements _$BackupSettingsInputCWProxy {
  const _$BackupSettingsInputCWProxyImpl(this._value);

  final BackupSettingsInput _value;

  @override
  BackupSettingsInput enabled(bool enabled) => call(enabled: enabled);

  @override
  BackupSettingsInput schedule(String? schedule) => call(schedule: schedule);

  @override
  BackupSettingsInput target(String target) => call(target: target);

  @override
  BackupSettingsInput root(String? root) => call(root: root);

  @override
  BackupSettingsInput s3(S3Target? s3) => call(s3: s3);

  @override
  BackupSettingsInput keepBackups(int? keepBackups) =>
      call(keepBackups: keepBackups);

  @override
  BackupSettingsInput includeConfig(bool? includeConfig) =>
      call(includeConfig: includeConfig);

  @override
  BackupSettingsInput secrets(ArchiveSettingsSecrets? secrets) =>
      call(secrets: secrets);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BackupSettingsInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BackupSettingsInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BackupSettingsInput call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? schedule = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? root = const $CopyWithPlaceholder(),
    Object? s3 = const $CopyWithPlaceholder(),
    Object? keepBackups = const $CopyWithPlaceholder(),
    Object? includeConfig = const $CopyWithPlaceholder(),
    Object? secrets = const $CopyWithPlaceholder(),
  }) {
    return BackupSettingsInput(
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      schedule: schedule == const $CopyWithPlaceholder()
          ? _value.schedule
          // ignore: cast_nullable_to_non_nullable
          : schedule as String?,
      target: target == const $CopyWithPlaceholder() || target == null
          ? _value.target
          // ignore: cast_nullable_to_non_nullable
          : target as String,
      root: root == const $CopyWithPlaceholder()
          ? _value.root
          // ignore: cast_nullable_to_non_nullable
          : root as String?,
      s3: s3 == const $CopyWithPlaceholder()
          ? _value.s3
          // ignore: cast_nullable_to_non_nullable
          : s3 as S3Target?,
      keepBackups: keepBackups == const $CopyWithPlaceholder()
          ? _value.keepBackups
          // ignore: cast_nullable_to_non_nullable
          : keepBackups as int?,
      includeConfig: includeConfig == const $CopyWithPlaceholder()
          ? _value.includeConfig
          // ignore: cast_nullable_to_non_nullable
          : includeConfig as bool?,
      secrets: secrets == const $CopyWithPlaceholder()
          ? _value.secrets
          // ignore: cast_nullable_to_non_nullable
          : secrets as ArchiveSettingsSecrets?,
    );
  }
}

extension $BackupSettingsInputCopyWith on BackupSettingsInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBackupSettingsInput.copyWith(...)` or `instanceOfBackupSettingsInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BackupSettingsInputCWProxy get copyWith =>
      _$BackupSettingsInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BackupSettingsInput _$BackupSettingsInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BackupSettingsInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['enabled', 'target']);
  final val = BackupSettingsInput(
    enabled: $checkedConvert('enabled', (v) => v as bool),
    schedule: $checkedConvert('schedule', (v) => v as String?),
    target: $checkedConvert('target', (v) => v as String),
    root: $checkedConvert('root', (v) => v as String?),
    s3: $checkedConvert(
      's3',
      (v) => v == null ? null : S3Target.fromJson(v as Map<String, dynamic>),
    ),
    keepBackups: $checkedConvert('keepBackups', (v) => (v as num?)?.toInt()),
    includeConfig: $checkedConvert('includeConfig', (v) => v as bool? ?? true),
    secrets: $checkedConvert(
      'secrets',
      (v) => v == null
          ? null
          : ArchiveSettingsSecrets.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$BackupSettingsInputToJson(
  BackupSettingsInput instance,
) => <String, dynamic>{
  'enabled': instance.enabled,
  'schedule': ?instance.schedule,
  'target': instance.target,
  'root': ?instance.root,
  's3': ?instance.s3?.toJson(),
  'keepBackups': ?instance.keepBackups,
  'includeConfig': ?instance.includeConfig,
  'secrets': ?instance.secrets?.toJson(),
};
