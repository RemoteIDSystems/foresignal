// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_evaluation_profiles_inner_tokens_per_day_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystEvaluationProfilesInnerTokensPerDayInnerCWProxy {
  AnalystEvaluationProfilesInnerTokensPerDayInner date(DateTime date);

  AnalystEvaluationProfilesInnerTokensPerDayInner tokens(int tokens);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystEvaluationProfilesInnerTokensPerDayInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystEvaluationProfilesInnerTokensPerDayInner(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystEvaluationProfilesInnerTokensPerDayInner call({
    DateTime date,
    int tokens,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystEvaluationProfilesInnerTokensPerDayInner.copyWith(...)` or call `instanceOfAnalystEvaluationProfilesInnerTokensPerDayInner.copyWith.fieldName(value)` for a single field.
class _$AnalystEvaluationProfilesInnerTokensPerDayInnerCWProxyImpl
    implements _$AnalystEvaluationProfilesInnerTokensPerDayInnerCWProxy {
  const _$AnalystEvaluationProfilesInnerTokensPerDayInnerCWProxyImpl(
    this._value,
  );

  final AnalystEvaluationProfilesInnerTokensPerDayInner _value;

  @override
  AnalystEvaluationProfilesInnerTokensPerDayInner date(DateTime date) =>
      call(date: date);

  @override
  AnalystEvaluationProfilesInnerTokensPerDayInner tokens(int tokens) =>
      call(tokens: tokens);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystEvaluationProfilesInnerTokensPerDayInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystEvaluationProfilesInnerTokensPerDayInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystEvaluationProfilesInnerTokensPerDayInner call({
    Object? date = const $CopyWithPlaceholder(),
    Object? tokens = const $CopyWithPlaceholder(),
  }) {
    return AnalystEvaluationProfilesInnerTokensPerDayInner(
      date: date == const $CopyWithPlaceholder() || date == null
          ? _value.date
          // ignore: cast_nullable_to_non_nullable
          : date as DateTime,
      tokens: tokens == const $CopyWithPlaceholder() || tokens == null
          ? _value.tokens
          // ignore: cast_nullable_to_non_nullable
          : tokens as int,
    );
  }
}

extension $AnalystEvaluationProfilesInnerTokensPerDayInnerCopyWith
    on AnalystEvaluationProfilesInnerTokensPerDayInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystEvaluationProfilesInnerTokensPerDayInner.copyWith(...)` or `instanceOfAnalystEvaluationProfilesInnerTokensPerDayInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystEvaluationProfilesInnerTokensPerDayInnerCWProxy get copyWith =>
      _$AnalystEvaluationProfilesInnerTokensPerDayInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystEvaluationProfilesInnerTokensPerDayInner
_$AnalystEvaluationProfilesInnerTokensPerDayInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalystEvaluationProfilesInnerTokensPerDayInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['date', 'tokens']);
  final val = AnalystEvaluationProfilesInnerTokensPerDayInner(
    date: $checkedConvert('date', (v) => DateTime.parse(v as String)),
    tokens: $checkedConvert('tokens', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$AnalystEvaluationProfilesInnerTokensPerDayInnerToJson(
  AnalystEvaluationProfilesInnerTokensPerDayInner instance,
) => <String, dynamic>{
  'date': instance.date.toIso8601String(),
  'tokens': instance.tokens,
};
