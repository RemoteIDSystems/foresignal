// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'approach_condition.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ApproachConditionCWProxy {
  ApproachCondition type(ApproachConditionTypeEnum type);

  ApproachCondition target(ApproachConditionTarget target);

  ApproachCondition withinMeters(num withinMeters);

  ApproachCondition withinMinutes(num withinMinutes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ApproachCondition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ApproachCondition(...).copyWith(id: 12, name: "My name")
  /// ```
  ApproachCondition call({
    ApproachConditionTypeEnum type,
    ApproachConditionTarget target,
    num withinMeters,
    num withinMinutes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfApproachCondition.copyWith(...)` or call `instanceOfApproachCondition.copyWith.fieldName(value)` for a single field.
class _$ApproachConditionCWProxyImpl implements _$ApproachConditionCWProxy {
  const _$ApproachConditionCWProxyImpl(this._value);

  final ApproachCondition _value;

  @override
  ApproachCondition type(ApproachConditionTypeEnum type) => call(type: type);

  @override
  ApproachCondition target(ApproachConditionTarget target) =>
      call(target: target);

  @override
  ApproachCondition withinMeters(num withinMeters) =>
      call(withinMeters: withinMeters);

  @override
  ApproachCondition withinMinutes(num withinMinutes) =>
      call(withinMinutes: withinMinutes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ApproachCondition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ApproachCondition(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ApproachCondition call({
    Object? type = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? withinMeters = const $CopyWithPlaceholder(),
    Object? withinMinutes = const $CopyWithPlaceholder(),
  }) {
    return ApproachCondition(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ApproachConditionTypeEnum,
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

extension $ApproachConditionCopyWith on ApproachCondition {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfApproachCondition.copyWith(...)` or `instanceOfApproachCondition.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ApproachConditionCWProxy get copyWith =>
      _$ApproachConditionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ApproachCondition _$ApproachConditionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ApproachCondition', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['type', 'target', 'withinMeters', 'withinMinutes'],
      );
      final val = ApproachCondition(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ApproachConditionTypeEnumEnumMap, v),
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

Map<String, dynamic> _$ApproachConditionToJson(ApproachCondition instance) =>
    <String, dynamic>{
      'type': _$ApproachConditionTypeEnumEnumMap[instance.type]!,
      'target': instance.target.toJson(),
      'withinMeters': instance.withinMeters,
      'withinMinutes': instance.withinMinutes,
    };

const _$ApproachConditionTypeEnumEnumMap = {
  ApproachConditionTypeEnum.approach: 'approach',
};
