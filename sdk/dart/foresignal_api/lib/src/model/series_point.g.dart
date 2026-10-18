// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'series_point.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SeriesPointCWProxy {
  SeriesPoint period(String period);

  SeriesPoint periodStart(DateTime periodStart);

  SeriesPoint periodEnd(DateTime periodEnd);

  SeriesPoint value(num? value);

  SeriesPoint unit(String unit);

  SeriesPoint status(SeriesPointStatusEnum status);

  SeriesPoint extra(Map<String, Object?>? extra);

  SeriesPoint version(int? version);

  SeriesPoint observedAt(DateTime? observedAt);

  SeriesPoint knownAt(DateTime? knownAt);

  SeriesPoint coverageOk(bool? coverageOk);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesPoint(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesPoint(...).copyWith(id: 12, name: "My name")
  /// ```
  SeriesPoint call({
    String period,
    DateTime periodStart,
    DateTime periodEnd,
    num? value,
    String unit,
    SeriesPointStatusEnum status,
    Map<String, Object?>? extra,
    int? version,
    DateTime? observedAt,
    DateTime? knownAt,
    bool? coverageOk,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSeriesPoint.copyWith(...)` or call `instanceOfSeriesPoint.copyWith.fieldName(value)` for a single field.
class _$SeriesPointCWProxyImpl implements _$SeriesPointCWProxy {
  const _$SeriesPointCWProxyImpl(this._value);

  final SeriesPoint _value;

  @override
  SeriesPoint period(String period) => call(period: period);

  @override
  SeriesPoint periodStart(DateTime periodStart) =>
      call(periodStart: periodStart);

  @override
  SeriesPoint periodEnd(DateTime periodEnd) => call(periodEnd: periodEnd);

  @override
  SeriesPoint value(num? value) => call(value: value);

  @override
  SeriesPoint unit(String unit) => call(unit: unit);

  @override
  SeriesPoint status(SeriesPointStatusEnum status) => call(status: status);

  @override
  SeriesPoint extra(Map<String, Object?>? extra) => call(extra: extra);

  @override
  SeriesPoint version(int? version) => call(version: version);

  @override
  SeriesPoint observedAt(DateTime? observedAt) => call(observedAt: observedAt);

  @override
  SeriesPoint knownAt(DateTime? knownAt) => call(knownAt: knownAt);

  @override
  SeriesPoint coverageOk(bool? coverageOk) => call(coverageOk: coverageOk);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesPoint(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesPoint(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SeriesPoint call({
    Object? period = const $CopyWithPlaceholder(),
    Object? periodStart = const $CopyWithPlaceholder(),
    Object? periodEnd = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? unit = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? extra = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? knownAt = const $CopyWithPlaceholder(),
    Object? coverageOk = const $CopyWithPlaceholder(),
  }) {
    return SeriesPoint(
      period: period == const $CopyWithPlaceholder() || period == null
          ? _value.period
          // ignore: cast_nullable_to_non_nullable
          : period as String,
      periodStart:
          periodStart == const $CopyWithPlaceholder() || periodStart == null
          ? _value.periodStart
          // ignore: cast_nullable_to_non_nullable
          : periodStart as DateTime,
      periodEnd: periodEnd == const $CopyWithPlaceholder() || periodEnd == null
          ? _value.periodEnd
          // ignore: cast_nullable_to_non_nullable
          : periodEnd as DateTime,
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num?,
      unit: unit == const $CopyWithPlaceholder() || unit == null
          ? _value.unit
          // ignore: cast_nullable_to_non_nullable
          : unit as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SeriesPointStatusEnum,
      extra: extra == const $CopyWithPlaceholder()
          ? _value.extra
          // ignore: cast_nullable_to_non_nullable
          : extra as Map<String, Object?>?,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int?,
      observedAt: observedAt == const $CopyWithPlaceholder()
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime?,
      knownAt: knownAt == const $CopyWithPlaceholder()
          ? _value.knownAt
          // ignore: cast_nullable_to_non_nullable
          : knownAt as DateTime?,
      coverageOk: coverageOk == const $CopyWithPlaceholder()
          ? _value.coverageOk
          // ignore: cast_nullable_to_non_nullable
          : coverageOk as bool?,
    );
  }
}

extension $SeriesPointCopyWith on SeriesPoint {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSeriesPoint.copyWith(...)` or `instanceOfSeriesPoint.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SeriesPointCWProxy get copyWith => _$SeriesPointCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SeriesPoint _$SeriesPointFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SeriesPoint', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'period',
          'periodStart',
          'periodEnd',
          'value',
          'unit',
          'status',
        ],
      );
      final val = SeriesPoint(
        period: $checkedConvert('period', (v) => v as String),
        periodStart: $checkedConvert(
          'periodStart',
          (v) => DateTime.parse(v as String),
        ),
        periodEnd: $checkedConvert(
          'periodEnd',
          (v) => DateTime.parse(v as String),
        ),
        value: $checkedConvert('value', (v) => v as num?),
        unit: $checkedConvert('unit', (v) => v as String),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$SeriesPointStatusEnumEnumMap, v),
        ),
        extra: $checkedConvert('extra', (v) => v as Map<String, dynamic>?),
        version: $checkedConvert('version', (v) => (v as num?)?.toInt()),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        knownAt: $checkedConvert(
          'knownAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        coverageOk: $checkedConvert('coverageOk', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$SeriesPointToJson(SeriesPoint instance) =>
    <String, dynamic>{
      'period': instance.period,
      'periodStart': instance.periodStart.toIso8601String(),
      'periodEnd': instance.periodEnd.toIso8601String(),
      'value': instance.value,
      'unit': instance.unit,
      'status': _$SeriesPointStatusEnumEnumMap[instance.status]!,
      'extra': ?instance.extra,
      'version': ?instance.version,
      'observedAt': ?instance.observedAt?.toIso8601String(),
      'knownAt': ?instance.knownAt?.toIso8601String(),
      'coverageOk': ?instance.coverageOk,
    };

const _$SeriesPointStatusEnumEnumMap = {
  SeriesPointStatusEnum.final_: 'final',
  SeriesPointStatusEnum.preliminary: 'preliminary',
  SeriesPointStatusEnum.estimated: 'estimated',
  SeriesPointStatusEnum.forecast: 'forecast',
};
