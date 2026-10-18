// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'anomaly_window.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnomalyWindowCWProxy {
  AnomalyWindow window(String window);

  AnomalyWindow step(String? step);

  AnomalyWindow distinctBy(String? distinctBy);

  AnomalyWindow groupBy(String? groupBy);

  AnomalyWindow maxGroups(int? maxGroups);

  AnomalyWindow bucket(AnomalyWindowBucketEnum? bucket);

  AnomalyWindow slotMode(AnomalyWindowSlotModeEnum? slotMode);

  AnomalyWindow direction(AnomalyWindowDirectionEnum? direction);

  AnomalyWindow sensitivity(AnomalyWindowSensitivityEnum? sensitivity);

  AnomalyWindow minAbsoluteDelta(num? minAbsoluteDelta);

  AnomalyWindow minWeeks(int? minWeeks);

  AnomalyWindow coverageMin(num? coverageMin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  AnomalyWindow call({
    String window,
    String? step,
    String? distinctBy,
    String? groupBy,
    int? maxGroups,
    AnomalyWindowBucketEnum? bucket,
    AnomalyWindowSlotModeEnum? slotMode,
    AnomalyWindowDirectionEnum? direction,
    AnomalyWindowSensitivityEnum? sensitivity,
    num? minAbsoluteDelta,
    int? minWeeks,
    num? coverageMin,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnomalyWindow.copyWith(...)` or call `instanceOfAnomalyWindow.copyWith.fieldName(value)` for a single field.
class _$AnomalyWindowCWProxyImpl implements _$AnomalyWindowCWProxy {
  const _$AnomalyWindowCWProxyImpl(this._value);

  final AnomalyWindow _value;

  @override
  AnomalyWindow window(String window) => call(window: window);

  @override
  AnomalyWindow step(String? step) => call(step: step);

  @override
  AnomalyWindow distinctBy(String? distinctBy) => call(distinctBy: distinctBy);

  @override
  AnomalyWindow groupBy(String? groupBy) => call(groupBy: groupBy);

  @override
  AnomalyWindow maxGroups(int? maxGroups) => call(maxGroups: maxGroups);

  @override
  AnomalyWindow bucket(AnomalyWindowBucketEnum? bucket) => call(bucket: bucket);

  @override
  AnomalyWindow slotMode(AnomalyWindowSlotModeEnum? slotMode) =>
      call(slotMode: slotMode);

  @override
  AnomalyWindow direction(AnomalyWindowDirectionEnum? direction) =>
      call(direction: direction);

  @override
  AnomalyWindow sensitivity(AnomalyWindowSensitivityEnum? sensitivity) =>
      call(sensitivity: sensitivity);

  @override
  AnomalyWindow minAbsoluteDelta(num? minAbsoluteDelta) =>
      call(minAbsoluteDelta: minAbsoluteDelta);

  @override
  AnomalyWindow minWeeks(int? minWeeks) => call(minWeeks: minWeeks);

  @override
  AnomalyWindow coverageMin(num? coverageMin) => call(coverageMin: coverageMin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnomalyWindow call({
    Object? window = const $CopyWithPlaceholder(),
    Object? step = const $CopyWithPlaceholder(),
    Object? distinctBy = const $CopyWithPlaceholder(),
    Object? groupBy = const $CopyWithPlaceholder(),
    Object? maxGroups = const $CopyWithPlaceholder(),
    Object? bucket = const $CopyWithPlaceholder(),
    Object? slotMode = const $CopyWithPlaceholder(),
    Object? direction = const $CopyWithPlaceholder(),
    Object? sensitivity = const $CopyWithPlaceholder(),
    Object? minAbsoluteDelta = const $CopyWithPlaceholder(),
    Object? minWeeks = const $CopyWithPlaceholder(),
    Object? coverageMin = const $CopyWithPlaceholder(),
  }) {
    return AnomalyWindow(
      window: window == const $CopyWithPlaceholder() || window == null
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as String,
      step: step == const $CopyWithPlaceholder()
          ? _value.step
          // ignore: cast_nullable_to_non_nullable
          : step as String?,
      distinctBy: distinctBy == const $CopyWithPlaceholder()
          ? _value.distinctBy
          // ignore: cast_nullable_to_non_nullable
          : distinctBy as String?,
      groupBy: groupBy == const $CopyWithPlaceholder()
          ? _value.groupBy
          // ignore: cast_nullable_to_non_nullable
          : groupBy as String?,
      maxGroups: maxGroups == const $CopyWithPlaceholder()
          ? _value.maxGroups
          // ignore: cast_nullable_to_non_nullable
          : maxGroups as int?,
      bucket: bucket == const $CopyWithPlaceholder()
          ? _value.bucket
          // ignore: cast_nullable_to_non_nullable
          : bucket as AnomalyWindowBucketEnum?,
      slotMode: slotMode == const $CopyWithPlaceholder()
          ? _value.slotMode
          // ignore: cast_nullable_to_non_nullable
          : slotMode as AnomalyWindowSlotModeEnum?,
      direction: direction == const $CopyWithPlaceholder()
          ? _value.direction
          // ignore: cast_nullable_to_non_nullable
          : direction as AnomalyWindowDirectionEnum?,
      sensitivity: sensitivity == const $CopyWithPlaceholder()
          ? _value.sensitivity
          // ignore: cast_nullable_to_non_nullable
          : sensitivity as AnomalyWindowSensitivityEnum?,
      minAbsoluteDelta: minAbsoluteDelta == const $CopyWithPlaceholder()
          ? _value.minAbsoluteDelta
          // ignore: cast_nullable_to_non_nullable
          : minAbsoluteDelta as num?,
      minWeeks: minWeeks == const $CopyWithPlaceholder()
          ? _value.minWeeks
          // ignore: cast_nullable_to_non_nullable
          : minWeeks as int?,
      coverageMin: coverageMin == const $CopyWithPlaceholder()
          ? _value.coverageMin
          // ignore: cast_nullable_to_non_nullable
          : coverageMin as num?,
    );
  }
}

extension $AnomalyWindowCopyWith on AnomalyWindow {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnomalyWindow.copyWith(...)` or `instanceOfAnomalyWindow.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnomalyWindowCWProxy get copyWith => _$AnomalyWindowCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnomalyWindow _$AnomalyWindowFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnomalyWindow', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['window']);
  final val = AnomalyWindow(
    window: $checkedConvert('window', (v) => v as String),
    step: $checkedConvert('step', (v) => v as String?),
    distinctBy: $checkedConvert('distinctBy', (v) => v as String? ?? 'entity'),
    groupBy: $checkedConvert('groupBy', (v) => v as String?),
    maxGroups: $checkedConvert('maxGroups', (v) => (v as num?)?.toInt() ?? 500),
    bucket: $checkedConvert(
      'bucket',
      (v) => $enumDecodeNullable(_$AnomalyWindowBucketEnumEnumMap, v),
    ),
    slotMode: $checkedConvert(
      'slotMode',
      (v) => $enumDecodeNullable(_$AnomalyWindowSlotModeEnumEnumMap, v),
    ),
    direction: $checkedConvert(
      'direction',
      (v) => $enumDecodeNullable(_$AnomalyWindowDirectionEnumEnumMap, v),
    ),
    sensitivity: $checkedConvert(
      'sensitivity',
      (v) => $enumDecodeNullable(_$AnomalyWindowSensitivityEnumEnumMap, v),
    ),
    minAbsoluteDelta: $checkedConvert(
      'minAbsoluteDelta',
      (v) => v as num? ?? 3,
    ),
    minWeeks: $checkedConvert('minWeeks', (v) => (v as num?)?.toInt() ?? 8),
    coverageMin: $checkedConvert('coverageMin', (v) => v as num? ?? 0.8),
  );
  return val;
});

Map<String, dynamic> _$AnomalyWindowToJson(
  AnomalyWindow instance,
) => <String, dynamic>{
  'window': instance.window,
  'step': ?instance.step,
  'distinctBy': ?instance.distinctBy,
  'groupBy': ?instance.groupBy,
  'maxGroups': ?instance.maxGroups,
  'bucket': ?_$AnomalyWindowBucketEnumEnumMap[instance.bucket],
  'slotMode': ?_$AnomalyWindowSlotModeEnumEnumMap[instance.slotMode],
  'direction': ?_$AnomalyWindowDirectionEnumEnumMap[instance.direction],
  'sensitivity': ?_$AnomalyWindowSensitivityEnumEnumMap[instance.sensitivity],
  'minAbsoluteDelta': ?instance.minAbsoluteDelta,
  'minWeeks': ?instance.minWeeks,
  'coverageMin': ?instance.coverageMin,
};

const _$AnomalyWindowBucketEnumEnumMap = {
  AnomalyWindowBucketEnum.n15m: '15m',
  AnomalyWindowBucketEnum.n1h: '1h',
};

const _$AnomalyWindowSlotModeEnumEnumMap = {
  AnomalyWindowSlotModeEnum.hourOfWeek: 'hour-of-week',
  AnomalyWindowSlotModeEnum.hourOfDay: 'hour-of-day',
};

const _$AnomalyWindowDirectionEnumEnumMap = {
  AnomalyWindowDirectionEnum.up: 'up',
  AnomalyWindowDirectionEnum.down: 'down',
  AnomalyWindowDirectionEnum.both: 'both',
};

const _$AnomalyWindowSensitivityEnumEnumMap = {
  AnomalyWindowSensitivityEnum.low: 'low',
  AnomalyWindowSensitivityEnum.medium: 'medium',
  AnomalyWindowSensitivityEnum.high: 'high',
};
