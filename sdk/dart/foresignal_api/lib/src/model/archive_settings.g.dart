// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'archive_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ArchiveSettingsCWProxy {
  ArchiveSettings enabled(bool enabled);

  ArchiveSettings sources(List<String>? sources);

  ArchiveSettings root(String? root);

  ArchiveSettings target(ArchiveSettingsTargetEnum target);

  ArchiveSettings s3(ArchiveSettingsS3? s3);

  ArchiveSettings secrets(ArchiveSettingsSecrets? secrets);

  ArchiveSettings secretsSet(List<String>? secretsSet);

  ArchiveSettings keepLocal(bool? keepLocal);

  ArchiveSettings cutoff(String? cutoff);

  ArchiveSettings schedule(String? schedule);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchiveSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchiveSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  ArchiveSettings call({
    bool enabled,
    List<String>? sources,
    String? root,
    ArchiveSettingsTargetEnum target,
    ArchiveSettingsS3? s3,
    ArchiveSettingsSecrets? secrets,
    List<String>? secretsSet,
    bool? keepLocal,
    String? cutoff,
    String? schedule,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfArchiveSettings.copyWith(...)` or call `instanceOfArchiveSettings.copyWith.fieldName(value)` for a single field.
class _$ArchiveSettingsCWProxyImpl implements _$ArchiveSettingsCWProxy {
  const _$ArchiveSettingsCWProxyImpl(this._value);

  final ArchiveSettings _value;

  @override
  ArchiveSettings enabled(bool enabled) => call(enabled: enabled);

  @override
  ArchiveSettings sources(List<String>? sources) => call(sources: sources);

  @override
  ArchiveSettings root(String? root) => call(root: root);

  @override
  ArchiveSettings target(ArchiveSettingsTargetEnum target) =>
      call(target: target);

  @override
  ArchiveSettings s3(ArchiveSettingsS3? s3) => call(s3: s3);

  @override
  ArchiveSettings secrets(ArchiveSettingsSecrets? secrets) =>
      call(secrets: secrets);

  @override
  ArchiveSettings secretsSet(List<String>? secretsSet) =>
      call(secretsSet: secretsSet);

  @override
  ArchiveSettings keepLocal(bool? keepLocal) => call(keepLocal: keepLocal);

  @override
  ArchiveSettings cutoff(String? cutoff) => call(cutoff: cutoff);

  @override
  ArchiveSettings schedule(String? schedule) => call(schedule: schedule);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchiveSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchiveSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ArchiveSettings call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? root = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? s3 = const $CopyWithPlaceholder(),
    Object? secrets = const $CopyWithPlaceholder(),
    Object? secretsSet = const $CopyWithPlaceholder(),
    Object? keepLocal = const $CopyWithPlaceholder(),
    Object? cutoff = const $CopyWithPlaceholder(),
    Object? schedule = const $CopyWithPlaceholder(),
  }) {
    return ArchiveSettings(
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      root: root == const $CopyWithPlaceholder()
          ? _value.root
          // ignore: cast_nullable_to_non_nullable
          : root as String?,
      target: target == const $CopyWithPlaceholder() || target == null
          ? _value.target
          // ignore: cast_nullable_to_non_nullable
          : target as ArchiveSettingsTargetEnum,
      s3: s3 == const $CopyWithPlaceholder()
          ? _value.s3
          // ignore: cast_nullable_to_non_nullable
          : s3 as ArchiveSettingsS3?,
      secrets: secrets == const $CopyWithPlaceholder()
          ? _value.secrets
          // ignore: cast_nullable_to_non_nullable
          : secrets as ArchiveSettingsSecrets?,
      secretsSet: secretsSet == const $CopyWithPlaceholder()
          ? _value.secretsSet
          // ignore: cast_nullable_to_non_nullable
          : secretsSet as List<String>?,
      keepLocal: keepLocal == const $CopyWithPlaceholder()
          ? _value.keepLocal
          // ignore: cast_nullable_to_non_nullable
          : keepLocal as bool?,
      cutoff: cutoff == const $CopyWithPlaceholder()
          ? _value.cutoff
          // ignore: cast_nullable_to_non_nullable
          : cutoff as String?,
      schedule: schedule == const $CopyWithPlaceholder()
          ? _value.schedule
          // ignore: cast_nullable_to_non_nullable
          : schedule as String?,
    );
  }
}

extension $ArchiveSettingsCopyWith on ArchiveSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfArchiveSettings.copyWith(...)` or `instanceOfArchiveSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ArchiveSettingsCWProxy get copyWith => _$ArchiveSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ArchiveSettings _$ArchiveSettingsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ArchiveSettings', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['enabled', 'target']);
      final val = ArchiveSettings(
        enabled: $checkedConvert('enabled', (v) => v as bool? ?? false),
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        root: $checkedConvert('root', (v) => v as String?),
        target: $checkedConvert(
          'target',
          (v) => $enumDecode(_$ArchiveSettingsTargetEnumEnumMap, v),
        ),
        s3: $checkedConvert(
          's3',
          (v) => v == null
              ? null
              : ArchiveSettingsS3.fromJson(v as Map<String, dynamic>),
        ),
        secrets: $checkedConvert(
          'secrets',
          (v) => v == null
              ? null
              : ArchiveSettingsSecrets.fromJson(v as Map<String, dynamic>),
        ),
        secretsSet: $checkedConvert(
          'secretsSet',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        keepLocal: $checkedConvert('keepLocal', (v) => v as bool? ?? true),
        cutoff: $checkedConvert('cutoff', (v) => v as String?),
        schedule: $checkedConvert('schedule', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ArchiveSettingsToJson(ArchiveSettings instance) =>
    <String, dynamic>{
      'enabled': instance.enabled,
      'sources': ?instance.sources,
      'root': ?instance.root,
      'target': _$ArchiveSettingsTargetEnumEnumMap[instance.target]!,
      's3': ?instance.s3?.toJson(),
      'secrets': ?instance.secrets?.toJson(),
      'secretsSet': ?instance.secretsSet,
      'keepLocal': ?instance.keepLocal,
      'cutoff': ?instance.cutoff,
      'schedule': ?instance.schedule,
    };

const _$ArchiveSettingsTargetEnumEnumMap = {
  ArchiveSettingsTargetEnum.local: 'local',
  ArchiveSettingsTargetEnum.s3: 's3',
  ArchiveSettingsTargetEnum.glacier: 'glacier',
};
