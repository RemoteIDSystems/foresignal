// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'backup_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BackupSettingsCWProxy {
  BackupSettings enabled(bool enabled);

  BackupSettings schedule(String? schedule);

  BackupSettings target(String target);

  BackupSettings root(String? root);

  BackupSettings s3(S3Target? s3);

  BackupSettings keepBackups(int? keepBackups);

  BackupSettings includeConfig(bool? includeConfig);

  BackupSettings secretsSet(List<String>? secretsSet);

  BackupSettings lastRunAt(DateTime? lastRunAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BackupSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BackupSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  BackupSettings call({
    bool enabled,
    String? schedule,
    String target,
    String? root,
    S3Target? s3,
    int? keepBackups,
    bool? includeConfig,
    List<String>? secretsSet,
    DateTime? lastRunAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBackupSettings.copyWith(...)` or call `instanceOfBackupSettings.copyWith.fieldName(value)` for a single field.
class _$BackupSettingsCWProxyImpl implements _$BackupSettingsCWProxy {
  const _$BackupSettingsCWProxyImpl(this._value);

  final BackupSettings _value;

  @override
  BackupSettings enabled(bool enabled) => call(enabled: enabled);

  @override
  BackupSettings schedule(String? schedule) => call(schedule: schedule);

  @override
  BackupSettings target(String target) => call(target: target);

  @override
  BackupSettings root(String? root) => call(root: root);

  @override
  BackupSettings s3(S3Target? s3) => call(s3: s3);

  @override
  BackupSettings keepBackups(int? keepBackups) =>
      call(keepBackups: keepBackups);

  @override
  BackupSettings includeConfig(bool? includeConfig) =>
      call(includeConfig: includeConfig);

  @override
  BackupSettings secretsSet(List<String>? secretsSet) =>
      call(secretsSet: secretsSet);

  @override
  BackupSettings lastRunAt(DateTime? lastRunAt) => call(lastRunAt: lastRunAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BackupSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BackupSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BackupSettings call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? schedule = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? root = const $CopyWithPlaceholder(),
    Object? s3 = const $CopyWithPlaceholder(),
    Object? keepBackups = const $CopyWithPlaceholder(),
    Object? includeConfig = const $CopyWithPlaceholder(),
    Object? secretsSet = const $CopyWithPlaceholder(),
    Object? lastRunAt = const $CopyWithPlaceholder(),
  }) {
    return BackupSettings(
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
      secretsSet: secretsSet == const $CopyWithPlaceholder()
          ? _value.secretsSet
          // ignore: cast_nullable_to_non_nullable
          : secretsSet as List<String>?,
      lastRunAt: lastRunAt == const $CopyWithPlaceholder()
          ? _value.lastRunAt
          // ignore: cast_nullable_to_non_nullable
          : lastRunAt as DateTime?,
    );
  }
}

extension $BackupSettingsCopyWith on BackupSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBackupSettings.copyWith(...)` or `instanceOfBackupSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BackupSettingsCWProxy get copyWith => _$BackupSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BackupSettings _$BackupSettingsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BackupSettings', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['enabled', 'target']);
      final val = BackupSettings(
        enabled: $checkedConvert('enabled', (v) => v as bool? ?? false),
        schedule: $checkedConvert('schedule', (v) => v as String?),
        target: $checkedConvert('target', (v) => v as String? ?? 'local'),
        root: $checkedConvert('root', (v) => v as String?),
        s3: $checkedConvert(
          's3',
          (v) =>
              v == null ? null : S3Target.fromJson(v as Map<String, dynamic>),
        ),
        keepBackups: $checkedConvert(
          'keepBackups',
          (v) => (v as num?)?.toInt() ?? 7,
        ),
        includeConfig: $checkedConvert(
          'includeConfig',
          (v) => v as bool? ?? true,
        ),
        secretsSet: $checkedConvert(
          'secretsSet',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        lastRunAt: $checkedConvert(
          'lastRunAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$BackupSettingsToJson(BackupSettings instance) =>
    <String, dynamic>{
      'enabled': instance.enabled,
      'schedule': ?instance.schedule,
      'target': instance.target,
      'root': ?instance.root,
      's3': ?instance.s3?.toJson(),
      'keepBackups': ?instance.keepBackups,
      'includeConfig': ?instance.includeConfig,
      'secretsSet': ?instance.secretsSet,
      'lastRunAt': ?instance.lastRunAt?.toIso8601String(),
    };
