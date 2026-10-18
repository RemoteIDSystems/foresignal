// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stat_interval.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StatIntervalCWProxy {
  StatInterval lower(num lower);

  StatInterval upper(num upper);

  StatInterval level(num level);

  StatInterval method(StatIntervalMethodEnum method);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatInterval(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatInterval(...).copyWith(id: 12, name: "My name")
  /// ```
  StatInterval call({
    num lower,
    num upper,
    num level,
    StatIntervalMethodEnum method,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStatInterval.copyWith(...)` or call `instanceOfStatInterval.copyWith.fieldName(value)` for a single field.
class _$StatIntervalCWProxyImpl implements _$StatIntervalCWProxy {
  const _$StatIntervalCWProxyImpl(this._value);

  final StatInterval _value;

  @override
  StatInterval lower(num lower) => call(lower: lower);

  @override
  StatInterval upper(num upper) => call(upper: upper);

  @override
  StatInterval level(num level) => call(level: level);

  @override
  StatInterval method(StatIntervalMethodEnum method) => call(method: method);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatInterval(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatInterval(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StatInterval call({
    Object? lower = const $CopyWithPlaceholder(),
    Object? upper = const $CopyWithPlaceholder(),
    Object? level = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
  }) {
    return StatInterval(
      lower: lower == const $CopyWithPlaceholder() || lower == null
          ? _value.lower
          // ignore: cast_nullable_to_non_nullable
          : lower as num,
      upper: upper == const $CopyWithPlaceholder() || upper == null
          ? _value.upper
          // ignore: cast_nullable_to_non_nullable
          : upper as num,
      level: level == const $CopyWithPlaceholder() || level == null
          ? _value.level
          // ignore: cast_nullable_to_non_nullable
          : level as num,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as StatIntervalMethodEnum,
    );
  }
}

extension $StatIntervalCopyWith on StatInterval {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStatInterval.copyWith(...)` or `instanceOfStatInterval.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StatIntervalCWProxy get copyWith => _$StatIntervalCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StatInterval _$StatIntervalFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StatInterval', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['lower', 'upper', 'level', 'method'],
      );
      final val = StatInterval(
        lower: $checkedConvert('lower', (v) => v as num),
        upper: $checkedConvert('upper', (v) => v as num),
        level: $checkedConvert('level', (v) => v as num? ?? 0.95),
        method: $checkedConvert(
          'method',
          (v) => $enumDecode(_$StatIntervalMethodEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$StatIntervalToJson(StatInterval instance) =>
    <String, dynamic>{
      'lower': instance.lower,
      'upper': instance.upper,
      'level': instance.level,
      'method': _$StatIntervalMethodEnumEnumMap[instance.method]!,
    };

const _$StatIntervalMethodEnumEnumMap = {
  StatIntervalMethodEnum.wilson: 'wilson',
  StatIntervalMethodEnum.jeffreys: 'jeffreys',
  StatIntervalMethodEnum.pairedBootstrap: 'paired-bootstrap',
  StatIntervalMethodEnum.empirical: 'empirical',
};
