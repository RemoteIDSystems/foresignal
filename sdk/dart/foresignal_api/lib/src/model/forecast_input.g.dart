// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastInputCWProxy {
  ForecastInput probability(num? probability);

  ForecastInput distribution(Map<String, num>? distribution);

  ForecastInput quantiles(Quantiles? quantiles);

  ForecastInput rationale(String? rationale);

  ForecastInput citations(List<ForecastCitationsInner>? citations);

  ForecastInput forecasterId(String? forecasterId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastInput call({
    num? probability,
    Map<String, num>? distribution,
    Quantiles? quantiles,
    String? rationale,
    List<ForecastCitationsInner>? citations,
    String? forecasterId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastInput.copyWith(...)` or call `instanceOfForecastInput.copyWith.fieldName(value)` for a single field.
class _$ForecastInputCWProxyImpl implements _$ForecastInputCWProxy {
  const _$ForecastInputCWProxyImpl(this._value);

  final ForecastInput _value;

  @override
  ForecastInput probability(num? probability) => call(probability: probability);

  @override
  ForecastInput distribution(Map<String, num>? distribution) =>
      call(distribution: distribution);

  @override
  ForecastInput quantiles(Quantiles? quantiles) => call(quantiles: quantiles);

  @override
  ForecastInput rationale(String? rationale) => call(rationale: rationale);

  @override
  ForecastInput citations(List<ForecastCitationsInner>? citations) =>
      call(citations: citations);

  @override
  ForecastInput forecasterId(String? forecasterId) =>
      call(forecasterId: forecasterId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastInput call({
    Object? probability = const $CopyWithPlaceholder(),
    Object? distribution = const $CopyWithPlaceholder(),
    Object? quantiles = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
    Object? citations = const $CopyWithPlaceholder(),
    Object? forecasterId = const $CopyWithPlaceholder(),
  }) {
    return ForecastInput(
      probability: probability == const $CopyWithPlaceholder()
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num?,
      distribution: distribution == const $CopyWithPlaceholder()
          ? _value.distribution
          // ignore: cast_nullable_to_non_nullable
          : distribution as Map<String, num>?,
      quantiles: quantiles == const $CopyWithPlaceholder()
          ? _value.quantiles
          // ignore: cast_nullable_to_non_nullable
          : quantiles as Quantiles?,
      rationale: rationale == const $CopyWithPlaceholder()
          ? _value.rationale
          // ignore: cast_nullable_to_non_nullable
          : rationale as String?,
      citations: citations == const $CopyWithPlaceholder()
          ? _value.citations
          // ignore: cast_nullable_to_non_nullable
          : citations as List<ForecastCitationsInner>?,
      forecasterId: forecasterId == const $CopyWithPlaceholder()
          ? _value.forecasterId
          // ignore: cast_nullable_to_non_nullable
          : forecasterId as String?,
    );
  }
}

extension $ForecastInputCopyWith on ForecastInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastInput.copyWith(...)` or `instanceOfForecastInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastInputCWProxy get copyWith => _$ForecastInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastInput _$ForecastInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ForecastInput', json, ($checkedConvert) {
      final val = ForecastInput(
        probability: $checkedConvert('probability', (v) => v as num?),
        distribution: $checkedConvert(
          'distribution',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as num),
          ),
        ),
        quantiles: $checkedConvert(
          'quantiles',
          (v) =>
              v == null ? null : Quantiles.fromJson(v as Map<String, dynamic>),
        ),
        rationale: $checkedConvert('rationale', (v) => v as String?),
        citations: $checkedConvert(
          'citations',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) =>
                    ForecastCitationsInner.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
        forecasterId: $checkedConvert('forecasterId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ForecastInputToJson(ForecastInput instance) =>
    <String, dynamic>{
      'probability': ?instance.probability,
      'distribution': ?instance.distribution,
      'quantiles': ?instance.quantiles?.toJson(),
      'rationale': ?instance.rationale,
      'citations': ?instance.citations?.map((e) => e.toJson()).toList(),
      'forecasterId': ?instance.forecasterId,
    };
