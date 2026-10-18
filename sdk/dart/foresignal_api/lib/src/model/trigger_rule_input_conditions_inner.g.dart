// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trigger_rule_input_conditions_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TriggerRuleInputConditionsInnerCWProxy {
  TriggerRuleInputConditionsInner path(String? path);

  TriggerRuleInputConditionsInner op(TriggerRuleInputConditionsInnerOpEnum op);

  TriggerRuleInputConditionsInner value(Object? value);

  TriggerRuleInputConditionsInner type(
    TriggerRuleInputConditionsInnerTypeEnum type,
  );

  TriggerRuleInputConditionsInner target(ApproachConditionTarget target);

  TriggerRuleInputConditionsInner withinMeters(num withinMeters);

  TriggerRuleInputConditionsInner withinMinutes(num withinMinutes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerRuleInputConditionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerRuleInputConditionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  TriggerRuleInputConditionsInner call({
    String? path,
    TriggerRuleInputConditionsInnerOpEnum op,
    Object? value,
    TriggerRuleInputConditionsInnerTypeEnum type,
    ApproachConditionTarget target,
    num withinMeters,
    num withinMinutes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTriggerRuleInputConditionsInner.copyWith(...)` or call `instanceOfTriggerRuleInputConditionsInner.copyWith.fieldName(value)` for a single field.
class _$TriggerRuleInputConditionsInnerCWProxyImpl
    implements _$TriggerRuleInputConditionsInnerCWProxy {
  const _$TriggerRuleInputConditionsInnerCWProxyImpl(this._value);

  final TriggerRuleInputConditionsInner _value;

  @override
  TriggerRuleInputConditionsInner path(String? path) => call(path: path);

  @override
  TriggerRuleInputConditionsInner op(
    TriggerRuleInputConditionsInnerOpEnum op,
  ) => call(op: op);

  @override
  TriggerRuleInputConditionsInner value(Object? value) => call(value: value);

  @override
  TriggerRuleInputConditionsInner type(
    TriggerRuleInputConditionsInnerTypeEnum type,
  ) => call(type: type);

  @override
  TriggerRuleInputConditionsInner target(ApproachConditionTarget target) =>
      call(target: target);

  @override
  TriggerRuleInputConditionsInner withinMeters(num withinMeters) =>
      call(withinMeters: withinMeters);

  @override
  TriggerRuleInputConditionsInner withinMinutes(num withinMinutes) =>
      call(withinMinutes: withinMinutes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerRuleInputConditionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerRuleInputConditionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TriggerRuleInputConditionsInner call({
    Object? path = const $CopyWithPlaceholder(),
    Object? op = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? withinMeters = const $CopyWithPlaceholder(),
    Object? withinMinutes = const $CopyWithPlaceholder(),
  }) {
    return TriggerRuleInputConditionsInner(
      path: path == const $CopyWithPlaceholder()
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String?,
      op: op == const $CopyWithPlaceholder() || op == null
          ? _value.op
          // ignore: cast_nullable_to_non_nullable
          : op as TriggerRuleInputConditionsInnerOpEnum,
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as Object?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as TriggerRuleInputConditionsInnerTypeEnum,
      target: target == const $CopyWithPlaceholder() || target == null
          ? _value.target
          // ignore: cast_nullable_to_non_nullable
          : target as ApproachConditionTarget,
      withinMeters:
          withinMeters == const $CopyWithPlaceholder() || withinMeters == null
          ? _value.withinMeters
          // ignore: cast_nullable_to_non_nullable
          : withinMeters as num,
      withinMinutes:
          withinMinutes == const $CopyWithPlaceholder() || withinMinutes == null
          ? _value.withinMinutes
          // ignore: cast_nullable_to_non_nullable
          : withinMinutes as num,
    );
  }
}

extension $TriggerRuleInputConditionsInnerCopyWith
    on TriggerRuleInputConditionsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTriggerRuleInputConditionsInner.copyWith(...)` or `instanceOfTriggerRuleInputConditionsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TriggerRuleInputConditionsInnerCWProxy get copyWith =>
      _$TriggerRuleInputConditionsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TriggerRuleInputConditionsInner _$TriggerRuleInputConditionsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TriggerRuleInputConditionsInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'op',
      'value',
      'type',
      'target',
      'withinMeters',
      'withinMinutes',
    ],
  );
  final val = TriggerRuleInputConditionsInner(
    path: $checkedConvert('path', (v) => v as String?),
    op: $checkedConvert(
      'op',
      (v) => $enumDecode(_$TriggerRuleInputConditionsInnerOpEnumEnumMap, v),
    ),
    value: $checkedConvert('value', (v) => v),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$TriggerRuleInputConditionsInnerTypeEnumEnumMap, v),
    ),
    target: $checkedConvert(
      'target',
      (v) => ApproachConditionTarget.fromJson(v as Map<String, dynamic>),
    ),
    withinMeters: $checkedConvert('withinMeters', (v) => v as num),
    withinMinutes: $checkedConvert('withinMinutes', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$TriggerRuleInputConditionsInnerToJson(
  TriggerRuleInputConditionsInner instance,
) => <String, dynamic>{
  'path': ?instance.path,
  'op': _$TriggerRuleInputConditionsInnerOpEnumEnumMap[instance.op]!,
  'value': instance.value,
  'type': _$TriggerRuleInputConditionsInnerTypeEnumEnumMap[instance.type]!,
  'target': instance.target.toJson(),
  'withinMeters': instance.withinMeters,
  'withinMinutes': instance.withinMinutes,
};

const _$TriggerRuleInputConditionsInnerOpEnumEnumMap = {
  TriggerRuleInputConditionsInnerOpEnum.greaterThan: '>',
  TriggerRuleInputConditionsInnerOpEnum.greaterThanEqual: '>=',
  TriggerRuleInputConditionsInnerOpEnum.lessThan: '<',
  TriggerRuleInputConditionsInnerOpEnum.lessThanEqual: '<=',
  TriggerRuleInputConditionsInnerOpEnum.equalEqual: '==',
  TriggerRuleInputConditionsInnerOpEnum.exclamationEqual: '!=',
  TriggerRuleInputConditionsInnerOpEnum.in_: 'in',
  TriggerRuleInputConditionsInnerOpEnum.notIn: 'notIn',
  TriggerRuleInputConditionsInnerOpEnum.exists: 'exists',
  TriggerRuleInputConditionsInnerOpEnum.startsWith: 'startsWith',
  TriggerRuleInputConditionsInnerOpEnum.matches: 'matches',
  TriggerRuleInputConditionsInnerOpEnum.inWatchlist: 'inWatchlist',
  TriggerRuleInputConditionsInnerOpEnum.notInWatchlist: 'notInWatchlist',
};

const _$TriggerRuleInputConditionsInnerTypeEnumEnumMap = {
  TriggerRuleInputConditionsInnerTypeEnum.approach: 'approach',
};
