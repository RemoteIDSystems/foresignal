// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_device.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestDeviceCWProxy {
  IngestDevice id(String id);

  IngestDevice sourceId(String sourceId);

  IngestDevice externalId(String externalId);

  IngestDevice name(String name);

  IngestDevice keyPrefix(String keyPrefix);

  IngestDevice createdAt(DateTime createdAt);

  IngestDevice lastSeenAt(DateTime? lastSeenAt);

  IngestDevice revokedAt(DateTime? revokedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestDevice(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestDevice(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestDevice call({
    String id,
    String sourceId,
    String externalId,
    String name,
    String keyPrefix,
    DateTime createdAt,
    DateTime? lastSeenAt,
    DateTime? revokedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestDevice.copyWith(...)` or call `instanceOfIngestDevice.copyWith.fieldName(value)` for a single field.
class _$IngestDeviceCWProxyImpl implements _$IngestDeviceCWProxy {
  const _$IngestDeviceCWProxyImpl(this._value);

  final IngestDevice _value;

  @override
  IngestDevice id(String id) => call(id: id);

  @override
  IngestDevice sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  IngestDevice externalId(String externalId) => call(externalId: externalId);

  @override
  IngestDevice name(String name) => call(name: name);

  @override
  IngestDevice keyPrefix(String keyPrefix) => call(keyPrefix: keyPrefix);

  @override
  IngestDevice createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  IngestDevice lastSeenAt(DateTime? lastSeenAt) => call(lastSeenAt: lastSeenAt);

  @override
  IngestDevice revokedAt(DateTime? revokedAt) => call(revokedAt: revokedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestDevice(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestDevice(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestDevice call({
    Object? id = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? keyPrefix = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? lastSeenAt = const $CopyWithPlaceholder(),
    Object? revokedAt = const $CopyWithPlaceholder(),
  }) {
    return IngestDevice(
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
    );
  }
}

extension $IngestDeviceCopyWith on IngestDevice {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestDevice.copyWith(...)` or `instanceOfIngestDevice.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestDeviceCWProxy get copyWith => _$IngestDeviceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestDevice _$IngestDeviceFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IngestDevice', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'sourceId',
          'externalId',
          'name',
          'keyPrefix',
          'createdAt',
        ],
      );
      final val = IngestDevice(
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
      );
      return val;
    });

Map<String, dynamic> _$IngestDeviceToJson(IngestDevice instance) =>
    <String, dynamic>{
      'id': instance.id,
      'sourceId': instance.sourceId,
      'externalId': instance.externalId,
      'name': instance.name,
      'keyPrefix': instance.keyPrefix,
      'createdAt': instance.createdAt.toIso8601String(),
      'lastSeenAt': ?instance.lastSeenAt?.toIso8601String(),
      'revokedAt': ?instance.revokedAt?.toIso8601String(),
    };
