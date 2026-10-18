// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'base_rate_unconditional.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BaseRateUnconditionalCWProxy {
  BaseRateUnconditional ratePerWindow(num ratePerWindow);

  BaseRateUnconditional pAtLeastOne(num pAtLeastOne);

  BaseRateUnconditional interval(StatInterval interval);

  BaseRateUnconditional nWindows(int? nWindows);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaseRateUnconditional(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaseRateUnconditional(...).copyWith(id: 12, name: "My name")
  /// ```
  BaseRateUnconditional call({
    num ratePerWindow,
    num pAtLeastOne,
    StatInterval interval,
    int? nWindows,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBaseRateUnconditional.copyWith(...)` or call `instanceOfBaseRateUnconditional.copyWith.fieldName(value)` for a single field.
class _$BaseRateUnconditionalCWProxyImpl
    implements _$BaseRateUnconditionalCWProxy {
  const _$BaseRateUnconditionalCWProxyImpl(this._value);

  final BaseRateUnconditional _value;

  @override
  BaseRateUnconditional ratePerWindow(num ratePerWindow) =>
      call(ratePerWindow: ratePerWindow);

  @override
  BaseRateUnconditional pAtLeastOne(num pAtLeastOne) =>
      call(pAtLeastOne: pAtLeastOne);

  @override
  BaseRateUnconditional interval(StatInterval interval) =>
      call(interval: interval);

  @override
  BaseRateUnconditional nWindows(int? nWindows) => call(nWindows: nWindows);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaseRateUnconditional(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaseRateUnconditional(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BaseRateUnconditional call({
    Object? ratePerWindow = const $CopyWithPlaceholder(),
    Object? pAtLeastOne = const $CopyWithPlaceholder(),
    Object? interval = const $CopyWithPlaceholder(),
    Object? nWindows = const $CopyWithPlaceholder(),
  }) {
    return BaseRateUnconditional(
      ratePerWindow:
          ratePerWindow == const $CopyWithPlaceholder() || ratePerWindow == null
          ? _value.ratePerWindow
          // ignore: cast_nullable_to_non_nullable
          : ratePerWindow as num,
      pAtLeastOne:
          pAtLeastOne == const $CopyWithPlaceholder() || pAtLeastOne == null
          ? _value.pAtLeastOne
          // ignore: cast_nullable_to_non_nullable
          : pAtLeastOne as num,
      interval: interval == const $CopyWithPlaceholder() || interval == null
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as StatInterval,
      nWindows: nWindows == const $CopyWithPlaceholder()
          ? _value.nWindows
          // ignore: cast_nullable_to_non_nullable
          : nWindows as int?,
    );
  }
}

extension $BaseRateUnconditionalCopyWith on BaseRateUnconditional {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBaseRateUnconditional.copyWith(...)` or `instanceOfBaseRateUnconditional.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BaseRateUnconditionalCWProxy get copyWith =>
      _$BaseRateUnconditionalCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BaseRateUnconditional _$BaseRateUnconditionalFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BaseRateUnconditional', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['ratePerWindow', 'pAtLeastOne', 'interval'],
  );
  final val = BaseRateUnconditional(
    ratePerWindow: $checkedConvert('ratePerWindow', (v) => v as num),
    pAtLeastOne: $checkedConvert('pAtLeastOne', (v) => v as num),
    interval: $checkedConvert(
      'interval',
      (v) => StatInterval.fromJson(v as Map<String, dynamic>),
    ),
    nWindows: $checkedConvert('nWindows', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$BaseRateUnconditionalToJson(
  BaseRateUnconditional instance,
) => <String, dynamic>{
  'ratePerWindow': instance.ratePerWindow,
  'pAtLeastOne': instance.pAtLeastOne,
  'interval': instance.interval.toJson(),
  'nWindows': ?instance.nWindows,
};
