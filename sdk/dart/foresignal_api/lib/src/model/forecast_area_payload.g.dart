// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_area_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastAreaPayloadCWProxy {
  ForecastAreaPayload model(ForecastAreaPayloadModelEnum model);

  ForecastAreaPayload field(String field);

  ForecastAreaPayload threshold(ForecastAreaPayloadThreshold threshold);

  ForecastAreaPayload firstExceedAt(DateTime firstExceedAt);

  ForecastAreaPayload peakValue(num? peakValue);

  ForecastAreaPayload peakAt(DateTime? peakAt);

  ForecastAreaPayload areaKm2(num? areaKm2);

  ForecastAreaPayload run(DateTime run);

  ForecastAreaPayload attribution(String? attribution);

  ForecastAreaPayload automated(bool? automated);

  ForecastAreaPayload notAWarning(bool? notAWarning);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastAreaPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastAreaPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastAreaPayload call({
    ForecastAreaPayloadModelEnum model,
    String field,
    ForecastAreaPayloadThreshold threshold,
    DateTime firstExceedAt,
    num? peakValue,
    DateTime? peakAt,
    num? areaKm2,
    DateTime run,
    String? attribution,
    bool? automated,
    bool? notAWarning,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastAreaPayload.copyWith(...)` or call `instanceOfForecastAreaPayload.copyWith.fieldName(value)` for a single field.
class _$ForecastAreaPayloadCWProxyImpl implements _$ForecastAreaPayloadCWProxy {
  const _$ForecastAreaPayloadCWProxyImpl(this._value);

  final ForecastAreaPayload _value;

  @override
  ForecastAreaPayload model(ForecastAreaPayloadModelEnum model) =>
      call(model: model);

  @override
  ForecastAreaPayload field(String field) => call(field: field);

  @override
  ForecastAreaPayload threshold(ForecastAreaPayloadThreshold threshold) =>
      call(threshold: threshold);

  @override
  ForecastAreaPayload firstExceedAt(DateTime firstExceedAt) =>
      call(firstExceedAt: firstExceedAt);

  @override
  ForecastAreaPayload peakValue(num? peakValue) => call(peakValue: peakValue);

  @override
  ForecastAreaPayload peakAt(DateTime? peakAt) => call(peakAt: peakAt);

  @override
  ForecastAreaPayload areaKm2(num? areaKm2) => call(areaKm2: areaKm2);

  @override
  ForecastAreaPayload run(DateTime run) => call(run: run);

  @override
  ForecastAreaPayload attribution(String? attribution) =>
      call(attribution: attribution);

  @override
  ForecastAreaPayload automated(bool? automated) => call(automated: automated);

  @override
  ForecastAreaPayload notAWarning(bool? notAWarning) =>
      call(notAWarning: notAWarning);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastAreaPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastAreaPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastAreaPayload call({
    Object? model = const $CopyWithPlaceholder(),
    Object? field = const $CopyWithPlaceholder(),
    Object? threshold = const $CopyWithPlaceholder(),
    Object? firstExceedAt = const $CopyWithPlaceholder(),
    Object? peakValue = const $CopyWithPlaceholder(),
    Object? peakAt = const $CopyWithPlaceholder(),
    Object? areaKm2 = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
  }) {
    return ForecastAreaPayload(
      model: model == const $CopyWithPlaceholder() || model == null
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as ForecastAreaPayloadModelEnum,
      field: field == const $CopyWithPlaceholder() || field == null
          ? _value.field
          // ignore: cast_nullable_to_non_nullable
          : field as String,
      threshold: threshold == const $CopyWithPlaceholder() || threshold == null
          ? _value.threshold
          // ignore: cast_nullable_to_non_nullable
          : threshold as ForecastAreaPayloadThreshold,
      firstExceedAt:
          firstExceedAt == const $CopyWithPlaceholder() || firstExceedAt == null
          ? _value.firstExceedAt
          // ignore: cast_nullable_to_non_nullable
          : firstExceedAt as DateTime,
      peakValue: peakValue == const $CopyWithPlaceholder()
          ? _value.peakValue
          // ignore: cast_nullable_to_non_nullable
          : peakValue as num?,
      peakAt: peakAt == const $CopyWithPlaceholder()
          ? _value.peakAt
          // ignore: cast_nullable_to_non_nullable
          : peakAt as DateTime?,
      areaKm2: areaKm2 == const $CopyWithPlaceholder()
          ? _value.areaKm2
          // ignore: cast_nullable_to_non_nullable
          : areaKm2 as num?,
      run: run == const $CopyWithPlaceholder() || run == null
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as DateTime,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as String?,
      automated: automated == const $CopyWithPlaceholder()
          ? _value.automated
          // ignore: cast_nullable_to_non_nullable
          : automated as bool?,
      notAWarning: notAWarning == const $CopyWithPlaceholder()
          ? _value.notAWarning
          // ignore: cast_nullable_to_non_nullable
          : notAWarning as bool?,
    );
  }
}

extension $ForecastAreaPayloadCopyWith on ForecastAreaPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastAreaPayload.copyWith(...)` or `instanceOfForecastAreaPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastAreaPayloadCWProxy get copyWith =>
      _$ForecastAreaPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastAreaPayload _$ForecastAreaPayloadFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ForecastAreaPayload', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'model',
          'field',
          'threshold',
          'firstExceedAt',
          'run',
        ],
      );
      final val = ForecastAreaPayload(
        model: $checkedConvert(
          'model',
          (v) => $enumDecode(_$ForecastAreaPayloadModelEnumEnumMap, v),
        ),
        field: $checkedConvert('field', (v) => v as String),
        threshold: $checkedConvert(
          'threshold',
          (v) =>
              ForecastAreaPayloadThreshold.fromJson(v as Map<String, dynamic>),
        ),
        firstExceedAt: $checkedConvert(
          'firstExceedAt',
          (v) => DateTime.parse(v as String),
        ),
        peakValue: $checkedConvert('peakValue', (v) => v as num?),
        peakAt: $checkedConvert(
          'peakAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        areaKm2: $checkedConvert('areaKm2', (v) => v as num?),
        run: $checkedConvert('run', (v) => DateTime.parse(v as String)),
        attribution: $checkedConvert('attribution', (v) => v as String?),
        automated: $checkedConvert('automated', (v) => v as bool?),
        notAWarning: $checkedConvert('notAWarning', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$ForecastAreaPayloadToJson(
  ForecastAreaPayload instance,
) => <String, dynamic>{
  'model': _$ForecastAreaPayloadModelEnumEnumMap[instance.model]!,
  'field': instance.field,
  'threshold': instance.threshold.toJson(),
  'firstExceedAt': instance.firstExceedAt.toIso8601String(),
  'peakValue': ?instance.peakValue,
  'peakAt': ?instance.peakAt?.toIso8601String(),
  'areaKm2': ?instance.areaKm2,
  'run': instance.run.toIso8601String(),
  'attribution': ?instance.attribution,
  'automated': ?instance.automated,
  'notAWarning': ?instance.notAWarning,
};

const _$ForecastAreaPayloadModelEnumEnumMap = {
  ForecastAreaPayloadModelEnum.gfs: 'gfs',
  ForecastAreaPayloadModelEnum.hrrr: 'hrrr',
  ForecastAreaPayloadModelEnum.ifs: 'ifs',
  ForecastAreaPayloadModelEnum.aifs: 'aifs',
};
