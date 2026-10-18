// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'series_forecast.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SeriesForecastCWProxy {
  SeriesForecast ref(String ref);

  SeriesForecast method(SeriesForecastMethodEnum method);

  SeriesForecast version(String version);

  SeriesForecast madeAt(DateTime madeAt);

  SeriesForecast asOfSeq(int asOfSeq);

  SeriesForecast manifestId(String? manifestId);

  SeriesForecast units(SeriesForecastUnitsEnum units);

  SeriesForecast unit(String? unit);

  SeriesForecast forecasts(List<SeriesForecastForecastsInner> forecasts);

  SeriesForecast questionIds(List<String>? questionIds);

  SeriesForecast caveats(List<Caveat>? caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesForecast(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesForecast(...).copyWith(id: 12, name: "My name")
  /// ```
  SeriesForecast call({
    String ref,
    SeriesForecastMethodEnum method,
    String version,
    DateTime madeAt,
    int asOfSeq,
    String? manifestId,
    SeriesForecastUnitsEnum units,
    String? unit,
    List<SeriesForecastForecastsInner> forecasts,
    List<String>? questionIds,
    List<Caveat>? caveats,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSeriesForecast.copyWith(...)` or call `instanceOfSeriesForecast.copyWith.fieldName(value)` for a single field.
class _$SeriesForecastCWProxyImpl implements _$SeriesForecastCWProxy {
  const _$SeriesForecastCWProxyImpl(this._value);

  final SeriesForecast _value;

  @override
  SeriesForecast ref(String ref) => call(ref: ref);

  @override
  SeriesForecast method(SeriesForecastMethodEnum method) =>
      call(method: method);

  @override
  SeriesForecast version(String version) => call(version: version);

  @override
  SeriesForecast madeAt(DateTime madeAt) => call(madeAt: madeAt);

  @override
  SeriesForecast asOfSeq(int asOfSeq) => call(asOfSeq: asOfSeq);

  @override
  SeriesForecast manifestId(String? manifestId) => call(manifestId: manifestId);

  @override
  SeriesForecast units(SeriesForecastUnitsEnum units) => call(units: units);

  @override
  SeriesForecast unit(String? unit) => call(unit: unit);

  @override
  SeriesForecast forecasts(List<SeriesForecastForecastsInner> forecasts) =>
      call(forecasts: forecasts);

  @override
  SeriesForecast questionIds(List<String>? questionIds) =>
      call(questionIds: questionIds);

  @override
  SeriesForecast caveats(List<Caveat>? caveats) => call(caveats: caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesForecast(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesForecast(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SeriesForecast call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? madeAt = const $CopyWithPlaceholder(),
    Object? asOfSeq = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? units = const $CopyWithPlaceholder(),
    Object? unit = const $CopyWithPlaceholder(),
    Object? forecasts = const $CopyWithPlaceholder(),
    Object? questionIds = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
  }) {
    return SeriesForecast(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as SeriesForecastMethodEnum,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String,
      madeAt: madeAt == const $CopyWithPlaceholder() || madeAt == null
          ? _value.madeAt
          // ignore: cast_nullable_to_non_nullable
          : madeAt as DateTime,
      asOfSeq: asOfSeq == const $CopyWithPlaceholder() || asOfSeq == null
          ? _value.asOfSeq
          // ignore: cast_nullable_to_non_nullable
          : asOfSeq as int,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
      units: units == const $CopyWithPlaceholder() || units == null
          ? _value.units
          // ignore: cast_nullable_to_non_nullable
          : units as SeriesForecastUnitsEnum,
      unit: unit == const $CopyWithPlaceholder()
          ? _value.unit
          // ignore: cast_nullable_to_non_nullable
          : unit as String?,
      forecasts: forecasts == const $CopyWithPlaceholder() || forecasts == null
          ? _value.forecasts
          // ignore: cast_nullable_to_non_nullable
          : forecasts as List<SeriesForecastForecastsInner>,
      questionIds: questionIds == const $CopyWithPlaceholder()
          ? _value.questionIds
          // ignore: cast_nullable_to_non_nullable
          : questionIds as List<String>?,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
    );
  }
}

extension $SeriesForecastCopyWith on SeriesForecast {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSeriesForecast.copyWith(...)` or `instanceOfSeriesForecast.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SeriesForecastCWProxy get copyWith => _$SeriesForecastCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SeriesForecast _$SeriesForecastFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SeriesForecast', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'ref',
          'method',
          'version',
          'madeAt',
          'asOfSeq',
          'units',
          'forecasts',
        ],
      );
      final val = SeriesForecast(
        ref: $checkedConvert('ref', (v) => v as String),
        method: $checkedConvert(
          'method',
          (v) => $enumDecode(_$SeriesForecastMethodEnumEnumMap, v),
        ),
        version: $checkedConvert('version', (v) => v as String),
        madeAt: $checkedConvert('madeAt', (v) => DateTime.parse(v as String)),
        asOfSeq: $checkedConvert('asOfSeq', (v) => (v as num).toInt()),
        manifestId: $checkedConvert('manifestId', (v) => v as String?),
        units: $checkedConvert(
          'units',
          (v) => $enumDecode(_$SeriesForecastUnitsEnumEnumMap, v),
        ),
        unit: $checkedConvert('unit', (v) => v as String?),
        forecasts: $checkedConvert(
          'forecasts',
          (v) => (v as List<dynamic>)
              .map(
                (e) => SeriesForecastForecastsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        questionIds: $checkedConvert(
          'questionIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        caveats: $checkedConvert(
          'caveats',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SeriesForecastToJson(SeriesForecast instance) =>
    <String, dynamic>{
      'ref': instance.ref,
      'method': _$SeriesForecastMethodEnumEnumMap[instance.method]!,
      'version': instance.version,
      'madeAt': instance.madeAt.toIso8601String(),
      'asOfSeq': instance.asOfSeq,
      'manifestId': ?instance.manifestId,
      'units': _$SeriesForecastUnitsEnumEnumMap[instance.units]!,
      'unit': ?instance.unit,
      'forecasts': instance.forecasts.map((e) => e.toJson()).toList(),
      'questionIds': ?instance.questionIds,
      'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
    };

const _$SeriesForecastMethodEnumEnumMap = {
  SeriesForecastMethodEnum.seasonalNaive: 'seasonal-naive',
  SeriesForecastMethodEnum.etsAnn: 'ets-ann',
  SeriesForecastMethodEnum.etsAaa: 'ets-aaa',
  SeriesForecastMethodEnum.nbGlm: 'nb-glm',
  SeriesForecastMethodEnum.dampedTrend: 'damped-trend',
};

const _$SeriesForecastUnitsEnumEnumMap = {
  SeriesForecastUnitsEnum.native_: 'native',
  SeriesForecastUnitsEnum.standardised: 'standardised',
};
