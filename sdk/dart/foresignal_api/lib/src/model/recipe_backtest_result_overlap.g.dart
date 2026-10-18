// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'recipe_backtest_result_overlap.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecipeBacktestResultOverlapCWProxy {
  RecipeBacktestResultOverlap matched(int? matched);

  RecipeBacktestResultOverlap onlyBacktest(int? onlyBacktest);

  RecipeBacktestResultOverlap onlyActual(int? onlyActual);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecipeBacktestResultOverlap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecipeBacktestResultOverlap(...).copyWith(id: 12, name: "My name")
  /// ```
  RecipeBacktestResultOverlap call({
    int? matched,
    int? onlyBacktest,
    int? onlyActual,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecipeBacktestResultOverlap.copyWith(...)` or call `instanceOfRecipeBacktestResultOverlap.copyWith.fieldName(value)` for a single field.
class _$RecipeBacktestResultOverlapCWProxyImpl
    implements _$RecipeBacktestResultOverlapCWProxy {
  const _$RecipeBacktestResultOverlapCWProxyImpl(this._value);

  final RecipeBacktestResultOverlap _value;

  @override
  RecipeBacktestResultOverlap matched(int? matched) => call(matched: matched);

  @override
  RecipeBacktestResultOverlap onlyBacktest(int? onlyBacktest) =>
      call(onlyBacktest: onlyBacktest);

  @override
  RecipeBacktestResultOverlap onlyActual(int? onlyActual) =>
      call(onlyActual: onlyActual);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecipeBacktestResultOverlap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecipeBacktestResultOverlap(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecipeBacktestResultOverlap call({
    Object? matched = const $CopyWithPlaceholder(),
    Object? onlyBacktest = const $CopyWithPlaceholder(),
    Object? onlyActual = const $CopyWithPlaceholder(),
  }) {
    return RecipeBacktestResultOverlap(
      matched: matched == const $CopyWithPlaceholder()
          ? _value.matched
          // ignore: cast_nullable_to_non_nullable
          : matched as int?,
      onlyBacktest: onlyBacktest == const $CopyWithPlaceholder()
          ? _value.onlyBacktest
          // ignore: cast_nullable_to_non_nullable
          : onlyBacktest as int?,
      onlyActual: onlyActual == const $CopyWithPlaceholder()
          ? _value.onlyActual
          // ignore: cast_nullable_to_non_nullable
          : onlyActual as int?,
    );
  }
}

extension $RecipeBacktestResultOverlapCopyWith on RecipeBacktestResultOverlap {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecipeBacktestResultOverlap.copyWith(...)` or `instanceOfRecipeBacktestResultOverlap.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecipeBacktestResultOverlapCWProxy get copyWith =>
      _$RecipeBacktestResultOverlapCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecipeBacktestResultOverlap _$RecipeBacktestResultOverlapFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RecipeBacktestResultOverlap', json, ($checkedConvert) {
  final val = RecipeBacktestResultOverlap(
    matched: $checkedConvert('matched', (v) => (v as num?)?.toInt()),
    onlyBacktest: $checkedConvert('onlyBacktest', (v) => (v as num?)?.toInt()),
    onlyActual: $checkedConvert('onlyActual', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$RecipeBacktestResultOverlapToJson(
  RecipeBacktestResultOverlap instance,
) => <String, dynamic>{
  'matched': ?instance.matched,
  'onlyBacktest': ?instance.onlyBacktest,
  'onlyActual': ?instance.onlyActual,
};
