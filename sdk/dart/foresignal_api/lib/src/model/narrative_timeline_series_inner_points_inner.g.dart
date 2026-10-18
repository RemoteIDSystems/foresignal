// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'narrative_timeline_series_inner_points_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NarrativeTimelineSeriesInnerPointsInnerCWProxy {
  NarrativeTimelineSeriesInnerPointsInner t(DateTime t);

  NarrativeTimelineSeriesInnerPointsInner posts(int posts);

  NarrativeTimelineSeriesInnerPointsInner uniqueAuthors(int uniqueAuthors);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativeTimelineSeriesInnerPointsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativeTimelineSeriesInnerPointsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  NarrativeTimelineSeriesInnerPointsInner call({
    DateTime t,
    int posts,
    int uniqueAuthors,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNarrativeTimelineSeriesInnerPointsInner.copyWith(...)` or call `instanceOfNarrativeTimelineSeriesInnerPointsInner.copyWith.fieldName(value)` for a single field.
class _$NarrativeTimelineSeriesInnerPointsInnerCWProxyImpl
    implements _$NarrativeTimelineSeriesInnerPointsInnerCWProxy {
  const _$NarrativeTimelineSeriesInnerPointsInnerCWProxyImpl(this._value);

  final NarrativeTimelineSeriesInnerPointsInner _value;

  @override
  NarrativeTimelineSeriesInnerPointsInner t(DateTime t) => call(t: t);

  @override
  NarrativeTimelineSeriesInnerPointsInner posts(int posts) =>
      call(posts: posts);

  @override
  NarrativeTimelineSeriesInnerPointsInner uniqueAuthors(int uniqueAuthors) =>
      call(uniqueAuthors: uniqueAuthors);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativeTimelineSeriesInnerPointsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativeTimelineSeriesInnerPointsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NarrativeTimelineSeriesInnerPointsInner call({
    Object? t = const $CopyWithPlaceholder(),
    Object? posts = const $CopyWithPlaceholder(),
    Object? uniqueAuthors = const $CopyWithPlaceholder(),
  }) {
    return NarrativeTimelineSeriesInnerPointsInner(
      t: t == const $CopyWithPlaceholder() || t == null
          ? _value.t
          // ignore: cast_nullable_to_non_nullable
          : t as DateTime,
      posts: posts == const $CopyWithPlaceholder() || posts == null
          ? _value.posts
          // ignore: cast_nullable_to_non_nullable
          : posts as int,
      uniqueAuthors:
          uniqueAuthors == const $CopyWithPlaceholder() || uniqueAuthors == null
          ? _value.uniqueAuthors
          // ignore: cast_nullable_to_non_nullable
          : uniqueAuthors as int,
    );
  }
}

extension $NarrativeTimelineSeriesInnerPointsInnerCopyWith
    on NarrativeTimelineSeriesInnerPointsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNarrativeTimelineSeriesInnerPointsInner.copyWith(...)` or `instanceOfNarrativeTimelineSeriesInnerPointsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NarrativeTimelineSeriesInnerPointsInnerCWProxy get copyWith =>
      _$NarrativeTimelineSeriesInnerPointsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NarrativeTimelineSeriesInnerPointsInner
_$NarrativeTimelineSeriesInnerPointsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NarrativeTimelineSeriesInnerPointsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['t', 'posts', 'uniqueAuthors']);
      final val = NarrativeTimelineSeriesInnerPointsInner(
        t: $checkedConvert('t', (v) => DateTime.parse(v as String)),
        posts: $checkedConvert('posts', (v) => (v as num).toInt()),
        uniqueAuthors: $checkedConvert(
          'uniqueAuthors',
          (v) => (v as num).toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$NarrativeTimelineSeriesInnerPointsInnerToJson(
  NarrativeTimelineSeriesInnerPointsInner instance,
) => <String, dynamic>{
  't': instance.t.toIso8601String(),
  'posts': instance.posts,
  'uniqueAuthors': instance.uniqueAuthors,
};
