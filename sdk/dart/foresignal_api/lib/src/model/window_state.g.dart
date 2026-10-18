// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'window_state.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WindowStateCWProxy {
  WindowState groupKey(String groupKey);

  WindowState state(WindowStateStateEnum state);

  WindowState since(DateTime since);

  WindowState lastValue(num? lastValue);

  WindowState threshold(num? threshold);

  WindowState lastEvaluatedAt(DateTime? lastEvaluatedAt);

  WindowState lastWatermarkSeq(int? lastWatermarkSeq);

  WindowState reason(String? reason);

  WindowState lastAlertId(int? lastAlertId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WindowState(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WindowState(...).copyWith(id: 12, name: "My name")
  /// ```
  WindowState call({
    String groupKey,
    WindowStateStateEnum state,
    DateTime since,
    num? lastValue,
    num? threshold,
    DateTime? lastEvaluatedAt,
    int? lastWatermarkSeq,
    String? reason,
    int? lastAlertId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWindowState.copyWith(...)` or call `instanceOfWindowState.copyWith.fieldName(value)` for a single field.
class _$WindowStateCWProxyImpl implements _$WindowStateCWProxy {
  const _$WindowStateCWProxyImpl(this._value);

  final WindowState _value;

  @override
  WindowState groupKey(String groupKey) => call(groupKey: groupKey);

  @override
  WindowState state(WindowStateStateEnum state) => call(state: state);

  @override
  WindowState since(DateTime since) => call(since: since);

  @override
  WindowState lastValue(num? lastValue) => call(lastValue: lastValue);

  @override
  WindowState threshold(num? threshold) => call(threshold: threshold);

  @override
  WindowState lastEvaluatedAt(DateTime? lastEvaluatedAt) =>
      call(lastEvaluatedAt: lastEvaluatedAt);

  @override
  WindowState lastWatermarkSeq(int? lastWatermarkSeq) =>
      call(lastWatermarkSeq: lastWatermarkSeq);

  @override
  WindowState reason(String? reason) => call(reason: reason);

  @override
  WindowState lastAlertId(int? lastAlertId) => call(lastAlertId: lastAlertId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WindowState(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WindowState(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WindowState call({
    Object? groupKey = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? since = const $CopyWithPlaceholder(),
    Object? lastValue = const $CopyWithPlaceholder(),
    Object? threshold = const $CopyWithPlaceholder(),
    Object? lastEvaluatedAt = const $CopyWithPlaceholder(),
    Object? lastWatermarkSeq = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? lastAlertId = const $CopyWithPlaceholder(),
  }) {
    return WindowState(
      groupKey: groupKey == const $CopyWithPlaceholder() || groupKey == null
          ? _value.groupKey
          // ignore: cast_nullable_to_non_nullable
          : groupKey as String,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as WindowStateStateEnum,
      since: since == const $CopyWithPlaceholder() || since == null
          ? _value.since
          // ignore: cast_nullable_to_non_nullable
          : since as DateTime,
      lastValue: lastValue == const $CopyWithPlaceholder()
          ? _value.lastValue
          // ignore: cast_nullable_to_non_nullable
          : lastValue as num?,
      threshold: threshold == const $CopyWithPlaceholder()
          ? _value.threshold
          // ignore: cast_nullable_to_non_nullable
          : threshold as num?,
      lastEvaluatedAt: lastEvaluatedAt == const $CopyWithPlaceholder()
          ? _value.lastEvaluatedAt
          // ignore: cast_nullable_to_non_nullable
          : lastEvaluatedAt as DateTime?,
      lastWatermarkSeq: lastWatermarkSeq == const $CopyWithPlaceholder()
          ? _value.lastWatermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : lastWatermarkSeq as int?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
      lastAlertId: lastAlertId == const $CopyWithPlaceholder()
          ? _value.lastAlertId
          // ignore: cast_nullable_to_non_nullable
          : lastAlertId as int?,
    );
  }
}

extension $WindowStateCopyWith on WindowState {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWindowState.copyWith(...)` or `instanceOfWindowState.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WindowStateCWProxy get copyWith => _$WindowStateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WindowState _$WindowStateFromJson(Map<String, dynamic> json) => $checkedCreate(
  'WindowState',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['groupKey', 'state', 'since']);
    final val = WindowState(
      groupKey: $checkedConvert('groupKey', (v) => v as String),
      state: $checkedConvert(
        'state',
        (v) => $enumDecode(_$WindowStateStateEnumEnumMap, v),
      ),
      since: $checkedConvert('since', (v) => DateTime.parse(v as String)),
      lastValue: $checkedConvert('lastValue', (v) => v as num?),
      threshold: $checkedConvert('threshold', (v) => v as num?),
      lastEvaluatedAt: $checkedConvert(
        'lastEvaluatedAt',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      lastWatermarkSeq: $checkedConvert(
        'lastWatermarkSeq',
        (v) => (v as num?)?.toInt(),
      ),
      reason: $checkedConvert('reason', (v) => v as String?),
      lastAlertId: $checkedConvert('lastAlertId', (v) => (v as num?)?.toInt()),
    );
    return val;
  },
);

Map<String, dynamic> _$WindowStateToJson(WindowState instance) =>
    <String, dynamic>{
      'groupKey': instance.groupKey,
      'state': _$WindowStateStateEnumEnumMap[instance.state]!,
      'since': instance.since.toIso8601String(),
      'lastValue': ?instance.lastValue,
      'threshold': ?instance.threshold,
      'lastEvaluatedAt': ?instance.lastEvaluatedAt?.toIso8601String(),
      'lastWatermarkSeq': ?instance.lastWatermarkSeq,
      'reason': ?instance.reason,
      'lastAlertId': ?instance.lastAlertId,
    };

const _$WindowStateStateEnumEnumMap = {
  WindowStateStateEnum.active: 'active',
  WindowStateStateEnum.clear: 'clear',
  WindowStateStateEnum.unknown: 'unknown',
  WindowStateStateEnum.learning: 'learning',
};
