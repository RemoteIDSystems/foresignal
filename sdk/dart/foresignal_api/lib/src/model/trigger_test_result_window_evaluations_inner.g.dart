// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trigger_test_result_window_evaluations_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TriggerTestResultWindowEvaluationsInnerCWProxy {
  TriggerTestResultWindowEvaluationsInner at(DateTime at);

  TriggerTestResultWindowEvaluationsInner groupKey(String groupKey);

  TriggerTestResultWindowEvaluationsInner value(num? value);

  TriggerTestResultWindowEvaluationsInner state(
    TriggerTestResultWindowEvaluationsInnerStateEnum state,
  );

  TriggerTestResultWindowEvaluationsInner reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerTestResultWindowEvaluationsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerTestResultWindowEvaluationsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  TriggerTestResultWindowEvaluationsInner call({
    DateTime at,
    String groupKey,
    num? value,
    TriggerTestResultWindowEvaluationsInnerStateEnum state,
    String? reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTriggerTestResultWindowEvaluationsInner.copyWith(...)` or call `instanceOfTriggerTestResultWindowEvaluationsInner.copyWith.fieldName(value)` for a single field.
class _$TriggerTestResultWindowEvaluationsInnerCWProxyImpl
    implements _$TriggerTestResultWindowEvaluationsInnerCWProxy {
  const _$TriggerTestResultWindowEvaluationsInnerCWProxyImpl(this._value);

  final TriggerTestResultWindowEvaluationsInner _value;

  @override
  TriggerTestResultWindowEvaluationsInner at(DateTime at) => call(at: at);

  @override
  TriggerTestResultWindowEvaluationsInner groupKey(String groupKey) =>
      call(groupKey: groupKey);

  @override
  TriggerTestResultWindowEvaluationsInner value(num? value) =>
      call(value: value);

  @override
  TriggerTestResultWindowEvaluationsInner state(
    TriggerTestResultWindowEvaluationsInnerStateEnum state,
  ) => call(state: state);

  @override
  TriggerTestResultWindowEvaluationsInner reason(String? reason) =>
      call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerTestResultWindowEvaluationsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerTestResultWindowEvaluationsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TriggerTestResultWindowEvaluationsInner call({
    Object? at = const $CopyWithPlaceholder(),
    Object? groupKey = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return TriggerTestResultWindowEvaluationsInner(
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      groupKey: groupKey == const $CopyWithPlaceholder() || groupKey == null
          ? _value.groupKey
          // ignore: cast_nullable_to_non_nullable
          : groupKey as String,
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num?,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as TriggerTestResultWindowEvaluationsInnerStateEnum,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $TriggerTestResultWindowEvaluationsInnerCopyWith
    on TriggerTestResultWindowEvaluationsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTriggerTestResultWindowEvaluationsInner.copyWith(...)` or `instanceOfTriggerTestResultWindowEvaluationsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TriggerTestResultWindowEvaluationsInnerCWProxy get copyWith =>
      _$TriggerTestResultWindowEvaluationsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TriggerTestResultWindowEvaluationsInner
_$TriggerTestResultWindowEvaluationsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TriggerTestResultWindowEvaluationsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['at', 'groupKey', 'state']);
      final val = TriggerTestResultWindowEvaluationsInner(
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
        groupKey: $checkedConvert('groupKey', (v) => v as String),
        value: $checkedConvert('value', (v) => v as num?),
        state: $checkedConvert(
          'state',
          (v) => $enumDecode(
            _$TriggerTestResultWindowEvaluationsInnerStateEnumEnumMap,
            v,
          ),
        ),
        reason: $checkedConvert('reason', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$TriggerTestResultWindowEvaluationsInnerToJson(
  TriggerTestResultWindowEvaluationsInner instance,
) => <String, dynamic>{
  'at': instance.at.toIso8601String(),
  'groupKey': instance.groupKey,
  'value': ?instance.value,
  'state':
      _$TriggerTestResultWindowEvaluationsInnerStateEnumEnumMap[instance
          .state]!,
  'reason': ?instance.reason,
};

const _$TriggerTestResultWindowEvaluationsInnerStateEnumEnumMap = {
  TriggerTestResultWindowEvaluationsInnerStateEnum.active: 'active',
  TriggerTestResultWindowEvaluationsInnerStateEnum.clear: 'clear',
  TriggerTestResultWindowEvaluationsInnerStateEnum.unknown: 'unknown',
  TriggerTestResultWindowEvaluationsInnerStateEnum.learning: 'learning',
};
