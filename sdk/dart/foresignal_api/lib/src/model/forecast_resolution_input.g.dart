// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_resolution_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastResolutionInputCWProxy {
  ForecastResolutionInput outcome(String? outcome);

  ForecastResolutionInput value(num? value);

  ForecastResolutionInput evidence(List<String> evidence);

  ForecastResolutionInput note(String? note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastResolutionInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastResolutionInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastResolutionInput call({
    String? outcome,
    num? value,
    List<String> evidence,
    String? note,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastResolutionInput.copyWith(...)` or call `instanceOfForecastResolutionInput.copyWith.fieldName(value)` for a single field.
class _$ForecastResolutionInputCWProxyImpl
    implements _$ForecastResolutionInputCWProxy {
  const _$ForecastResolutionInputCWProxyImpl(this._value);

  final ForecastResolutionInput _value;

  @override
  ForecastResolutionInput outcome(String? outcome) => call(outcome: outcome);

  @override
  ForecastResolutionInput value(num? value) => call(value: value);

  @override
  ForecastResolutionInput evidence(List<String> evidence) =>
      call(evidence: evidence);

  @override
  ForecastResolutionInput note(String? note) => call(note: note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastResolutionInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastResolutionInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastResolutionInput call({
    Object? outcome = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
  }) {
    return ForecastResolutionInput(
      outcome: outcome == const $CopyWithPlaceholder()
          ? _value.outcome
          // ignore: cast_nullable_to_non_nullable
          : outcome as String?,
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num?,
      evidence: evidence == const $CopyWithPlaceholder() || evidence == null
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<String>,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
    );
  }
}

extension $ForecastResolutionInputCopyWith on ForecastResolutionInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastResolutionInput.copyWith(...)` or `instanceOfForecastResolutionInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastResolutionInputCWProxy get copyWith =>
      _$ForecastResolutionInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastResolutionInput _$ForecastResolutionInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastResolutionInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['evidence']);
  final val = ForecastResolutionInput(
    outcome: $checkedConvert('outcome', (v) => v as String?),
    value: $checkedConvert('value', (v) => v as num?),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    note: $checkedConvert('note', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ForecastResolutionInputToJson(
  ForecastResolutionInput instance,
) => <String, dynamic>{
  'outcome': ?instance.outcome,
  'value': ?instance.value,
  'evidence': instance.evidence,
  'note': ?instance.note,
};
