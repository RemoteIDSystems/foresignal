// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_evaluation_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastEvaluationRequestCWProxy {
  ForecastEvaluationRequest forecasterId(String forecasterId);

  ForecastEvaluationRequest from(DateTime from);

  ForecastEvaluationRequest to(DateTime to);

  ForecastEvaluationRequest categories(List<String>? categories);

  ForecastEvaluationRequest maxQuestions(int? maxQuestions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastEvaluationRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastEvaluationRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastEvaluationRequest call({
    String forecasterId,
    DateTime from,
    DateTime to,
    List<String>? categories,
    int? maxQuestions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastEvaluationRequest.copyWith(...)` or call `instanceOfForecastEvaluationRequest.copyWith.fieldName(value)` for a single field.
class _$ForecastEvaluationRequestCWProxyImpl
    implements _$ForecastEvaluationRequestCWProxy {
  const _$ForecastEvaluationRequestCWProxyImpl(this._value);

  final ForecastEvaluationRequest _value;

  @override
  ForecastEvaluationRequest forecasterId(String forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  ForecastEvaluationRequest from(DateTime from) => call(from: from);

  @override
  ForecastEvaluationRequest to(DateTime to) => call(to: to);

  @override
  ForecastEvaluationRequest categories(List<String>? categories) =>
      call(categories: categories);

  @override
  ForecastEvaluationRequest maxQuestions(int? maxQuestions) =>
      call(maxQuestions: maxQuestions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastEvaluationRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastEvaluationRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastEvaluationRequest call({
    Object? forecasterId = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? categories = const $CopyWithPlaceholder(),
    Object? maxQuestions = const $CopyWithPlaceholder(),
  }) {
    return ForecastEvaluationRequest(
      forecasterId:
          forecasterId == const $CopyWithPlaceholder() || forecasterId == null
          ? _value.forecasterId
          // ignore: cast_nullable_to_non_nullable
          : forecasterId as String,
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime,
      categories: categories == const $CopyWithPlaceholder()
          ? _value.categories
          // ignore: cast_nullable_to_non_nullable
          : categories as List<String>?,
      maxQuestions: maxQuestions == const $CopyWithPlaceholder()
          ? _value.maxQuestions
          // ignore: cast_nullable_to_non_nullable
          : maxQuestions as int?,
    );
  }
}

extension $ForecastEvaluationRequestCopyWith on ForecastEvaluationRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastEvaluationRequest.copyWith(...)` or `instanceOfForecastEvaluationRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastEvaluationRequestCWProxy get copyWith =>
      _$ForecastEvaluationRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastEvaluationRequest _$ForecastEvaluationRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastEvaluationRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['forecasterId', 'from', 'to']);
  final val = ForecastEvaluationRequest(
    forecasterId: $checkedConvert('forecasterId', (v) => v as String),
    from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
    to: $checkedConvert('to', (v) => DateTime.parse(v as String)),
    categories: $checkedConvert(
      'categories',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    maxQuestions: $checkedConvert('maxQuestions', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$ForecastEvaluationRequestToJson(
  ForecastEvaluationRequest instance,
) => <String, dynamic>{
  'forecasterId': instance.forecasterId,
  'from': instance.from.toIso8601String(),
  'to': instance.to.toIso8601String(),
  'categories': ?instance.categories,
  'maxQuestions': ?instance.maxQuestions,
};
