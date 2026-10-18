// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'remote_media_meta.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RemoteMediaMetaCWProxy {
  RemoteMediaMeta passId(String? passId);

  RemoteMediaMeta composite(String composite);

  RemoteMediaMeta timestamp(DateTime timestamp);

  RemoteMediaMeta satellite(String satellite);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RemoteMediaMeta(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RemoteMediaMeta(...).copyWith(id: 12, name: "My name")
  /// ```
  RemoteMediaMeta call({
    String? passId,
    String composite,
    DateTime timestamp,
    String satellite,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRemoteMediaMeta.copyWith(...)` or call `instanceOfRemoteMediaMeta.copyWith.fieldName(value)` for a single field.
class _$RemoteMediaMetaCWProxyImpl implements _$RemoteMediaMetaCWProxy {
  const _$RemoteMediaMetaCWProxyImpl(this._value);

  final RemoteMediaMeta _value;

  @override
  RemoteMediaMeta passId(String? passId) => call(passId: passId);

  @override
  RemoteMediaMeta composite(String composite) => call(composite: composite);

  @override
  RemoteMediaMeta timestamp(DateTime timestamp) => call(timestamp: timestamp);

  @override
  RemoteMediaMeta satellite(String satellite) => call(satellite: satellite);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RemoteMediaMeta(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RemoteMediaMeta(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RemoteMediaMeta call({
    Object? passId = const $CopyWithPlaceholder(),
    Object? composite = const $CopyWithPlaceholder(),
    Object? timestamp = const $CopyWithPlaceholder(),
    Object? satellite = const $CopyWithPlaceholder(),
  }) {
    return RemoteMediaMeta(
      passId: passId == const $CopyWithPlaceholder()
          ? _value.passId
          // ignore: cast_nullable_to_non_nullable
          : passId as String?,
      composite: composite == const $CopyWithPlaceholder() || composite == null
          ? _value.composite
          // ignore: cast_nullable_to_non_nullable
          : composite as String,
      timestamp: timestamp == const $CopyWithPlaceholder() || timestamp == null
          ? _value.timestamp
          // ignore: cast_nullable_to_non_nullable
          : timestamp as DateTime,
      satellite: satellite == const $CopyWithPlaceholder() || satellite == null
          ? _value.satellite
          // ignore: cast_nullable_to_non_nullable
          : satellite as String,
    );
  }
}

extension $RemoteMediaMetaCopyWith on RemoteMediaMeta {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRemoteMediaMeta.copyWith(...)` or `instanceOfRemoteMediaMeta.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RemoteMediaMetaCWProxy get copyWith => _$RemoteMediaMetaCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RemoteMediaMeta _$RemoteMediaMetaFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RemoteMediaMeta', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['composite', 'timestamp', 'satellite']);
  final val = RemoteMediaMeta(
    passId: $checkedConvert('passId', (v) => v as String?),
    composite: $checkedConvert('composite', (v) => v as String),
    timestamp: $checkedConvert('timestamp', (v) => DateTime.parse(v as String)),
    satellite: $checkedConvert('satellite', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$RemoteMediaMetaToJson(RemoteMediaMeta instance) =>
    <String, dynamic>{
      'passId': ?instance.passId,
      'composite': instance.composite,
      'timestamp': instance.timestamp.toIso8601String(),
      'satellite': instance.satellite,
    };
