// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ledger_question_brief_forecasters_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LedgerQuestionBriefForecastersInnerCWProxy {
  LedgerQuestionBriefForecastersInner forecasterId(String forecasterId);

  LedgerQuestionBriefForecastersInner probability(num? probability);

  LedgerQuestionBriefForecastersInner madeAt(DateTime? madeAt);

  LedgerQuestionBriefForecastersInner knowledgeCutoff(
    DateTime? knowledgeCutoff,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LedgerQuestionBriefForecastersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LedgerQuestionBriefForecastersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  LedgerQuestionBriefForecastersInner call({
    String forecasterId,
    num? probability,
    DateTime? madeAt,
    DateTime? knowledgeCutoff,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLedgerQuestionBriefForecastersInner.copyWith(...)` or call `instanceOfLedgerQuestionBriefForecastersInner.copyWith.fieldName(value)` for a single field.
class _$LedgerQuestionBriefForecastersInnerCWProxyImpl
    implements _$LedgerQuestionBriefForecastersInnerCWProxy {
  const _$LedgerQuestionBriefForecastersInnerCWProxyImpl(this._value);

  final LedgerQuestionBriefForecastersInner _value;

  @override
  LedgerQuestionBriefForecastersInner forecasterId(String forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  LedgerQuestionBriefForecastersInner probability(num? probability) =>
      call(probability: probability);

  @override
  LedgerQuestionBriefForecastersInner madeAt(DateTime? madeAt) =>
      call(madeAt: madeAt);

  @override
  LedgerQuestionBriefForecastersInner knowledgeCutoff(
    DateTime? knowledgeCutoff,
  ) => call(knowledgeCutoff: knowledgeCutoff);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LedgerQuestionBriefForecastersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LedgerQuestionBriefForecastersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LedgerQuestionBriefForecastersInner call({
    Object? forecasterId = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? madeAt = const $CopyWithPlaceholder(),
    Object? knowledgeCutoff = const $CopyWithPlaceholder(),
  }) {
    return LedgerQuestionBriefForecastersInner(
      forecasterId:
          forecasterId == const $CopyWithPlaceholder() || forecasterId == null
          ? _value.forecasterId
          // ignore: cast_nullable_to_non_nullable
          : forecasterId as String,
      probability: probability == const $CopyWithPlaceholder()
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num?,
      madeAt: madeAt == const $CopyWithPlaceholder()
          ? _value.madeAt
          // ignore: cast_nullable_to_non_nullable
          : madeAt as DateTime?,
      knowledgeCutoff: knowledgeCutoff == const $CopyWithPlaceholder()
          ? _value.knowledgeCutoff
          // ignore: cast_nullable_to_non_nullable
          : knowledgeCutoff as DateTime?,
    );
  }
}

extension $LedgerQuestionBriefForecastersInnerCopyWith
    on LedgerQuestionBriefForecastersInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLedgerQuestionBriefForecastersInner.copyWith(...)` or `instanceOfLedgerQuestionBriefForecastersInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LedgerQuestionBriefForecastersInnerCWProxy get copyWith =>
      _$LedgerQuestionBriefForecastersInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LedgerQuestionBriefForecastersInner
_$LedgerQuestionBriefForecastersInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LedgerQuestionBriefForecastersInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['forecasterId']);
      final val = LedgerQuestionBriefForecastersInner(
        forecasterId: $checkedConvert('forecasterId', (v) => v as String),
        probability: $checkedConvert('probability', (v) => v as num?),
        madeAt: $checkedConvert(
          'madeAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        knowledgeCutoff: $checkedConvert(
          'knowledgeCutoff',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$LedgerQuestionBriefForecastersInnerToJson(
  LedgerQuestionBriefForecastersInner instance,
) => <String, dynamic>{
  'forecasterId': instance.forecasterId,
  'probability': ?instance.probability,
  'madeAt': ?instance.madeAt?.toIso8601String(),
  'knowledgeCutoff': ?instance.knowledgeCutoff?.toIso8601String(),
};
