// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'media_match.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MediaMatchCWProxy {
  MediaMatch fingerprint(MediaFingerprint fingerprint);

  MediaMatch distance(int distance);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaMatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaMatch(...).copyWith(id: 12, name: "My name")
  /// ```
  MediaMatch call({MediaFingerprint fingerprint, int distance});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMediaMatch.copyWith(...)` or call `instanceOfMediaMatch.copyWith.fieldName(value)` for a single field.
class _$MediaMatchCWProxyImpl implements _$MediaMatchCWProxy {
  const _$MediaMatchCWProxyImpl(this._value);

  final MediaMatch _value;

  @override
  MediaMatch fingerprint(MediaFingerprint fingerprint) =>
      call(fingerprint: fingerprint);

  @override
  MediaMatch distance(int distance) => call(distance: distance);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaMatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaMatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MediaMatch call({
    Object? fingerprint = const $CopyWithPlaceholder(),
    Object? distance = const $CopyWithPlaceholder(),
  }) {
    return MediaMatch(
      fingerprint:
          fingerprint == const $CopyWithPlaceholder() || fingerprint == null
          ? _value.fingerprint
          // ignore: cast_nullable_to_non_nullable
          : fingerprint as MediaFingerprint,
      distance: distance == const $CopyWithPlaceholder() || distance == null
          ? _value.distance
          // ignore: cast_nullable_to_non_nullable
          : distance as int,
    );
  }
}

extension $MediaMatchCopyWith on MediaMatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMediaMatch.copyWith(...)` or `instanceOfMediaMatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MediaMatchCWProxy get copyWith => _$MediaMatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MediaMatch _$MediaMatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MediaMatch', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['fingerprint', 'distance']);
      final val = MediaMatch(
        fingerprint: $checkedConvert(
          'fingerprint',
          (v) => MediaFingerprint.fromJson(v as Map<String, dynamic>),
        ),
        distance: $checkedConvert('distance', (v) => (v as num).toInt()),
      );
      return val;
    });

Map<String, dynamic> _$MediaMatchToJson(MediaMatch instance) =>
    <String, dynamic>{
      'fingerprint': instance.fingerprint.toJson(),
      'distance': instance.distance,
    };
