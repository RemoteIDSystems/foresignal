// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'narrative_platform_stats.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NarrativePlatformStatsCWProxy {
  NarrativePlatformStats platform(String platform);

  NarrativePlatformStats posts(int posts);

  NarrativePlatformStats uniqueAuthors(int uniqueAuthors);

  NarrativePlatformStats firstObservedAt(DateTime firstObservedAt);

  NarrativePlatformStats earliestPostRef(String earliestPostRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativePlatformStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativePlatformStats(...).copyWith(id: 12, name: "My name")
  /// ```
  NarrativePlatformStats call({
    String platform,
    int posts,
    int uniqueAuthors,
    DateTime firstObservedAt,
    String earliestPostRef,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNarrativePlatformStats.copyWith(...)` or call `instanceOfNarrativePlatformStats.copyWith.fieldName(value)` for a single field.
class _$NarrativePlatformStatsCWProxyImpl
    implements _$NarrativePlatformStatsCWProxy {
  const _$NarrativePlatformStatsCWProxyImpl(this._value);

  final NarrativePlatformStats _value;

  @override
  NarrativePlatformStats platform(String platform) => call(platform: platform);

  @override
  NarrativePlatformStats posts(int posts) => call(posts: posts);

  @override
  NarrativePlatformStats uniqueAuthors(int uniqueAuthors) =>
      call(uniqueAuthors: uniqueAuthors);

  @override
  NarrativePlatformStats firstObservedAt(DateTime firstObservedAt) =>
      call(firstObservedAt: firstObservedAt);

  @override
  NarrativePlatformStats earliestPostRef(String earliestPostRef) =>
      call(earliestPostRef: earliestPostRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativePlatformStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativePlatformStats(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NarrativePlatformStats call({
    Object? platform = const $CopyWithPlaceholder(),
    Object? posts = const $CopyWithPlaceholder(),
    Object? uniqueAuthors = const $CopyWithPlaceholder(),
    Object? firstObservedAt = const $CopyWithPlaceholder(),
    Object? earliestPostRef = const $CopyWithPlaceholder(),
  }) {
    return NarrativePlatformStats(
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String,
      posts: posts == const $CopyWithPlaceholder() || posts == null
          ? _value.posts
          // ignore: cast_nullable_to_non_nullable
          : posts as int,
      uniqueAuthors:
          uniqueAuthors == const $CopyWithPlaceholder() || uniqueAuthors == null
          ? _value.uniqueAuthors
          // ignore: cast_nullable_to_non_nullable
          : uniqueAuthors as int,
      firstObservedAt:
          firstObservedAt == const $CopyWithPlaceholder() ||
              firstObservedAt == null
          ? _value.firstObservedAt
          // ignore: cast_nullable_to_non_nullable
          : firstObservedAt as DateTime,
      earliestPostRef:
          earliestPostRef == const $CopyWithPlaceholder() ||
              earliestPostRef == null
          ? _value.earliestPostRef
          // ignore: cast_nullable_to_non_nullable
          : earliestPostRef as String,
    );
  }
}

extension $NarrativePlatformStatsCopyWith on NarrativePlatformStats {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNarrativePlatformStats.copyWith(...)` or `instanceOfNarrativePlatformStats.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NarrativePlatformStatsCWProxy get copyWith =>
      _$NarrativePlatformStatsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NarrativePlatformStats _$NarrativePlatformStatsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NarrativePlatformStats', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'platform',
      'posts',
      'uniqueAuthors',
      'firstObservedAt',
      'earliestPostRef',
    ],
  );
  final val = NarrativePlatformStats(
    platform: $checkedConvert('platform', (v) => v as String),
    posts: $checkedConvert('posts', (v) => (v as num).toInt()),
    uniqueAuthors: $checkedConvert('uniqueAuthors', (v) => (v as num).toInt()),
    firstObservedAt: $checkedConvert(
      'firstObservedAt',
      (v) => DateTime.parse(v as String),
    ),
    earliestPostRef: $checkedConvert('earliestPostRef', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$NarrativePlatformStatsToJson(
  NarrativePlatformStats instance,
) => <String, dynamic>{
  'platform': instance.platform,
  'posts': instance.posts,
  'uniqueAuthors': instance.uniqueAuthors,
  'firstObservedAt': instance.firstObservedAt.toIso8601String(),
  'earliestPostRef': instance.earliestPostRef,
};
