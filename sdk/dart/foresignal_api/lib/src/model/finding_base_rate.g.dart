// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'finding_base_rate.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FindingBaseRateCWProxy {
  FindingBaseRate value(num? value);

  FindingBaseRate unit(String unit);

  FindingBaseRate method(FindingBaseRateMethodEnum method);

  FindingBaseRate n(int? n);

  FindingBaseRate period(TimeRange? period);

  FindingBaseRate text(String text);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FindingBaseRate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FindingBaseRate(...).copyWith(id: 12, name: "My name")
  /// ```
  FindingBaseRate call({
    num? value,
    String unit,
    FindingBaseRateMethodEnum method,
    int? n,
    TimeRange? period,
    String text,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFindingBaseRate.copyWith(...)` or call `instanceOfFindingBaseRate.copyWith.fieldName(value)` for a single field.
class _$FindingBaseRateCWProxyImpl implements _$FindingBaseRateCWProxy {
  const _$FindingBaseRateCWProxyImpl(this._value);

  final FindingBaseRate _value;

  @override
  FindingBaseRate value(num? value) => call(value: value);

  @override
  FindingBaseRate unit(String unit) => call(unit: unit);

  @override
  FindingBaseRate method(FindingBaseRateMethodEnum method) =>
      call(method: method);

  @override
  FindingBaseRate n(int? n) => call(n: n);

  @override
  FindingBaseRate period(TimeRange? period) => call(period: period);

  @override
  FindingBaseRate text(String text) => call(text: text);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FindingBaseRate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FindingBaseRate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FindingBaseRate call({
    Object? value = const $CopyWithPlaceholder(),
    Object? unit = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
    Object? period = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
  }) {
    return FindingBaseRate(
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num?,
      unit: unit == const $CopyWithPlaceholder() || unit == null
          ? _value.unit
          // ignore: cast_nullable_to_non_nullable
          : unit as String,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as FindingBaseRateMethodEnum,
      n: n == const $CopyWithPlaceholder()
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int?,
      period: period == const $CopyWithPlaceholder()
          ? _value.period
          // ignore: cast_nullable_to_non_nullable
          : period as TimeRange?,
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
    );
  }
}

extension $FindingBaseRateCopyWith on FindingBaseRate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFindingBaseRate.copyWith(...)` or `instanceOfFindingBaseRate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FindingBaseRateCWProxy get copyWith => _$FindingBaseRateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FindingBaseRate _$FindingBaseRateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FindingBaseRate', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['value', 'unit', 'method', 'text']);
      final val = FindingBaseRate(
        value: $checkedConvert('value', (v) => v as num?),
        unit: $checkedConvert('unit', (v) => v as String),
        method: $checkedConvert(
          'method',
          (v) => $enumDecode(_$FindingBaseRateMethodEnumEnumMap, v),
        ),
        n: $checkedConvert('n', (v) => (v as num?)?.toInt()),
        period: $checkedConvert(
          'period',
          (v) =>
              v == null ? null : TimeRange.fromJson(v as Map<String, dynamic>),
        ),
        text: $checkedConvert('text', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$FindingBaseRateToJson(FindingBaseRate instance) =>
    <String, dynamic>{
      'value': instance.value,
      'unit': instance.unit,
      'method': _$FindingBaseRateMethodEnumEnumMap[instance.method]!,
      'n': ?instance.n,
      'period': ?instance.period?.toJson(),
      'text': instance.text,
    };

const _$FindingBaseRateMethodEnumEnumMap = {
  FindingBaseRateMethodEnum.detectorFalseAlarmRate: 'detector-false-alarm-rate',
  FindingBaseRateMethodEnum.placeboDistribution: 'placebo-distribution',
  FindingBaseRateMethodEnum.independenceBaseline: 'independence-baseline',
  FindingBaseRateMethodEnum.precursorBaseRate: 'precursor-base-rate',
  FindingBaseRateMethodEnum.other: 'other',
};
