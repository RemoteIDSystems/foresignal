// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'media_fingerprint_uses_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MediaFingerprintUsesInnerCWProxy {
  MediaFingerprintUsesInner postRef(String postRef);

  MediaFingerprintUsesInner seenAt(DateTime seenAt);

  MediaFingerprintUsesInner platform(String? platform);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaFingerprintUsesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaFingerprintUsesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  MediaFingerprintUsesInner call({
    String postRef,
    DateTime seenAt,
    String? platform,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMediaFingerprintUsesInner.copyWith(...)` or call `instanceOfMediaFingerprintUsesInner.copyWith.fieldName(value)` for a single field.
class _$MediaFingerprintUsesInnerCWProxyImpl
    implements _$MediaFingerprintUsesInnerCWProxy {
  const _$MediaFingerprintUsesInnerCWProxyImpl(this._value);

  final MediaFingerprintUsesInner _value;

  @override
  MediaFingerprintUsesInner postRef(String postRef) => call(postRef: postRef);

  @override
  MediaFingerprintUsesInner seenAt(DateTime seenAt) => call(seenAt: seenAt);

  @override
  MediaFingerprintUsesInner platform(String? platform) =>
      call(platform: platform);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaFingerprintUsesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaFingerprintUsesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MediaFingerprintUsesInner call({
    Object? postRef = const $CopyWithPlaceholder(),
    Object? seenAt = const $CopyWithPlaceholder(),
    Object? platform = const $CopyWithPlaceholder(),
  }) {
    return MediaFingerprintUsesInner(
      postRef: postRef == const $CopyWithPlaceholder() || postRef == null
          ? _value.postRef
          // ignore: cast_nullable_to_non_nullable
          : postRef as String,
      seenAt: seenAt == const $CopyWithPlaceholder() || seenAt == null
          ? _value.seenAt
          // ignore: cast_nullable_to_non_nullable
          : seenAt as DateTime,
      platform: platform == const $CopyWithPlaceholder()
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String?,
    );
  }
}

extension $MediaFingerprintUsesInnerCopyWith on MediaFingerprintUsesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMediaFingerprintUsesInner.copyWith(...)` or `instanceOfMediaFingerprintUsesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MediaFingerprintUsesInnerCWProxy get copyWith =>
      _$MediaFingerprintUsesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MediaFingerprintUsesInner _$MediaFingerprintUsesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MediaFingerprintUsesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['postRef', 'seenAt']);
  final val = MediaFingerprintUsesInner(
    postRef: $checkedConvert('postRef', (v) => v as String),
    seenAt: $checkedConvert('seenAt', (v) => DateTime.parse(v as String)),
    platform: $checkedConvert('platform', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$MediaFingerprintUsesInnerToJson(
  MediaFingerprintUsesInner instance,
) => <String, dynamic>{
  'postRef': instance.postRef,
  'seenAt': instance.seenAt.toIso8601String(),
  'platform': ?instance.platform,
};
