// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_coverage_bucket.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialCoverageBucketCWProxy {
  SocialCoverageBucket hour(DateTime hour);

  SocialCoverageBucket postsSeen(int postsSeen);

  SocialCoverageBucket denominator(int? denominator);

  SocialCoverageBucket complete(bool complete);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCoverageBucket(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCoverageBucket(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialCoverageBucket call({
    DateTime hour,
    int postsSeen,
    int? denominator,
    bool complete,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialCoverageBucket.copyWith(...)` or call `instanceOfSocialCoverageBucket.copyWith.fieldName(value)` for a single field.
class _$SocialCoverageBucketCWProxyImpl
    implements _$SocialCoverageBucketCWProxy {
  const _$SocialCoverageBucketCWProxyImpl(this._value);

  final SocialCoverageBucket _value;

  @override
  SocialCoverageBucket hour(DateTime hour) => call(hour: hour);

  @override
  SocialCoverageBucket postsSeen(int postsSeen) => call(postsSeen: postsSeen);

  @override
  SocialCoverageBucket denominator(int? denominator) =>
      call(denominator: denominator);

  @override
  SocialCoverageBucket complete(bool complete) => call(complete: complete);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCoverageBucket(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCoverageBucket(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialCoverageBucket call({
    Object? hour = const $CopyWithPlaceholder(),
    Object? postsSeen = const $CopyWithPlaceholder(),
    Object? denominator = const $CopyWithPlaceholder(),
    Object? complete = const $CopyWithPlaceholder(),
  }) {
    return SocialCoverageBucket(
      hour: hour == const $CopyWithPlaceholder() || hour == null
          ? _value.hour
          // ignore: cast_nullable_to_non_nullable
          : hour as DateTime,
      postsSeen: postsSeen == const $CopyWithPlaceholder() || postsSeen == null
          ? _value.postsSeen
          // ignore: cast_nullable_to_non_nullable
          : postsSeen as int,
      denominator: denominator == const $CopyWithPlaceholder()
          ? _value.denominator
          // ignore: cast_nullable_to_non_nullable
          : denominator as int?,
      complete: complete == const $CopyWithPlaceholder() || complete == null
          ? _value.complete
          // ignore: cast_nullable_to_non_nullable
          : complete as bool,
    );
  }
}

extension $SocialCoverageBucketCopyWith on SocialCoverageBucket {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialCoverageBucket.copyWith(...)` or `instanceOfSocialCoverageBucket.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialCoverageBucketCWProxy get copyWith =>
      _$SocialCoverageBucketCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialCoverageBucket _$SocialCoverageBucketFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialCoverageBucket', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['hour', 'postsSeen', 'complete']);
  final val = SocialCoverageBucket(
    hour: $checkedConvert('hour', (v) => DateTime.parse(v as String)),
    postsSeen: $checkedConvert('postsSeen', (v) => (v as num).toInt()),
    denominator: $checkedConvert('denominator', (v) => (v as num?)?.toInt()),
    complete: $checkedConvert('complete', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$SocialCoverageBucketToJson(
  SocialCoverageBucket instance,
) => <String, dynamic>{
  'hour': instance.hour.toIso8601String(),
  'postsSeen': instance.postsSeen,
  'denominator': ?instance.denominator,
  'complete': instance.complete,
};
