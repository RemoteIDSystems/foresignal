// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'peer_ingest_key.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PeerIngestKeyCWProxy {
  PeerIngestKey peerId(String peerId);

  PeerIngestKey key(String key);

  PeerIngestKey keyPrefix(String keyPrefix);

  PeerIngestKey createdAt(DateTime createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PeerIngestKey(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PeerIngestKey(...).copyWith(id: 12, name: "My name")
  /// ```
  PeerIngestKey call({
    String peerId,
    String key,
    String keyPrefix,
    DateTime createdAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPeerIngestKey.copyWith(...)` or call `instanceOfPeerIngestKey.copyWith.fieldName(value)` for a single field.
class _$PeerIngestKeyCWProxyImpl implements _$PeerIngestKeyCWProxy {
  const _$PeerIngestKeyCWProxyImpl(this._value);

  final PeerIngestKey _value;

  @override
  PeerIngestKey peerId(String peerId) => call(peerId: peerId);

  @override
  PeerIngestKey key(String key) => call(key: key);

  @override
  PeerIngestKey keyPrefix(String keyPrefix) => call(keyPrefix: keyPrefix);

  @override
  PeerIngestKey createdAt(DateTime createdAt) => call(createdAt: createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PeerIngestKey(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PeerIngestKey(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PeerIngestKey call({
    Object? peerId = const $CopyWithPlaceholder(),
    Object? key = const $CopyWithPlaceholder(),
    Object? keyPrefix = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
  }) {
    return PeerIngestKey(
      peerId: peerId == const $CopyWithPlaceholder() || peerId == null
          ? _value.peerId
          // ignore: cast_nullable_to_non_nullable
          : peerId as String,
      key: key == const $CopyWithPlaceholder() || key == null
          ? _value.key
          // ignore: cast_nullable_to_non_nullable
          : key as String,
      keyPrefix: keyPrefix == const $CopyWithPlaceholder() || keyPrefix == null
          ? _value.keyPrefix
          // ignore: cast_nullable_to_non_nullable
          : keyPrefix as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
    );
  }
}

extension $PeerIngestKeyCopyWith on PeerIngestKey {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPeerIngestKey.copyWith(...)` or `instanceOfPeerIngestKey.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PeerIngestKeyCWProxy get copyWith => _$PeerIngestKeyCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PeerIngestKey _$PeerIngestKeyFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PeerIngestKey', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['peerId', 'key', 'keyPrefix', 'createdAt'],
      );
      final val = PeerIngestKey(
        peerId: $checkedConvert('peerId', (v) => v as String),
        key: $checkedConvert('key', (v) => v as String),
        keyPrefix: $checkedConvert('keyPrefix', (v) => v as String),
        createdAt: $checkedConvert(
          'createdAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PeerIngestKeyToJson(PeerIngestKey instance) =>
    <String, dynamic>{
      'peerId': instance.peerId,
      'key': instance.key,
      'keyPrefix': instance.keyPrefix,
      'createdAt': instance.createdAt.toIso8601String(),
    };
