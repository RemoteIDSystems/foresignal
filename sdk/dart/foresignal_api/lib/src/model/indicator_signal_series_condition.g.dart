// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'indicator_signal_series_condition.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndicatorSignalSeriesConditionCWProxy {
  IndicatorSignalSeriesCondition op(IndicatorSignalSeriesConditionOpEnum op);

  IndicatorSignalSeriesCondition value(num value);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorSignalSeriesCondition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorSignalSeriesCondition(...).copyWith(id: 12, name: "My name")
  /// ```
  IndicatorSignalSeriesCondition call({
    IndicatorSignalSeriesConditionOpEnum op,
    num value,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndicatorSignalSeriesCondition.copyWith(...)` or call `instanceOfIndicatorSignalSeriesCondition.copyWith.fieldName(value)` for a single field.
class _$IndicatorSignalSeriesConditionCWProxyImpl
    implements _$IndicatorSignalSeriesConditionCWProxy {
  const _$IndicatorSignalSeriesConditionCWProxyImpl(this._value);

  final IndicatorSignalSeriesCondition _value;

  @override
  IndicatorSignalSeriesCondition op(IndicatorSignalSeriesConditionOpEnum op) =>
      call(op: op);

  @override
  IndicatorSignalSeriesCondition value(num value) => call(value: value);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorSignalSeriesCondition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorSignalSeriesCondition(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IndicatorSignalSeriesCondition call({
    Object? op = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
  }) {
    return IndicatorSignalSeriesCondition(
      op: op == const $CopyWithPlaceholder() || op == null
          ? _value.op
          // ignore: cast_nullable_to_non_nullable
          : op as IndicatorSignalSeriesConditionOpEnum,
      value: value == const $CopyWithPlaceholder() || value == null
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num,
    );
  }
}

extension $IndicatorSignalSeriesConditionCopyWith
    on IndicatorSignalSeriesCondition {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndicatorSignalSeriesCondition.copyWith(...)` or `instanceOfIndicatorSignalSeriesCondition.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndicatorSignalSeriesConditionCWProxy get copyWith =>
      _$IndicatorSignalSeriesConditionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IndicatorSignalSeriesCondition _$IndicatorSignalSeriesConditionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IndicatorSignalSeriesCondition', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['op', 'value']);
  final val = IndicatorSignalSeriesCondition(
    op: $checkedConvert(
      'op',
      (v) => $enumDecode(_$IndicatorSignalSeriesConditionOpEnumEnumMap, v),
    ),
    value: $checkedConvert('value', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$IndicatorSignalSeriesConditionToJson(
  IndicatorSignalSeriesCondition instance,
) => <String, dynamic>{
  'op': _$IndicatorSignalSeriesConditionOpEnumEnumMap[instance.op]!,
  'value': instance.value,
};

const _$IndicatorSignalSeriesConditionOpEnumEnumMap = {
  IndicatorSignalSeriesConditionOpEnum.gt: 'gt',
  IndicatorSignalSeriesConditionOpEnum.gte: 'gte',
  IndicatorSignalSeriesConditionOpEnum.lt: 'lt',
  IndicatorSignalSeriesConditionOpEnum.lte: 'lte',
};
