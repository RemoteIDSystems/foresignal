// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'impact_rate_interval.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ImpactRateIntervalCWProxy {
  ImpactRateInterval lo(num lo);

  ImpactRateInterval hi(num hi);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ImpactRateInterval(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ImpactRateInterval(...).copyWith(id: 12, name: "My name")
  /// ```
  ImpactRateInterval call({num lo, num hi});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfImpactRateInterval.copyWith(...)` or call `instanceOfImpactRateInterval.copyWith.fieldName(value)` for a single field.
class _$ImpactRateIntervalCWProxyImpl implements _$ImpactRateIntervalCWProxy {
  const _$ImpactRateIntervalCWProxyImpl(this._value);

  final ImpactRateInterval _value;

  @override
  ImpactRateInterval lo(num lo) => call(lo: lo);

  @override
  ImpactRateInterval hi(num hi) => call(hi: hi);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ImpactRateInterval(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ImpactRateInterval(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ImpactRateInterval call({
    Object? lo = const $CopyWithPlaceholder(),
    Object? hi = const $CopyWithPlaceholder(),
  }) {
    return ImpactRateInterval(
      lo: lo == const $CopyWithPlaceholder() || lo == null
          ? _value.lo
          // ignore: cast_nullable_to_non_nullable
          : lo as num,
      hi: hi == const $CopyWithPlaceholder() || hi == null
          ? _value.hi
          // ignore: cast_nullable_to_non_nullable
          : hi as num,
    );
  }
}

extension $ImpactRateIntervalCopyWith on ImpactRateInterval {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfImpactRateInterval.copyWith(...)` or `instanceOfImpactRateInterval.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ImpactRateIntervalCWProxy get copyWith =>
      _$ImpactRateIntervalCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ImpactRateInterval _$ImpactRateIntervalFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ImpactRateInterval', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['lo', 'hi']);
      final val = ImpactRateInterval(
        lo: $checkedConvert('lo', (v) => v as num),
        hi: $checkedConvert('hi', (v) => v as num),
      );
      return val;
    });

Map<String, dynamic> _$ImpactRateIntervalToJson(ImpactRateInterval instance) =>
    <String, dynamic>{'lo': instance.lo, 'hi': instance.hi};
