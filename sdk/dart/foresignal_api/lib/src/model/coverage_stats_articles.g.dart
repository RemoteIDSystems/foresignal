// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'coverage_stats_articles.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CoverageStatsArticlesCWProxy {
  CoverageStatsArticles left(int left);

  CoverageStatsArticles center(int center);

  CoverageStatsArticles right(int right);

  CoverageStatsArticles unrated(int unrated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CoverageStatsArticles(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CoverageStatsArticles(...).copyWith(id: 12, name: "My name")
  /// ```
  CoverageStatsArticles call({int left, int center, int right, int unrated});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCoverageStatsArticles.copyWith(...)` or call `instanceOfCoverageStatsArticles.copyWith.fieldName(value)` for a single field.
class _$CoverageStatsArticlesCWProxyImpl
    implements _$CoverageStatsArticlesCWProxy {
  const _$CoverageStatsArticlesCWProxyImpl(this._value);

  final CoverageStatsArticles _value;

  @override
  CoverageStatsArticles left(int left) => call(left: left);

  @override
  CoverageStatsArticles center(int center) => call(center: center);

  @override
  CoverageStatsArticles right(int right) => call(right: right);

  @override
  CoverageStatsArticles unrated(int unrated) => call(unrated: unrated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CoverageStatsArticles(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CoverageStatsArticles(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CoverageStatsArticles call({
    Object? left = const $CopyWithPlaceholder(),
    Object? center = const $CopyWithPlaceholder(),
    Object? right = const $CopyWithPlaceholder(),
    Object? unrated = const $CopyWithPlaceholder(),
  }) {
    return CoverageStatsArticles(
      left: left == const $CopyWithPlaceholder() || left == null
          ? _value.left
          // ignore: cast_nullable_to_non_nullable
          : left as int,
      center: center == const $CopyWithPlaceholder() || center == null
          ? _value.center
          // ignore: cast_nullable_to_non_nullable
          : center as int,
      right: right == const $CopyWithPlaceholder() || right == null
          ? _value.right
          // ignore: cast_nullable_to_non_nullable
          : right as int,
      unrated: unrated == const $CopyWithPlaceholder() || unrated == null
          ? _value.unrated
          // ignore: cast_nullable_to_non_nullable
          : unrated as int,
    );
  }
}

extension $CoverageStatsArticlesCopyWith on CoverageStatsArticles {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCoverageStatsArticles.copyWith(...)` or `instanceOfCoverageStatsArticles.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CoverageStatsArticlesCWProxy get copyWith =>
      _$CoverageStatsArticlesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CoverageStatsArticles _$CoverageStatsArticlesFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CoverageStatsArticles', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['left', 'center', 'right', 'unrated']);
  final val = CoverageStatsArticles(
    left: $checkedConvert('left', (v) => (v as num).toInt()),
    center: $checkedConvert('center', (v) => (v as num).toInt()),
    right: $checkedConvert('right', (v) => (v as num).toInt()),
    unrated: $checkedConvert('unrated', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$CoverageStatsArticlesToJson(
  CoverageStatsArticles instance,
) => <String, dynamic>{
  'left': instance.left,
  'center': instance.center,
  'right': instance.right,
  'unrated': instance.unrated,
};
