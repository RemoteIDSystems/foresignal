// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_pricing.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmPricingCWProxy {
  LlmPricing inputUsdPerMillionTokens(num? inputUsdPerMillionTokens);

  LlmPricing outputUsdPerMillionTokens(num? outputUsdPerMillionTokens);

  LlmPricing cachedInputUsdPerMillionTokens(
    num? cachedInputUsdPerMillionTokens,
  );

  LlmPricing embeddingUsdPerMillionTokens(num? embeddingUsdPerMillionTokens);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmPricing(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmPricing(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmPricing call({
    num? inputUsdPerMillionTokens,
    num? outputUsdPerMillionTokens,
    num? cachedInputUsdPerMillionTokens,
    num? embeddingUsdPerMillionTokens,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmPricing.copyWith(...)` or call `instanceOfLlmPricing.copyWith.fieldName(value)` for a single field.
class _$LlmPricingCWProxyImpl implements _$LlmPricingCWProxy {
  const _$LlmPricingCWProxyImpl(this._value);

  final LlmPricing _value;

  @override
  LlmPricing inputUsdPerMillionTokens(num? inputUsdPerMillionTokens) =>
      call(inputUsdPerMillionTokens: inputUsdPerMillionTokens);

  @override
  LlmPricing outputUsdPerMillionTokens(num? outputUsdPerMillionTokens) =>
      call(outputUsdPerMillionTokens: outputUsdPerMillionTokens);

  @override
  LlmPricing cachedInputUsdPerMillionTokens(
    num? cachedInputUsdPerMillionTokens,
  ) => call(cachedInputUsdPerMillionTokens: cachedInputUsdPerMillionTokens);

  @override
  LlmPricing embeddingUsdPerMillionTokens(num? embeddingUsdPerMillionTokens) =>
      call(embeddingUsdPerMillionTokens: embeddingUsdPerMillionTokens);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmPricing(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmPricing(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmPricing call({
    Object? inputUsdPerMillionTokens = const $CopyWithPlaceholder(),
    Object? outputUsdPerMillionTokens = const $CopyWithPlaceholder(),
    Object? cachedInputUsdPerMillionTokens = const $CopyWithPlaceholder(),
    Object? embeddingUsdPerMillionTokens = const $CopyWithPlaceholder(),
  }) {
    return LlmPricing(
      inputUsdPerMillionTokens:
          inputUsdPerMillionTokens == const $CopyWithPlaceholder()
          ? _value.inputUsdPerMillionTokens
          // ignore: cast_nullable_to_non_nullable
          : inputUsdPerMillionTokens as num?,
      outputUsdPerMillionTokens:
          outputUsdPerMillionTokens == const $CopyWithPlaceholder()
          ? _value.outputUsdPerMillionTokens
          // ignore: cast_nullable_to_non_nullable
          : outputUsdPerMillionTokens as num?,
      cachedInputUsdPerMillionTokens:
          cachedInputUsdPerMillionTokens == const $CopyWithPlaceholder()
          ? _value.cachedInputUsdPerMillionTokens
          // ignore: cast_nullable_to_non_nullable
          : cachedInputUsdPerMillionTokens as num?,
      embeddingUsdPerMillionTokens:
          embeddingUsdPerMillionTokens == const $CopyWithPlaceholder()
          ? _value.embeddingUsdPerMillionTokens
          // ignore: cast_nullable_to_non_nullable
          : embeddingUsdPerMillionTokens as num?,
    );
  }
}

extension $LlmPricingCopyWith on LlmPricing {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmPricing.copyWith(...)` or `instanceOfLlmPricing.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmPricingCWProxy get copyWith => _$LlmPricingCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmPricing _$LlmPricingFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LlmPricing', json, ($checkedConvert) {
      final val = LlmPricing(
        inputUsdPerMillionTokens: $checkedConvert(
          'inputUsdPerMillionTokens',
          (v) => v as num?,
        ),
        outputUsdPerMillionTokens: $checkedConvert(
          'outputUsdPerMillionTokens',
          (v) => v as num?,
        ),
        cachedInputUsdPerMillionTokens: $checkedConvert(
          'cachedInputUsdPerMillionTokens',
          (v) => v as num?,
        ),
        embeddingUsdPerMillionTokens: $checkedConvert(
          'embeddingUsdPerMillionTokens',
          (v) => v as num?,
        ),
      );
      return val;
    });

Map<String, dynamic> _$LlmPricingToJson(
  LlmPricing instance,
) => <String, dynamic>{
  'inputUsdPerMillionTokens': ?instance.inputUsdPerMillionTokens,
  'outputUsdPerMillionTokens': ?instance.outputUsdPerMillionTokens,
  'cachedInputUsdPerMillionTokens': ?instance.cachedInputUsdPerMillionTokens,
  'embeddingUsdPerMillionTokens': ?instance.embeddingUsdPerMillionTokens,
};
