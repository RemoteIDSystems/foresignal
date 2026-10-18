// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_citations_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastCitationsInnerCWProxy {
  ForecastCitationsInner ref(String ref);

  ForecastCitationsInner quote(String? quote);

  ForecastCitationsInner claim(String? claim);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastCitationsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastCitationsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastCitationsInner call({String ref, String? quote, String? claim});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastCitationsInner.copyWith(...)` or call `instanceOfForecastCitationsInner.copyWith.fieldName(value)` for a single field.
class _$ForecastCitationsInnerCWProxyImpl
    implements _$ForecastCitationsInnerCWProxy {
  const _$ForecastCitationsInnerCWProxyImpl(this._value);

  final ForecastCitationsInner _value;

  @override
  ForecastCitationsInner ref(String ref) => call(ref: ref);

  @override
  ForecastCitationsInner quote(String? quote) => call(quote: quote);

  @override
  ForecastCitationsInner claim(String? claim) => call(claim: claim);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastCitationsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastCitationsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastCitationsInner call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? quote = const $CopyWithPlaceholder(),
    Object? claim = const $CopyWithPlaceholder(),
  }) {
    return ForecastCitationsInner(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      quote: quote == const $CopyWithPlaceholder()
          ? _value.quote
          // ignore: cast_nullable_to_non_nullable
          : quote as String?,
      claim: claim == const $CopyWithPlaceholder()
          ? _value.claim
          // ignore: cast_nullable_to_non_nullable
          : claim as String?,
    );
  }
}

extension $ForecastCitationsInnerCopyWith on ForecastCitationsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastCitationsInner.copyWith(...)` or `instanceOfForecastCitationsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastCitationsInnerCWProxy get copyWith =>
      _$ForecastCitationsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastCitationsInner _$ForecastCitationsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastCitationsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['ref']);
  final val = ForecastCitationsInner(
    ref: $checkedConvert('ref', (v) => v as String),
    quote: $checkedConvert('quote', (v) => v as String?),
    claim: $checkedConvert('claim', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ForecastCitationsInnerToJson(
  ForecastCitationsInner instance,
) => <String, dynamic>{
  'ref': instance.ref,
  'quote': ?instance.quote,
  'claim': ?instance.claim,
};
