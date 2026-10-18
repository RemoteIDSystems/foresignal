// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'condition.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ConditionCWProxy {
  Condition path(String? path);

  Condition op(ConditionOpEnum op);

  Condition value(Object? value);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Condition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Condition(...).copyWith(id: 12, name: "My name")
  /// ```
  Condition call({String? path, ConditionOpEnum op, Object? value});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCondition.copyWith(...)` or call `instanceOfCondition.copyWith.fieldName(value)` for a single field.
class _$ConditionCWProxyImpl implements _$ConditionCWProxy {
  const _$ConditionCWProxyImpl(this._value);

  final Condition _value;

  @override
  Condition path(String? path) => call(path: path);

  @override
  Condition op(ConditionOpEnum op) => call(op: op);

  @override
  Condition value(Object? value) => call(value: value);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Condition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Condition(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Condition call({
    Object? path = const $CopyWithPlaceholder(),
    Object? op = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
  }) {
    return Condition(
      path: path == const $CopyWithPlaceholder()
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String?,
      op: op == const $CopyWithPlaceholder() || op == null
          ? _value.op
          // ignore: cast_nullable_to_non_nullable
          : op as ConditionOpEnum,
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as Object?,
    );
  }
}

extension $ConditionCopyWith on Condition {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCondition.copyWith(...)` or `instanceOfCondition.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ConditionCWProxy get copyWith => _$ConditionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Condition _$ConditionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Condition', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['op', 'value']);
      final val = Condition(
        path: $checkedConvert('path', (v) => v as String?),
        op: $checkedConvert(
          'op',
          (v) => $enumDecode(_$ConditionOpEnumEnumMap, v),
        ),
        value: $checkedConvert('value', (v) => v),
      );
      return val;
    });

Map<String, dynamic> _$ConditionToJson(Condition instance) => <String, dynamic>{
  'path': ?instance.path,
  'op': _$ConditionOpEnumEnumMap[instance.op]!,
  'value': instance.value,
};

const _$ConditionOpEnumEnumMap = {
  ConditionOpEnum.greaterThan: '>',
  ConditionOpEnum.greaterThanEqual: '>=',
  ConditionOpEnum.lessThan: '<',
  ConditionOpEnum.lessThanEqual: '<=',
  ConditionOpEnum.equalEqual: '==',
  ConditionOpEnum.exclamationEqual: '!=',
  ConditionOpEnum.in_: 'in',
  ConditionOpEnum.notIn: 'notIn',
  ConditionOpEnum.exists: 'exists',
  ConditionOpEnum.startsWith: 'startsWith',
  ConditionOpEnum.matches: 'matches',
  ConditionOpEnum.inWatchlist: 'inWatchlist',
  ConditionOpEnum.notInWatchlist: 'notInWatchlist',
};
