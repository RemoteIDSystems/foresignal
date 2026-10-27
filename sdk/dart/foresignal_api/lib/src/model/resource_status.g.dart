// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'resource_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ResourceStatusCWProxy {
  ResourceStatus status(String status);

  ResourceStatus disk(ResourceGauge disk);

  ResourceStatus memory(ResourceGauge? memory);

  ResourceStatus database(ResourceStatusDatabase? database);

  ResourceStatus warnings(List<String>? warnings);

  ResourceStatus checkedAt(DateTime checkedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResourceStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResourceStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  ResourceStatus call({
    String status,
    ResourceGauge disk,
    ResourceGauge? memory,
    ResourceStatusDatabase? database,
    List<String>? warnings,
    DateTime checkedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfResourceStatus.copyWith(...)` or call `instanceOfResourceStatus.copyWith.fieldName(value)` for a single field.
class _$ResourceStatusCWProxyImpl implements _$ResourceStatusCWProxy {
  const _$ResourceStatusCWProxyImpl(this._value);

  final ResourceStatus _value;

  @override
  ResourceStatus status(String status) => call(status: status);

  @override
  ResourceStatus disk(ResourceGauge disk) => call(disk: disk);

  @override
  ResourceStatus memory(ResourceGauge? memory) => call(memory: memory);

  @override
  ResourceStatus database(ResourceStatusDatabase? database) =>
      call(database: database);

  @override
  ResourceStatus warnings(List<String>? warnings) => call(warnings: warnings);

  @override
  ResourceStatus checkedAt(DateTime checkedAt) => call(checkedAt: checkedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResourceStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResourceStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ResourceStatus call({
    Object? status = const $CopyWithPlaceholder(),
    Object? disk = const $CopyWithPlaceholder(),
    Object? memory = const $CopyWithPlaceholder(),
    Object? database = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
    Object? checkedAt = const $CopyWithPlaceholder(),
  }) {
    return ResourceStatus(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as String,
      disk: disk == const $CopyWithPlaceholder() || disk == null
          ? _value.disk
          // ignore: cast_nullable_to_non_nullable
          : disk as ResourceGauge,
      memory: memory == const $CopyWithPlaceholder()
          ? _value.memory
          // ignore: cast_nullable_to_non_nullable
          : memory as ResourceGauge?,
      database: database == const $CopyWithPlaceholder()
          ? _value.database
          // ignore: cast_nullable_to_non_nullable
          : database as ResourceStatusDatabase?,
      warnings: warnings == const $CopyWithPlaceholder()
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as List<String>?,
      checkedAt: checkedAt == const $CopyWithPlaceholder() || checkedAt == null
          ? _value.checkedAt
          // ignore: cast_nullable_to_non_nullable
          : checkedAt as DateTime,
    );
  }
}

extension $ResourceStatusCopyWith on ResourceStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfResourceStatus.copyWith(...)` or `instanceOfResourceStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ResourceStatusCWProxy get copyWith => _$ResourceStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ResourceStatus _$ResourceStatusFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ResourceStatus', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status', 'disk', 'checkedAt']);
  final val = ResourceStatus(
    status: $checkedConvert('status', (v) => v as String),
    disk: $checkedConvert(
      'disk',
      (v) => ResourceGauge.fromJson(v as Map<String, dynamic>),
    ),
    memory: $checkedConvert(
      'memory',
      (v) =>
          v == null ? null : ResourceGauge.fromJson(v as Map<String, dynamic>),
    ),
    database: $checkedConvert(
      'database',
      (v) => v == null
          ? null
          : ResourceStatusDatabase.fromJson(v as Map<String, dynamic>),
    ),
    warnings: $checkedConvert(
      'warnings',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    checkedAt: $checkedConvert('checkedAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$ResourceStatusToJson(ResourceStatus instance) =>
    <String, dynamic>{
      'status': instance.status,
      'disk': instance.disk.toJson(),
      'memory': ?instance.memory?.toJson(),
      'database': ?instance.database?.toJson(),
      'warnings': ?instance.warnings,
      'checkedAt': instance.checkedAt.toIso8601String(),
    };
