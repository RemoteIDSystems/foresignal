// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'series_data.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SeriesDataCWProxy {
  SeriesData ref(String ref);

  SeriesData from(DateTime from);

  SeriesData to(DateTime to);

  SeriesData asOf(DateTime? asOf);

  SeriesData frequency(SeriesDataFrequencyEnum frequency);

  SeriesData unit(String? unit);

  SeriesData family(String? family);

  SeriesData valuesWithheld(bool valuesWithheld);

  SeriesData points(List<SeriesPoint> points);

  SeriesData gaps(List<TimeRange> gaps);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesData(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesData(...).copyWith(id: 12, name: "My name")
  /// ```
  SeriesData call({
    String ref,
    DateTime from,
    DateTime to,
    DateTime? asOf,
    SeriesDataFrequencyEnum frequency,
    String? unit,
    String? family,
    bool valuesWithheld,
    List<SeriesPoint> points,
    List<TimeRange> gaps,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSeriesData.copyWith(...)` or call `instanceOfSeriesData.copyWith.fieldName(value)` for a single field.
class _$SeriesDataCWProxyImpl implements _$SeriesDataCWProxy {
  const _$SeriesDataCWProxyImpl(this._value);

  final SeriesData _value;

  @override
  SeriesData ref(String ref) => call(ref: ref);

  @override
  SeriesData from(DateTime from) => call(from: from);

  @override
  SeriesData to(DateTime to) => call(to: to);

  @override
  SeriesData asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  SeriesData frequency(SeriesDataFrequencyEnum frequency) =>
      call(frequency: frequency);

  @override
  SeriesData unit(String? unit) => call(unit: unit);

  @override
  SeriesData family(String? family) => call(family: family);

  @override
  SeriesData valuesWithheld(bool valuesWithheld) =>
      call(valuesWithheld: valuesWithheld);

  @override
  SeriesData points(List<SeriesPoint> points) => call(points: points);

  @override
  SeriesData gaps(List<TimeRange> gaps) => call(gaps: gaps);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesData(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesData(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SeriesData call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? frequency = const $CopyWithPlaceholder(),
    Object? unit = const $CopyWithPlaceholder(),
    Object? family = const $CopyWithPlaceholder(),
    Object? valuesWithheld = const $CopyWithPlaceholder(),
    Object? points = const $CopyWithPlaceholder(),
    Object? gaps = const $CopyWithPlaceholder(),
  }) {
    return SeriesData(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      frequency: frequency == const $CopyWithPlaceholder() || frequency == null
          ? _value.frequency
          // ignore: cast_nullable_to_non_nullable
          : frequency as SeriesDataFrequencyEnum,
      unit: unit == const $CopyWithPlaceholder()
          ? _value.unit
          // ignore: cast_nullable_to_non_nullable
          : unit as String?,
      family: family == const $CopyWithPlaceholder()
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String?,
      valuesWithheld:
          valuesWithheld == const $CopyWithPlaceholder() ||
              valuesWithheld == null
          ? _value.valuesWithheld
          // ignore: cast_nullable_to_non_nullable
          : valuesWithheld as bool,
      points: points == const $CopyWithPlaceholder() || points == null
          ? _value.points
          // ignore: cast_nullable_to_non_nullable
          : points as List<SeriesPoint>,
      gaps: gaps == const $CopyWithPlaceholder() || gaps == null
          ? _value.gaps
          // ignore: cast_nullable_to_non_nullable
          : gaps as List<TimeRange>,
    );
  }
}

extension $SeriesDataCopyWith on SeriesData {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSeriesData.copyWith(...)` or `instanceOfSeriesData.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SeriesDataCWProxy get copyWith => _$SeriesDataCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SeriesData _$SeriesDataFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SeriesData', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'ref',
          'from',
          'to',
          'frequency',
          'valuesWithheld',
          'points',
          'gaps',
        ],
      );
      final val = SeriesData(
        ref: $checkedConvert('ref', (v) => v as String),
        from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
        to: $checkedConvert('to', (v) => DateTime.parse(v as String)),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        frequency: $checkedConvert(
          'frequency',
          (v) => $enumDecode(_$SeriesDataFrequencyEnumEnumMap, v),
        ),
        unit: $checkedConvert('unit', (v) => v as String?),
        family: $checkedConvert('family', (v) => v as String?),
        valuesWithheld: $checkedConvert('valuesWithheld', (v) => v as bool),
        points: $checkedConvert(
          'points',
          (v) => (v as List<dynamic>)
              .map((e) => SeriesPoint.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        gaps: $checkedConvert(
          'gaps',
          (v) => (v as List<dynamic>)
              .map((e) => TimeRange.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SeriesDataToJson(SeriesData instance) =>
    <String, dynamic>{
      'ref': instance.ref,
      'from': instance.from.toIso8601String(),
      'to': instance.to.toIso8601String(),
      'asOf': ?instance.asOf?.toIso8601String(),
      'frequency': _$SeriesDataFrequencyEnumEnumMap[instance.frequency]!,
      'unit': ?instance.unit,
      'family': ?instance.family,
      'valuesWithheld': instance.valuesWithheld,
      'points': instance.points.map((e) => e.toJson()).toList(),
      'gaps': instance.gaps.map((e) => e.toJson()).toList(),
    };

const _$SeriesDataFrequencyEnumEnumMap = {
  SeriesDataFrequencyEnum.hour: 'hour',
  SeriesDataFrequencyEnum.day: 'day',
  SeriesDataFrequencyEnum.week: 'week',
  SeriesDataFrequencyEnum.month: 'month',
  SeriesDataFrequencyEnum.irregular: 'irregular',
};
