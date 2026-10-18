// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'recipe_test_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecipeTestResultCWProxy {
  RecipeTestResult recipeId(String recipeId);

  RecipeTestResult recipeVersion(int? recipeVersion);

  RecipeTestResult fromSeq(int fromSeq);

  RecipeTestResult toSeq(int toSeq);

  RecipeTestResult events(List<IntelligenceEvent> events);

  RecipeTestResult truncated(bool? truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecipeTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecipeTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  RecipeTestResult call({
    String recipeId,
    int? recipeVersion,
    int fromSeq,
    int toSeq,
    List<IntelligenceEvent> events,
    bool? truncated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecipeTestResult.copyWith(...)` or call `instanceOfRecipeTestResult.copyWith.fieldName(value)` for a single field.
class _$RecipeTestResultCWProxyImpl implements _$RecipeTestResultCWProxy {
  const _$RecipeTestResultCWProxyImpl(this._value);

  final RecipeTestResult _value;

  @override
  RecipeTestResult recipeId(String recipeId) => call(recipeId: recipeId);

  @override
  RecipeTestResult recipeVersion(int? recipeVersion) =>
      call(recipeVersion: recipeVersion);

  @override
  RecipeTestResult fromSeq(int fromSeq) => call(fromSeq: fromSeq);

  @override
  RecipeTestResult toSeq(int toSeq) => call(toSeq: toSeq);

  @override
  RecipeTestResult events(List<IntelligenceEvent> events) =>
      call(events: events);

  @override
  RecipeTestResult truncated(bool? truncated) => call(truncated: truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecipeTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecipeTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecipeTestResult call({
    Object? recipeId = const $CopyWithPlaceholder(),
    Object? recipeVersion = const $CopyWithPlaceholder(),
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
    Object? events = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
  }) {
    return RecipeTestResult(
      recipeId: recipeId == const $CopyWithPlaceholder() || recipeId == null
          ? _value.recipeId
          // ignore: cast_nullable_to_non_nullable
          : recipeId as String,
      recipeVersion: recipeVersion == const $CopyWithPlaceholder()
          ? _value.recipeVersion
          // ignore: cast_nullable_to_non_nullable
          : recipeVersion as int?,
      fromSeq: fromSeq == const $CopyWithPlaceholder() || fromSeq == null
          ? _value.fromSeq
          // ignore: cast_nullable_to_non_nullable
          : fromSeq as int,
      toSeq: toSeq == const $CopyWithPlaceholder() || toSeq == null
          ? _value.toSeq
          // ignore: cast_nullable_to_non_nullable
          : toSeq as int,
      events: events == const $CopyWithPlaceholder() || events == null
          ? _value.events
          // ignore: cast_nullable_to_non_nullable
          : events as List<IntelligenceEvent>,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
    );
  }
}

extension $RecipeTestResultCopyWith on RecipeTestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecipeTestResult.copyWith(...)` or `instanceOfRecipeTestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecipeTestResultCWProxy get copyWith => _$RecipeTestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecipeTestResult _$RecipeTestResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RecipeTestResult', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['recipeId', 'fromSeq', 'toSeq', 'events'],
      );
      final val = RecipeTestResult(
        recipeId: $checkedConvert('recipeId', (v) => v as String),
        recipeVersion: $checkedConvert(
          'recipeVersion',
          (v) => (v as num?)?.toInt(),
        ),
        fromSeq: $checkedConvert('fromSeq', (v) => (v as num).toInt()),
        toSeq: $checkedConvert('toSeq', (v) => (v as num).toInt()),
        events: $checkedConvert(
          'events',
          (v) => (v as List<dynamic>)
              .map((e) => IntelligenceEvent.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        truncated: $checkedConvert('truncated', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$RecipeTestResultToJson(RecipeTestResult instance) =>
    <String, dynamic>{
      'recipeId': instance.recipeId,
      'recipeVersion': ?instance.recipeVersion,
      'fromSeq': instance.fromSeq,
      'toSeq': instance.toSeq,
      'events': instance.events.map((e) => e.toJson()).toList(),
      'truncated': ?instance.truncated,
    };
