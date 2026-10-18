// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'window_state_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WindowStatePayloadCWProxy {
  WindowStatePayload ruleId(String ruleId);

  WindowStatePayload name(String? name);

  WindowStatePayload type(WindowStatePayloadTypeEnum type);

  WindowStatePayload groupKey(String groupKey);

  WindowStatePayload state(WindowStatePayloadStateEnum state);

  WindowStatePayload value(num? value);

  WindowStatePayload threshold(num? threshold);

  WindowStatePayload reason(String? reason);

  WindowStatePayload detector(DetectorRef? detector);

  WindowStatePayload automated(bool? automated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WindowStatePayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WindowStatePayload(...).copyWith(id: 12, name: "My name")
  /// ```
  WindowStatePayload call({
    String ruleId,
    String? name,
    WindowStatePayloadTypeEnum type,
    String groupKey,
    WindowStatePayloadStateEnum state,
    num? value,
    num? threshold,
    String? reason,
    DetectorRef? detector,
    bool? automated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWindowStatePayload.copyWith(...)` or call `instanceOfWindowStatePayload.copyWith.fieldName(value)` for a single field.
class _$WindowStatePayloadCWProxyImpl implements _$WindowStatePayloadCWProxy {
  const _$WindowStatePayloadCWProxyImpl(this._value);

  final WindowStatePayload _value;

  @override
  WindowStatePayload ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  WindowStatePayload name(String? name) => call(name: name);

  @override
  WindowStatePayload type(WindowStatePayloadTypeEnum type) => call(type: type);

  @override
  WindowStatePayload groupKey(String groupKey) => call(groupKey: groupKey);

  @override
  WindowStatePayload state(WindowStatePayloadStateEnum state) =>
      call(state: state);

  @override
  WindowStatePayload value(num? value) => call(value: value);

  @override
  WindowStatePayload threshold(num? threshold) => call(threshold: threshold);

  @override
  WindowStatePayload reason(String? reason) => call(reason: reason);

  @override
  WindowStatePayload detector(DetectorRef? detector) =>
      call(detector: detector);

  @override
  WindowStatePayload automated(bool? automated) => call(automated: automated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WindowStatePayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WindowStatePayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WindowStatePayload call({
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? groupKey = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? threshold = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? automated = const $CopyWithPlaceholder(),
  }) {
    return WindowStatePayload(
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as WindowStatePayloadTypeEnum,
      groupKey: groupKey == const $CopyWithPlaceholder() || groupKey == null
          ? _value.groupKey
          // ignore: cast_nullable_to_non_nullable
          : groupKey as String,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as WindowStatePayloadStateEnum,
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num?,
      threshold: threshold == const $CopyWithPlaceholder()
          ? _value.threshold
          // ignore: cast_nullable_to_non_nullable
          : threshold as num?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
      detector: detector == const $CopyWithPlaceholder()
          ? _value.detector
          // ignore: cast_nullable_to_non_nullable
          : detector as DetectorRef?,
      automated: automated == const $CopyWithPlaceholder()
          ? _value.automated
          // ignore: cast_nullable_to_non_nullable
          : automated as bool?,
    );
  }
}

extension $WindowStatePayloadCopyWith on WindowStatePayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWindowStatePayload.copyWith(...)` or `instanceOfWindowStatePayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WindowStatePayloadCWProxy get copyWith =>
      _$WindowStatePayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WindowStatePayload _$WindowStatePayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('WindowStatePayload', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['ruleId', 'type', 'groupKey', 'state']);
  final val = WindowStatePayload(
    ruleId: $checkedConvert('ruleId', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String?),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$WindowStatePayloadTypeEnumEnumMap, v),
    ),
    groupKey: $checkedConvert('groupKey', (v) => v as String),
    state: $checkedConvert(
      'state',
      (v) => $enumDecode(_$WindowStatePayloadStateEnumEnumMap, v),
    ),
    value: $checkedConvert('value', (v) => v as num?),
    threshold: $checkedConvert('threshold', (v) => v as num?),
    reason: $checkedConvert('reason', (v) => v as String?),
    detector: $checkedConvert(
      'detector',
      (v) => v == null ? null : DetectorRef.fromJson(v as Map<String, dynamic>),
    ),
    automated: $checkedConvert('automated', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$WindowStatePayloadToJson(WindowStatePayload instance) =>
    <String, dynamic>{
      'ruleId': instance.ruleId,
      'name': ?instance.name,
      'type': _$WindowStatePayloadTypeEnumEnumMap[instance.type]!,
      'groupKey': instance.groupKey,
      'state': _$WindowStatePayloadStateEnumEnumMap[instance.state]!,
      'value': ?instance.value,
      'threshold': ?instance.threshold,
      'reason': ?instance.reason,
      'detector': ?instance.detector?.toJson(),
      'automated': ?instance.automated,
    };

const _$WindowStatePayloadTypeEnumEnumMap = {
  WindowStatePayloadTypeEnum.count: 'count',
  WindowStatePayloadTypeEnum.absence: 'absence',
  WindowStatePayloadTypeEnum.anomaly: 'anomaly',
};

const _$WindowStatePayloadStateEnumEnumMap = {
  WindowStatePayloadStateEnum.active: 'active',
  WindowStatePayloadStateEnum.clear: 'clear',
  WindowStatePayloadStateEnum.unknown: 'unknown',
  WindowStatePayloadStateEnum.learning: 'learning',
};
