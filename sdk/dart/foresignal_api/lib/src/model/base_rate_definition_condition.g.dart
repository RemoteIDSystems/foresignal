// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'base_rate_definition_condition.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BaseRateDefinitionConditionCWProxy {
  BaseRateDefinitionCondition family(String family);

  BaseRateDefinitionCondition withinDays(int withinDays);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaseRateDefinitionCondition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaseRateDefinitionCondition(...).copyWith(id: 12, name: "My name")
  /// ```
  BaseRateDefinitionCondition call({String family, int withinDays});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBaseRateDefinitionCondition.copyWith(...)` or call `instanceOfBaseRateDefinitionCondition.copyWith.fieldName(value)` for a single field.
class _$BaseRateDefinitionConditionCWProxyImpl
    implements _$BaseRateDefinitionConditionCWProxy {
  const _$BaseRateDefinitionConditionCWProxyImpl(this._value);

  final BaseRateDefinitionCondition _value;

  @override
  BaseRateDefinitionCondition family(String family) => call(family: family);

  @override
  BaseRateDefinitionCondition withinDays(int withinDays) =>
      call(withinDays: withinDays);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaseRateDefinitionCondition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaseRateDefinitionCondition(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BaseRateDefinitionCondition call({
    Object? family = const $CopyWithPlaceholder(),
    Object? withinDays = const $CopyWithPlaceholder(),
  }) {
    return BaseRateDefinitionCondition(
      family: family == const $CopyWithPlaceholder() || family == null
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String,
      withinDays:
          withinDays == const $CopyWithPlaceholder() || withinDays == null
          ? _value.withinDays
          // ignore: cast_nullable_to_non_nullable
          : withinDays as int,
    );
  }
}

extension $BaseRateDefinitionConditionCopyWith on BaseRateDefinitionCondition {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBaseRateDefinitionCondition.copyWith(...)` or `instanceOfBaseRateDefinitionCondition.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BaseRateDefinitionConditionCWProxy get copyWith =>
      _$BaseRateDefinitionConditionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BaseRateDefinitionCondition _$BaseRateDefinitionConditionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BaseRateDefinitionCondition', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['family', 'withinDays']);
  final val = BaseRateDefinitionCondition(
    family: $checkedConvert('family', (v) => v as String),
    withinDays: $checkedConvert('withinDays', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$BaseRateDefinitionConditionToJson(
  BaseRateDefinitionCondition instance,
) => <String, dynamic>{
  'family': instance.family,
  'withinDays': instance.withinDays,
};
