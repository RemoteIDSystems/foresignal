// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_archive.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoArchiveCWProxy {
  SystemInfoArchive enabled(bool? enabled);

  SystemInfoArchive lastRunAt(DateTime? lastRunAt);

  SystemInfoArchive bytes(int? bytes);

  SystemInfoArchive failures(int? failures);

  SystemInfoArchive target(SystemInfoArchiveTargetEnum? target);

  SystemInfoArchive lastRunBytes(int? lastRunBytes);

  SystemInfoArchive lastRunFailures(int? lastRunFailures);

  SystemInfoArchive lastFailureAt(DateTime? lastFailureAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoArchive(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoArchive(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoArchive call({
    bool? enabled,
    DateTime? lastRunAt,
    int? bytes,
    int? failures,
    SystemInfoArchiveTargetEnum? target,
    int? lastRunBytes,
    int? lastRunFailures,
    DateTime? lastFailureAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoArchive.copyWith(...)` or call `instanceOfSystemInfoArchive.copyWith.fieldName(value)` for a single field.
class _$SystemInfoArchiveCWProxyImpl implements _$SystemInfoArchiveCWProxy {
  const _$SystemInfoArchiveCWProxyImpl(this._value);

  final SystemInfoArchive _value;

  @override
  SystemInfoArchive enabled(bool? enabled) => call(enabled: enabled);

  @override
  SystemInfoArchive lastRunAt(DateTime? lastRunAt) =>
      call(lastRunAt: lastRunAt);

  @override
  SystemInfoArchive bytes(int? bytes) => call(bytes: bytes);

  @override
  SystemInfoArchive failures(int? failures) => call(failures: failures);

  @override
  SystemInfoArchive target(SystemInfoArchiveTargetEnum? target) =>
      call(target: target);

  @override
  SystemInfoArchive lastRunBytes(int? lastRunBytes) =>
      call(lastRunBytes: lastRunBytes);

  @override
  SystemInfoArchive lastRunFailures(int? lastRunFailures) =>
      call(lastRunFailures: lastRunFailures);

  @override
  SystemInfoArchive lastFailureAt(DateTime? lastFailureAt) =>
      call(lastFailureAt: lastFailureAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoArchive(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoArchive(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoArchive call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? lastRunAt = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? failures = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? lastRunBytes = const $CopyWithPlaceholder(),
    Object? lastRunFailures = const $CopyWithPlaceholder(),
    Object? lastFailureAt = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoArchive(
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      lastRunAt: lastRunAt == const $CopyWithPlaceholder()
          ? _value.lastRunAt
          // ignore: cast_nullable_to_non_nullable
          : lastRunAt as DateTime?,
      bytes: bytes == const $CopyWithPlaceholder()
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int?,
      failures: failures == const $CopyWithPlaceholder()
          ? _value.failures
          // ignore: cast_nullable_to_non_nullable
          : failures as int?,
      target: target == const $CopyWithPlaceholder()
          ? _value.target
          // ignore: cast_nullable_to_non_nullable
          : target as SystemInfoArchiveTargetEnum?,
      lastRunBytes: lastRunBytes == const $CopyWithPlaceholder()
          ? _value.lastRunBytes
          // ignore: cast_nullable_to_non_nullable
          : lastRunBytes as int?,
      lastRunFailures: lastRunFailures == const $CopyWithPlaceholder()
          ? _value.lastRunFailures
          // ignore: cast_nullable_to_non_nullable
          : lastRunFailures as int?,
      lastFailureAt: lastFailureAt == const $CopyWithPlaceholder()
          ? _value.lastFailureAt
          // ignore: cast_nullable_to_non_nullable
          : lastFailureAt as DateTime?,
    );
  }
}

extension $SystemInfoArchiveCopyWith on SystemInfoArchive {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoArchive.copyWith(...)` or `instanceOfSystemInfoArchive.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoArchiveCWProxy get copyWith =>
      _$SystemInfoArchiveCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoArchive _$SystemInfoArchiveFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SystemInfoArchive', json, ($checkedConvert) {
      final val = SystemInfoArchive(
        enabled: $checkedConvert('enabled', (v) => v as bool?),
        lastRunAt: $checkedConvert(
          'lastRunAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        bytes: $checkedConvert('bytes', (v) => (v as num?)?.toInt()),
        failures: $checkedConvert('failures', (v) => (v as num?)?.toInt()),
        target: $checkedConvert(
          'target',
          (v) => $enumDecodeNullable(_$SystemInfoArchiveTargetEnumEnumMap, v),
        ),
        lastRunBytes: $checkedConvert(
          'lastRunBytes',
          (v) => (v as num?)?.toInt(),
        ),
        lastRunFailures: $checkedConvert(
          'lastRunFailures',
          (v) => (v as num?)?.toInt(),
        ),
        lastFailureAt: $checkedConvert(
          'lastFailureAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SystemInfoArchiveToJson(SystemInfoArchive instance) =>
    <String, dynamic>{
      'enabled': ?instance.enabled,
      'lastRunAt': ?instance.lastRunAt?.toIso8601String(),
      'bytes': ?instance.bytes,
      'failures': ?instance.failures,
      'target': ?_$SystemInfoArchiveTargetEnumEnumMap[instance.target],
      'lastRunBytes': ?instance.lastRunBytes,
      'lastRunFailures': ?instance.lastRunFailures,
      'lastFailureAt': ?instance.lastFailureAt?.toIso8601String(),
    };

const _$SystemInfoArchiveTargetEnumEnumMap = {
  SystemInfoArchiveTargetEnum.local: 'local',
  SystemInfoArchiveTargetEnum.s3: 's3',
};
