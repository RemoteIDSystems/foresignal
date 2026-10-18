// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trigger_rule_input_window.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TriggerRuleInputWindowCWProxy {
  TriggerRuleInputWindow window(String window);

  TriggerRuleInputWindow step(String? step);

  TriggerRuleInputWindow distinctBy(String? distinctBy);

  TriggerRuleInputWindow groupBy(String? groupBy);

  TriggerRuleInputWindow maxGroups(int? maxGroups);

  TriggerRuleInputWindow op(TriggerRuleInputWindowOpEnum op);

  TriggerRuleInputWindow threshold(int threshold);

  TriggerRuleInputWindow coverageMin(num? coverageMin);

  TriggerRuleInputWindow bucket(TriggerRuleInputWindowBucketEnum? bucket);

  TriggerRuleInputWindow slotMode(TriggerRuleInputWindowSlotModeEnum? slotMode);

  TriggerRuleInputWindow direction(
    TriggerRuleInputWindowDirectionEnum? direction,
  );

  TriggerRuleInputWindow sensitivity(
    TriggerRuleInputWindowSensitivityEnum? sensitivity,
  );

  TriggerRuleInputWindow minAbsoluteDelta(num? minAbsoluteDelta);

  TriggerRuleInputWindow minWeeks(int? minWeeks);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerRuleInputWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerRuleInputWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  TriggerRuleInputWindow call({
    String window,
    String? step,
    String? distinctBy,
    String? groupBy,
    int? maxGroups,
    TriggerRuleInputWindowOpEnum op,
    int threshold,
    num? coverageMin,
    TriggerRuleInputWindowBucketEnum? bucket,
    TriggerRuleInputWindowSlotModeEnum? slotMode,
    TriggerRuleInputWindowDirectionEnum? direction,
    TriggerRuleInputWindowSensitivityEnum? sensitivity,
    num? minAbsoluteDelta,
    int? minWeeks,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTriggerRuleInputWindow.copyWith(...)` or call `instanceOfTriggerRuleInputWindow.copyWith.fieldName(value)` for a single field.
class _$TriggerRuleInputWindowCWProxyImpl
    implements _$TriggerRuleInputWindowCWProxy {
  const _$TriggerRuleInputWindowCWProxyImpl(this._value);

  final TriggerRuleInputWindow _value;

  @override
  TriggerRuleInputWindow window(String window) => call(window: window);

  @override
  TriggerRuleInputWindow step(String? step) => call(step: step);

  @override
  TriggerRuleInputWindow distinctBy(String? distinctBy) =>
      call(distinctBy: distinctBy);

  @override
  TriggerRuleInputWindow groupBy(String? groupBy) => call(groupBy: groupBy);

  @override
  TriggerRuleInputWindow maxGroups(int? maxGroups) =>
      call(maxGroups: maxGroups);

  @override
  TriggerRuleInputWindow op(TriggerRuleInputWindowOpEnum op) => call(op: op);

  @override
  TriggerRuleInputWindow threshold(int threshold) => call(threshold: threshold);

  @override
  TriggerRuleInputWindow coverageMin(num? coverageMin) =>
      call(coverageMin: coverageMin);

  @override
  TriggerRuleInputWindow bucket(TriggerRuleInputWindowBucketEnum? bucket) =>
      call(bucket: bucket);

  @override
  TriggerRuleInputWindow slotMode(
    TriggerRuleInputWindowSlotModeEnum? slotMode,
  ) => call(slotMode: slotMode);

  @override
  TriggerRuleInputWindow direction(
    TriggerRuleInputWindowDirectionEnum? direction,
  ) => call(direction: direction);

  @override
  TriggerRuleInputWindow sensitivity(
    TriggerRuleInputWindowSensitivityEnum? sensitivity,
  ) => call(sensitivity: sensitivity);

  @override
  TriggerRuleInputWindow minAbsoluteDelta(num? minAbsoluteDelta) =>
      call(minAbsoluteDelta: minAbsoluteDelta);

  @override
  TriggerRuleInputWindow minWeeks(int? minWeeks) => call(minWeeks: minWeeks);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerRuleInputWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerRuleInputWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TriggerRuleInputWindow call({
    Object? window = const $CopyWithPlaceholder(),
    Object? step = const $CopyWithPlaceholder(),
    Object? distinctBy = const $CopyWithPlaceholder(),
    Object? groupBy = const $CopyWithPlaceholder(),
    Object? maxGroups = const $CopyWithPlaceholder(),
    Object? op = const $CopyWithPlaceholder(),
    Object? threshold = const $CopyWithPlaceholder(),
    Object? coverageMin = const $CopyWithPlaceholder(),
    Object? bucket = const $CopyWithPlaceholder(),
    Object? slotMode = const $CopyWithPlaceholder(),
    Object? direction = const $CopyWithPlaceholder(),
    Object? sensitivity = const $CopyWithPlaceholder(),
    Object? minAbsoluteDelta = const $CopyWithPlaceholder(),
    Object? minWeeks = const $CopyWithPlaceholder(),
  }) {
    return TriggerRuleInputWindow(
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
      op: op == const $CopyWithPlaceholder() || op == null
          ? _value.op
          // ignore: cast_nullable_to_non_nullable
          : op as TriggerRuleInputWindowOpEnum,
      threshold: threshold == const $CopyWithPlaceholder() || threshold == null
          ? _value.threshold
          // ignore: cast_nullable_to_non_nullable
          : threshold as int,
      coverageMin: coverageMin == const $CopyWithPlaceholder()
          ? _value.coverageMin
          // ignore: cast_nullable_to_non_nullable
          : coverageMin as num?,
      bucket: bucket == const $CopyWithPlaceholder()
          ? _value.bucket
          // ignore: cast_nullable_to_non_nullable
          : bucket as TriggerRuleInputWindowBucketEnum?,
      slotMode: slotMode == const $CopyWithPlaceholder()
          ? _value.slotMode
          // ignore: cast_nullable_to_non_nullable
          : slotMode as TriggerRuleInputWindowSlotModeEnum?,
      direction: direction == const $CopyWithPlaceholder()
          ? _value.direction
          // ignore: cast_nullable_to_non_nullable
          : direction as TriggerRuleInputWindowDirectionEnum?,
      sensitivity: sensitivity == const $CopyWithPlaceholder()
          ? _value.sensitivity
          // ignore: cast_nullable_to_non_nullable
          : sensitivity as TriggerRuleInputWindowSensitivityEnum?,
      minAbsoluteDelta: minAbsoluteDelta == const $CopyWithPlaceholder()
          ? _value.minAbsoluteDelta
          // ignore: cast_nullable_to_non_nullable
          : minAbsoluteDelta as num?,
      minWeeks: minWeeks == const $CopyWithPlaceholder()
          ? _value.minWeeks
          // ignore: cast_nullable_to_non_nullable
          : minWeeks as int?,
    );
  }
}

extension $TriggerRuleInputWindowCopyWith on TriggerRuleInputWindow {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTriggerRuleInputWindow.copyWith(...)` or `instanceOfTriggerRuleInputWindow.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TriggerRuleInputWindowCWProxy get copyWith =>
      _$TriggerRuleInputWindowCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TriggerRuleInputWindow _$TriggerRuleInputWindowFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TriggerRuleInputWindow', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['window', 'op', 'threshold']);
  final val = TriggerRuleInputWindow(
    window: $checkedConvert('window', (v) => v as String),
    step: $checkedConvert('step', (v) => v as String?),
    distinctBy: $checkedConvert('distinctBy', (v) => v as String? ?? 'entity'),
    groupBy: $checkedConvert('groupBy', (v) => v as String?),
    maxGroups: $checkedConvert('maxGroups', (v) => (v as num?)?.toInt() ?? 500),
    op: $checkedConvert(
      'op',
      (v) => $enumDecode(_$TriggerRuleInputWindowOpEnumEnumMap, v),
    ),
    threshold: $checkedConvert('threshold', (v) => (v as num).toInt()),
    coverageMin: $checkedConvert('coverageMin', (v) => v as num? ?? 0.8),
    bucket: $checkedConvert(
      'bucket',
      (v) => $enumDecodeNullable(_$TriggerRuleInputWindowBucketEnumEnumMap, v),
    ),
    slotMode: $checkedConvert(
      'slotMode',
      (v) =>
          $enumDecodeNullable(_$TriggerRuleInputWindowSlotModeEnumEnumMap, v),
    ),
    direction: $checkedConvert(
      'direction',
      (v) =>
          $enumDecodeNullable(_$TriggerRuleInputWindowDirectionEnumEnumMap, v),
    ),
    sensitivity: $checkedConvert(
      'sensitivity',
      (v) => $enumDecodeNullable(
        _$TriggerRuleInputWindowSensitivityEnumEnumMap,
        v,
      ),
    ),
    minAbsoluteDelta: $checkedConvert(
      'minAbsoluteDelta',
      (v) => v as num? ?? 3,
    ),
    minWeeks: $checkedConvert('minWeeks', (v) => (v as num?)?.toInt() ?? 8),
  );
  return val;
});

Map<String, dynamic> _$TriggerRuleInputWindowToJson(
  TriggerRuleInputWindow instance,
) => <String, dynamic>{
  'window': instance.window,
  'step': ?instance.step,
  'distinctBy': ?instance.distinctBy,
  'groupBy': ?instance.groupBy,
  'maxGroups': ?instance.maxGroups,
  'op': _$TriggerRuleInputWindowOpEnumEnumMap[instance.op]!,
  'threshold': instance.threshold,
  'coverageMin': ?instance.coverageMin,
  'bucket': ?_$TriggerRuleInputWindowBucketEnumEnumMap[instance.bucket],
  'slotMode': ?_$TriggerRuleInputWindowSlotModeEnumEnumMap[instance.slotMode],
  'direction':
      ?_$TriggerRuleInputWindowDirectionEnumEnumMap[instance.direction],
  'sensitivity':
      ?_$TriggerRuleInputWindowSensitivityEnumEnumMap[instance.sensitivity],
  'minAbsoluteDelta': ?instance.minAbsoluteDelta,
  'minWeeks': ?instance.minWeeks,
};

const _$TriggerRuleInputWindowOpEnumEnumMap = {
  TriggerRuleInputWindowOpEnum.greaterThanEqual: '>=',
  TriggerRuleInputWindowOpEnum.lessThanEqual: '<=',
};

const _$TriggerRuleInputWindowBucketEnumEnumMap = {
  TriggerRuleInputWindowBucketEnum.n15m: '15m',
  TriggerRuleInputWindowBucketEnum.n1h: '1h',
};

const _$TriggerRuleInputWindowSlotModeEnumEnumMap = {
  TriggerRuleInputWindowSlotModeEnum.hourOfWeek: 'hour-of-week',
  TriggerRuleInputWindowSlotModeEnum.hourOfDay: 'hour-of-day',
};

const _$TriggerRuleInputWindowDirectionEnumEnumMap = {
  TriggerRuleInputWindowDirectionEnum.up: 'up',
  TriggerRuleInputWindowDirectionEnum.down: 'down',
  TriggerRuleInputWindowDirectionEnum.both: 'both',
};

const _$TriggerRuleInputWindowSensitivityEnumEnumMap = {
  TriggerRuleInputWindowSensitivityEnum.low: 'low',
  TriggerRuleInputWindowSensitivityEnum.medium: 'medium',
  TriggerRuleInputWindowSensitivityEnum.high: 'high',
};
