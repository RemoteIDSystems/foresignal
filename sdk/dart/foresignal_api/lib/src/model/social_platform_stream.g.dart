// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_platform_stream.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPlatformStreamCWProxy {
  SocialPlatformStream lagSeconds(num? lagSeconds);

  SocialPlatformStream cursor(String? cursor);

  SocialPlatformStream connectedSince(DateTime? connectedSince);

  SocialPlatformStream lastGap(SocialPlatformStreamLastGap? lastGap);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformStream(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformStream(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPlatformStream call({
    num? lagSeconds,
    String? cursor,
    DateTime? connectedSince,
    SocialPlatformStreamLastGap? lastGap,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPlatformStream.copyWith(...)` or call `instanceOfSocialPlatformStream.copyWith.fieldName(value)` for a single field.
class _$SocialPlatformStreamCWProxyImpl
    implements _$SocialPlatformStreamCWProxy {
  const _$SocialPlatformStreamCWProxyImpl(this._value);

  final SocialPlatformStream _value;

  @override
  SocialPlatformStream lagSeconds(num? lagSeconds) =>
      call(lagSeconds: lagSeconds);

  @override
  SocialPlatformStream cursor(String? cursor) => call(cursor: cursor);

  @override
  SocialPlatformStream connectedSince(DateTime? connectedSince) =>
      call(connectedSince: connectedSince);

  @override
  SocialPlatformStream lastGap(SocialPlatformStreamLastGap? lastGap) =>
      call(lastGap: lastGap);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformStream(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformStream(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPlatformStream call({
    Object? lagSeconds = const $CopyWithPlaceholder(),
    Object? cursor = const $CopyWithPlaceholder(),
    Object? connectedSince = const $CopyWithPlaceholder(),
    Object? lastGap = const $CopyWithPlaceholder(),
  }) {
    return SocialPlatformStream(
      lagSeconds: lagSeconds == const $CopyWithPlaceholder()
          ? _value.lagSeconds
          // ignore: cast_nullable_to_non_nullable
          : lagSeconds as num?,
      cursor: cursor == const $CopyWithPlaceholder()
          ? _value.cursor
          // ignore: cast_nullable_to_non_nullable
          : cursor as String?,
      connectedSince: connectedSince == const $CopyWithPlaceholder()
          ? _value.connectedSince
          // ignore: cast_nullable_to_non_nullable
          : connectedSince as DateTime?,
      lastGap: lastGap == const $CopyWithPlaceholder()
          ? _value.lastGap
          // ignore: cast_nullable_to_non_nullable
          : lastGap as SocialPlatformStreamLastGap?,
    );
  }
}

extension $SocialPlatformStreamCopyWith on SocialPlatformStream {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPlatformStream.copyWith(...)` or `instanceOfSocialPlatformStream.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPlatformStreamCWProxy get copyWith =>
      _$SocialPlatformStreamCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPlatformStream _$SocialPlatformStreamFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialPlatformStream', json, ($checkedConvert) {
  final val = SocialPlatformStream(
    lagSeconds: $checkedConvert('lagSeconds', (v) => v as num?),
    cursor: $checkedConvert('cursor', (v) => v as String?),
    connectedSince: $checkedConvert(
      'connectedSince',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastGap: $checkedConvert(
      'lastGap',
      (v) => v == null
          ? null
          : SocialPlatformStreamLastGap.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$SocialPlatformStreamToJson(
  SocialPlatformStream instance,
) => <String, dynamic>{
  'lagSeconds': ?instance.lagSeconds,
  'cursor': ?instance.cursor,
  'connectedSince': ?instance.connectedSince?.toIso8601String(),
  'lastGap': ?instance.lastGap?.toJson(),
};
