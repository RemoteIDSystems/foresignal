// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'anomaly_observation_baseline.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnomalyObservationBaselineCWProxy {
  AnomalyObservationBaseline weeks(num weeks);

  AnomalyObservationBaseline slotMode(
    AnomalyObservationBaselineSlotModeEnum slotMode,
  );

  AnomalyObservationBaseline mean(num mean);

  AnomalyObservationBaseline variance(num variance);

  AnomalyObservationBaseline model(AnomalyObservationBaselineModelEnum model);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyObservationBaseline(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyObservationBaseline(...).copyWith(id: 12, name: "My name")
  /// ```
  AnomalyObservationBaseline call({
    num weeks,
    AnomalyObservationBaselineSlotModeEnum slotMode,
    num mean,
    num variance,
    AnomalyObservationBaselineModelEnum model,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnomalyObservationBaseline.copyWith(...)` or call `instanceOfAnomalyObservationBaseline.copyWith.fieldName(value)` for a single field.
class _$AnomalyObservationBaselineCWProxyImpl
    implements _$AnomalyObservationBaselineCWProxy {
  const _$AnomalyObservationBaselineCWProxyImpl(this._value);

  final AnomalyObservationBaseline _value;

  @override
  AnomalyObservationBaseline weeks(num weeks) => call(weeks: weeks);

  @override
  AnomalyObservationBaseline slotMode(
    AnomalyObservationBaselineSlotModeEnum slotMode,
  ) => call(slotMode: slotMode);

  @override
  AnomalyObservationBaseline mean(num mean) => call(mean: mean);

  @override
  AnomalyObservationBaseline variance(num variance) => call(variance: variance);

  @override
  AnomalyObservationBaseline model(AnomalyObservationBaselineModelEnum model) =>
      call(model: model);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyObservationBaseline(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyObservationBaseline(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnomalyObservationBaseline call({
    Object? weeks = const $CopyWithPlaceholder(),
    Object? slotMode = const $CopyWithPlaceholder(),
    Object? mean = const $CopyWithPlaceholder(),
    Object? variance = const $CopyWithPlaceholder(),
    Object? model = const $CopyWithPlaceholder(),
  }) {
    return AnomalyObservationBaseline(
      weeks: weeks == const $CopyWithPlaceholder() || weeks == null
          ? _value.weeks
          // ignore: cast_nullable_to_non_nullable
          : weeks as num,
      slotMode: slotMode == const $CopyWithPlaceholder() || slotMode == null
          ? _value.slotMode
          // ignore: cast_nullable_to_non_nullable
          : slotMode as AnomalyObservationBaselineSlotModeEnum,
      mean: mean == const $CopyWithPlaceholder() || mean == null
          ? _value.mean
          // ignore: cast_nullable_to_non_nullable
          : mean as num,
      variance: variance == const $CopyWithPlaceholder() || variance == null
          ? _value.variance
          // ignore: cast_nullable_to_non_nullable
          : variance as num,
      model: model == const $CopyWithPlaceholder() || model == null
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as AnomalyObservationBaselineModelEnum,
    );
  }
}

extension $AnomalyObservationBaselineCopyWith on AnomalyObservationBaseline {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnomalyObservationBaseline.copyWith(...)` or `instanceOfAnomalyObservationBaseline.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnomalyObservationBaselineCWProxy get copyWith =>
      _$AnomalyObservationBaselineCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnomalyObservationBaseline _$AnomalyObservationBaselineFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnomalyObservationBaseline', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['weeks', 'slotMode', 'mean', 'variance', 'model'],
  );
  final val = AnomalyObservationBaseline(
    weeks: $checkedConvert('weeks', (v) => v as num),
    slotMode: $checkedConvert(
      'slotMode',
      (v) => $enumDecode(_$AnomalyObservationBaselineSlotModeEnumEnumMap, v),
    ),
    mean: $checkedConvert('mean', (v) => v as num),
    variance: $checkedConvert('variance', (v) => v as num),
    model: $checkedConvert(
      'model',
      (v) => $enumDecode(_$AnomalyObservationBaselineModelEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$AnomalyObservationBaselineToJson(
  AnomalyObservationBaseline instance,
) => <String, dynamic>{
  'weeks': instance.weeks,
  'slotMode':
      _$AnomalyObservationBaselineSlotModeEnumEnumMap[instance.slotMode]!,
  'mean': instance.mean,
  'variance': instance.variance,
  'model': _$AnomalyObservationBaselineModelEnumEnumMap[instance.model]!,
};

const _$AnomalyObservationBaselineSlotModeEnumEnumMap = {
  AnomalyObservationBaselineSlotModeEnum.hourOfWeek: 'hour-of-week',
  AnomalyObservationBaselineSlotModeEnum.hourOfDay: 'hour-of-day',
};

const _$AnomalyObservationBaselineModelEnumEnumMap = {
  AnomalyObservationBaselineModelEnum.poisson: 'poisson',
  AnomalyObservationBaselineModelEnum.nb: 'nb',
};
