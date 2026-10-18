// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'indicator_signal.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndicatorSignalCWProxy {
  IndicatorSignal type(IndicatorSignalTypeEnum type);

  IndicatorSignal rule(TriggerRuleInput? rule);

  IndicatorSignal precursorId(String? precursorId);

  IndicatorSignal series(IndicatorSignalSeries? series);

  IndicatorSignal intelEvent(IndicatorSignalIntelEvent? intelEvent);

  IndicatorSignal situation(IndicatorSignalSituation? situation);

  IndicatorSignal question(IndicatorSignalQuestion? question);

  IndicatorSignal emerging(Object? emerging);

  IndicatorSignal baseRate(num? baseRate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorSignal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorSignal(...).copyWith(id: 12, name: "My name")
  /// ```
  IndicatorSignal call({
    IndicatorSignalTypeEnum type,
    TriggerRuleInput? rule,
    String? precursorId,
    IndicatorSignalSeries? series,
    IndicatorSignalIntelEvent? intelEvent,
    IndicatorSignalSituation? situation,
    IndicatorSignalQuestion? question,
    Object? emerging,
    num? baseRate,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndicatorSignal.copyWith(...)` or call `instanceOfIndicatorSignal.copyWith.fieldName(value)` for a single field.
class _$IndicatorSignalCWProxyImpl implements _$IndicatorSignalCWProxy {
  const _$IndicatorSignalCWProxyImpl(this._value);

  final IndicatorSignal _value;

  @override
  IndicatorSignal type(IndicatorSignalTypeEnum type) => call(type: type);

  @override
  IndicatorSignal rule(TriggerRuleInput? rule) => call(rule: rule);

  @override
  IndicatorSignal precursorId(String? precursorId) =>
      call(precursorId: precursorId);

  @override
  IndicatorSignal series(IndicatorSignalSeries? series) => call(series: series);

  @override
  IndicatorSignal intelEvent(IndicatorSignalIntelEvent? intelEvent) =>
      call(intelEvent: intelEvent);

  @override
  IndicatorSignal situation(IndicatorSignalSituation? situation) =>
      call(situation: situation);

  @override
  IndicatorSignal question(IndicatorSignalQuestion? question) =>
      call(question: question);

  @override
  IndicatorSignal emerging(Object? emerging) => call(emerging: emerging);

  @override
  IndicatorSignal baseRate(num? baseRate) => call(baseRate: baseRate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorSignal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorSignal(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IndicatorSignal call({
    Object? type = const $CopyWithPlaceholder(),
    Object? rule = const $CopyWithPlaceholder(),
    Object? precursorId = const $CopyWithPlaceholder(),
    Object? series = const $CopyWithPlaceholder(),
    Object? intelEvent = const $CopyWithPlaceholder(),
    Object? situation = const $CopyWithPlaceholder(),
    Object? question = const $CopyWithPlaceholder(),
    Object? emerging = const $CopyWithPlaceholder(),
    Object? baseRate = const $CopyWithPlaceholder(),
  }) {
    return IndicatorSignal(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as IndicatorSignalTypeEnum,
      rule: rule == const $CopyWithPlaceholder()
          ? _value.rule
          // ignore: cast_nullable_to_non_nullable
          : rule as TriggerRuleInput?,
      precursorId: precursorId == const $CopyWithPlaceholder()
          ? _value.precursorId
          // ignore: cast_nullable_to_non_nullable
          : precursorId as String?,
      series: series == const $CopyWithPlaceholder()
          ? _value.series
          // ignore: cast_nullable_to_non_nullable
          : series as IndicatorSignalSeries?,
      intelEvent: intelEvent == const $CopyWithPlaceholder()
          ? _value.intelEvent
          // ignore: cast_nullable_to_non_nullable
          : intelEvent as IndicatorSignalIntelEvent?,
      situation: situation == const $CopyWithPlaceholder()
          ? _value.situation
          // ignore: cast_nullable_to_non_nullable
          : situation as IndicatorSignalSituation?,
      question: question == const $CopyWithPlaceholder()
          ? _value.question
          // ignore: cast_nullable_to_non_nullable
          : question as IndicatorSignalQuestion?,
      emerging: emerging == const $CopyWithPlaceholder()
          ? _value.emerging
          // ignore: cast_nullable_to_non_nullable
          : emerging as Object?,
      baseRate: baseRate == const $CopyWithPlaceholder()
          ? _value.baseRate
          // ignore: cast_nullable_to_non_nullable
          : baseRate as num?,
    );
  }
}

extension $IndicatorSignalCopyWith on IndicatorSignal {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndicatorSignal.copyWith(...)` or `instanceOfIndicatorSignal.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndicatorSignalCWProxy get copyWith => _$IndicatorSignalCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IndicatorSignal _$IndicatorSignalFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IndicatorSignal', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type']);
      final val = IndicatorSignal(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$IndicatorSignalTypeEnumEnumMap, v),
        ),
        rule: $checkedConvert(
          'rule',
          (v) => v == null
              ? null
              : TriggerRuleInput.fromJson(v as Map<String, dynamic>),
        ),
        precursorId: $checkedConvert('precursorId', (v) => v as String?),
        series: $checkedConvert(
          'series',
          (v) => v == null
              ? null
              : IndicatorSignalSeries.fromJson(v as Map<String, dynamic>),
        ),
        intelEvent: $checkedConvert(
          'intelEvent',
          (v) => v == null
              ? null
              : IndicatorSignalIntelEvent.fromJson(v as Map<String, dynamic>),
        ),
        situation: $checkedConvert(
          'situation',
          (v) => v == null
              ? null
              : IndicatorSignalSituation.fromJson(v as Map<String, dynamic>),
        ),
        question: $checkedConvert(
          'question',
          (v) => v == null
              ? null
              : IndicatorSignalQuestion.fromJson(v as Map<String, dynamic>),
        ),
        emerging: $checkedConvert('emerging', (v) => v),
        baseRate: $checkedConvert('baseRate', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$IndicatorSignalToJson(IndicatorSignal instance) =>
    <String, dynamic>{
      'type': _$IndicatorSignalTypeEnumEnumMap[instance.type]!,
      'rule': ?instance.rule?.toJson(),
      'precursorId': ?instance.precursorId,
      'series': ?instance.series?.toJson(),
      'intelEvent': ?instance.intelEvent?.toJson(),
      'situation': ?instance.situation?.toJson(),
      'question': ?instance.question?.toJson(),
      'emerging': ?instance.emerging,
      'baseRate': ?instance.baseRate,
    };

const _$IndicatorSignalTypeEnumEnumMap = {
  IndicatorSignalTypeEnum.rule: 'rule',
  IndicatorSignalTypeEnum.precursor: 'precursor',
  IndicatorSignalTypeEnum.series: 'series',
  IndicatorSignalTypeEnum.intelEvent: 'intelEvent',
  IndicatorSignalTypeEnum.situation: 'situation',
  IndicatorSignalTypeEnum.question: 'question',
  IndicatorSignalTypeEnum.manual: 'manual',
};
