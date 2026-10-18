// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'recipe_backtest_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecipeBacktestResultCWProxy {
  RecipeBacktestResult recipeId(String recipeId);

  RecipeBacktestResult recipeVersion(int? recipeVersion);

  RecipeBacktestResult events(List<RecipeBacktestResultEventsInner> events);

  RecipeBacktestResult countsByType(Map<String, int>? countsByType);

  RecipeBacktestResult overlap(RecipeBacktestResultOverlap? overlap);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecipeBacktestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecipeBacktestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  RecipeBacktestResult call({
    String recipeId,
    int? recipeVersion,
    List<RecipeBacktestResultEventsInner> events,
    Map<String, int>? countsByType,
    RecipeBacktestResultOverlap? overlap,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecipeBacktestResult.copyWith(...)` or call `instanceOfRecipeBacktestResult.copyWith.fieldName(value)` for a single field.
class _$RecipeBacktestResultCWProxyImpl
    implements _$RecipeBacktestResultCWProxy {
  const _$RecipeBacktestResultCWProxyImpl(this._value);

  final RecipeBacktestResult _value;

  @override
  RecipeBacktestResult recipeId(String recipeId) => call(recipeId: recipeId);

  @override
  RecipeBacktestResult recipeVersion(int? recipeVersion) =>
      call(recipeVersion: recipeVersion);

  @override
  RecipeBacktestResult events(List<RecipeBacktestResultEventsInner> events) =>
      call(events: events);

  @override
  RecipeBacktestResult countsByType(Map<String, int>? countsByType) =>
      call(countsByType: countsByType);

  @override
  RecipeBacktestResult overlap(RecipeBacktestResultOverlap? overlap) =>
      call(overlap: overlap);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecipeBacktestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecipeBacktestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecipeBacktestResult call({
    Object? recipeId = const $CopyWithPlaceholder(),
    Object? recipeVersion = const $CopyWithPlaceholder(),
    Object? events = const $CopyWithPlaceholder(),
    Object? countsByType = const $CopyWithPlaceholder(),
    Object? overlap = const $CopyWithPlaceholder(),
  }) {
    return RecipeBacktestResult(
      recipeId: recipeId == const $CopyWithPlaceholder() || recipeId == null
          ? _value.recipeId
          // ignore: cast_nullable_to_non_nullable
          : recipeId as String,
      recipeVersion: recipeVersion == const $CopyWithPlaceholder()
          ? _value.recipeVersion
          // ignore: cast_nullable_to_non_nullable
          : recipeVersion as int?,
      events: events == const $CopyWithPlaceholder() || events == null
          ? _value.events
          // ignore: cast_nullable_to_non_nullable
          : events as List<RecipeBacktestResultEventsInner>,
      countsByType: countsByType == const $CopyWithPlaceholder()
          ? _value.countsByType
          // ignore: cast_nullable_to_non_nullable
          : countsByType as Map<String, int>?,
      overlap: overlap == const $CopyWithPlaceholder()
          ? _value.overlap
          // ignore: cast_nullable_to_non_nullable
          : overlap as RecipeBacktestResultOverlap?,
    );
  }
}

extension $RecipeBacktestResultCopyWith on RecipeBacktestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecipeBacktestResult.copyWith(...)` or `instanceOfRecipeBacktestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecipeBacktestResultCWProxy get copyWith =>
      _$RecipeBacktestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecipeBacktestResult _$RecipeBacktestResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RecipeBacktestResult', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['recipeId', 'events']);
  final val = RecipeBacktestResult(
    recipeId: $checkedConvert('recipeId', (v) => v as String),
    recipeVersion: $checkedConvert(
      'recipeVersion',
      (v) => (v as num?)?.toInt(),
    ),
    events: $checkedConvert(
      'events',
      (v) => (v as List<dynamic>)
          .map(
            (e) => RecipeBacktestResultEventsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    countsByType: $checkedConvert(
      'countsByType',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, (e as num).toInt()),
      ),
    ),
    overlap: $checkedConvert(
      'overlap',
      (v) => v == null
          ? null
          : RecipeBacktestResultOverlap.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$RecipeBacktestResultToJson(
  RecipeBacktestResult instance,
) => <String, dynamic>{
  'recipeId': instance.recipeId,
  'recipeVersion': ?instance.recipeVersion,
  'events': instance.events.map((e) => e.toJson()).toList(),
  'countsByType': ?instance.countsByType,
  'overlap': ?instance.overlap?.toJson(),
};
