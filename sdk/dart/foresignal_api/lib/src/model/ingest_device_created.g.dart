// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_device_created.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestDeviceCreatedCWProxy {
  IngestDeviceCreated id(String id);

  IngestDeviceCreated sourceId(String sourceId);

  IngestDeviceCreated externalId(String externalId);

  IngestDeviceCreated name(String name);

  IngestDeviceCreated keyPrefix(String keyPrefix);

  IngestDeviceCreated createdAt(DateTime createdAt);

  IngestDeviceCreated lastSeenAt(DateTime? lastSeenAt);

  IngestDeviceCreated revokedAt(DateTime? revokedAt);

  IngestDeviceCreated key(String key);

  IngestDeviceCreated setup(IngestDeviceSetup setup);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestDeviceCreated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestDeviceCreated(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestDeviceCreated call({
    String id,
    String sourceId,
    String externalId,
    String name,
    String keyPrefix,
    DateTime createdAt,
    DateTime? lastSeenAt,
    DateTime? revokedAt,
    String key,
    IngestDeviceSetup setup,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestDeviceCreated.copyWith(...)` or call `instanceOfIngestDeviceCreated.copyWith.fieldName(value)` for a single field.
class _$IngestDeviceCreatedCWProxyImpl implements _$IngestDeviceCreatedCWProxy {
  const _$IngestDeviceCreatedCWProxyImpl(this._value);

  final IngestDeviceCreated _value;

  @override
  IngestDeviceCreated id(String id) => call(id: id);

  @override
  IngestDeviceCreated sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  IngestDeviceCreated externalId(String externalId) =>
      call(externalId: externalId);

  @override
  IngestDeviceCreated name(String name) => call(name: name);

  @override
  IngestDeviceCreated keyPrefix(String keyPrefix) => call(keyPrefix: keyPrefix);

  @override
  IngestDeviceCreated createdAt(DateTime createdAt) =>
      call(createdAt: createdAt);

  @override
  IngestDeviceCreated lastSeenAt(DateTime? lastSeenAt) =>
      call(lastSeenAt: lastSeenAt);

  @override
  IngestDeviceCreated revokedAt(DateTime? revokedAt) =>
      call(revokedAt: revokedAt);

  @override
  IngestDeviceCreated key(String key) => call(key: key);

  @override
  IngestDeviceCreated setup(IngestDeviceSetup setup) => call(setup: setup);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestDeviceCreated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestDeviceCreated(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestDeviceCreated call({
    Object? id = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? keyPrefix = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? lastSeenAt = const $CopyWithPlaceholder(),
    Object? revokedAt = const $CopyWithPlaceholder(),
    Object? key = const $CopyWithPlaceholder(),
    Object? setup = const $CopyWithPlaceholder(),
  }) {
    return IngestDeviceCreated(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      keyPrefix: keyPrefix == const $CopyWithPlaceholder() || keyPrefix == null
          ? _value.keyPrefix
          // ignore: cast_nullable_to_non_nullable
          : keyPrefix as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      lastSeenAt: lastSeenAt == const $CopyWithPlaceholder()
          ? _value.lastSeenAt
          // ignore: cast_nullable_to_non_nullable
          : lastSeenAt as DateTime?,
      revokedAt: revokedAt == const $CopyWithPlaceholder()
          ? _value.revokedAt
          // ignore: cast_nullable_to_non_nullable
          : revokedAt as DateTime?,
      key: key == const $CopyWithPlaceholder() || key == null
          ? _value.key
          // ignore: cast_nullable_to_non_nullable
          : key as String,
      setup: setup == const $CopyWithPlaceholder() || setup == null
          ? _value.setup
          // ignore: cast_nullable_to_non_nullable
          : setup as IngestDeviceSetup,
    );
  }
}

extension $IngestDeviceCreatedCopyWith on IngestDeviceCreated {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestDeviceCreated.copyWith(...)` or `instanceOfIngestDeviceCreated.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestDeviceCreatedCWProxy get copyWith =>
      _$IngestDeviceCreatedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestDeviceCreated _$IngestDeviceCreatedFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IngestDeviceCreated', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'sourceId',
          'externalId',
          'name',
          'keyPrefix',
          'createdAt',
          'key',
          'setup',
        ],
      );
      final val = IngestDeviceCreated(
        id: $checkedConvert('id', (v) => v as String),
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        externalId: $checkedConvert('externalId', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String),
        keyPrefix: $checkedConvert('keyPrefix', (v) => v as String),
        createdAt: $checkedConvert(
          'createdAt',
          (v) => DateTime.parse(v as String),
        ),
        lastSeenAt: $checkedConvert(
          'lastSeenAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        revokedAt: $checkedConvert(
          'revokedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        key: $checkedConvert('key', (v) => v as String),
        setup: $checkedConvert(
          'setup',
          (v) => IngestDeviceSetup.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$IngestDeviceCreatedToJson(
  IngestDeviceCreated instance,
) => <String, dynamic>{
  'id': instance.id,
  'sourceId': instance.sourceId,
  'externalId': instance.externalId,
  'name': instance.name,
  'keyPrefix': instance.keyPrefix,
  'createdAt': instance.createdAt.toIso8601String(),
  'lastSeenAt': ?instance.lastSeenAt?.toIso8601String(),
  'revokedAt': ?instance.revokedAt?.toIso8601String(),
  'key': instance.key,
  'setup': instance.setup.toJson(),
};
