// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rule_backtest_result_variants_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RuleBacktestResultVariantsInnerCWProxy {
  RuleBacktestResultVariantsInner index(int index);

  RuleBacktestResultVariantsInner patch_(Map<String, Object> patch_);

  RuleBacktestResultVariantsInner wouldFire(int wouldFire);

  RuleBacktestResultVariantsInner perDay(
    List<RuleBacktestResultPerDayInner>? perDay,
  );

  RuleBacktestResultVariantsInner overlap(RuleBacktestResultOverlap? overlap);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResultVariantsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResultVariantsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  RuleBacktestResultVariantsInner call({
    int index,
    Map<String, Object> patch_,
    int wouldFire,
    List<RuleBacktestResultPerDayInner>? perDay,
    RuleBacktestResultOverlap? overlap,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRuleBacktestResultVariantsInner.copyWith(...)` or call `instanceOfRuleBacktestResultVariantsInner.copyWith.fieldName(value)` for a single field.
class _$RuleBacktestResultVariantsInnerCWProxyImpl
    implements _$RuleBacktestResultVariantsInnerCWProxy {
  const _$RuleBacktestResultVariantsInnerCWProxyImpl(this._value);

  final RuleBacktestResultVariantsInner _value;

  @override
  RuleBacktestResultVariantsInner index(int index) => call(index: index);

  @override
  RuleBacktestResultVariantsInner patch_(Map<String, Object> patch_) =>
      call(patch_: patch_);

  @override
  RuleBacktestResultVariantsInner wouldFire(int wouldFire) =>
      call(wouldFire: wouldFire);

  @override
  RuleBacktestResultVariantsInner perDay(
    List<RuleBacktestResultPerDayInner>? perDay,
  ) => call(perDay: perDay);

  @override
  RuleBacktestResultVariantsInner overlap(RuleBacktestResultOverlap? overlap) =>
      call(overlap: overlap);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResultVariantsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResultVariantsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RuleBacktestResultVariantsInner call({
    Object? index = const $CopyWithPlaceholder(),
    Object? patch_ = const $CopyWithPlaceholder(),
    Object? wouldFire = const $CopyWithPlaceholder(),
    Object? perDay = const $CopyWithPlaceholder(),
    Object? overlap = const $CopyWithPlaceholder(),
  }) {
    return RuleBacktestResultVariantsInner(
      index: index == const $CopyWithPlaceholder() || index == null
          ? _value.index
          // ignore: cast_nullable_to_non_nullable
          : index as int,
      patch_: patch_ == const $CopyWithPlaceholder() || patch_ == null
          ? _value.patch_
          // ignore: cast_nullable_to_non_nullable
          : patch_ as Map<String, Object>,
      wouldFire: wouldFire == const $CopyWithPlaceholder() || wouldFire == null
          ? _value.wouldFire
          // ignore: cast_nullable_to_non_nullable
          : wouldFire as int,
      perDay: perDay == const $CopyWithPlaceholder()
          ? _value.perDay
          // ignore: cast_nullable_to_non_nullable
          : perDay as List<RuleBacktestResultPerDayInner>?,
      overlap: overlap == const $CopyWithPlaceholder()
          ? _value.overlap
          // ignore: cast_nullable_to_non_nullable
          : overlap as RuleBacktestResultOverlap?,
    );
  }
}

extension $RuleBacktestResultVariantsInnerCopyWith
    on RuleBacktestResultVariantsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRuleBacktestResultVariantsInner.copyWith(...)` or `instanceOfRuleBacktestResultVariantsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RuleBacktestResultVariantsInnerCWProxy get copyWith =>
      _$RuleBacktestResultVariantsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RuleBacktestResultVariantsInner _$RuleBacktestResultVariantsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RuleBacktestResultVariantsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['index', 'patch', 'wouldFire']);
  final val = RuleBacktestResultVariantsInner(
    index: $checkedConvert('index', (v) => (v as num).toInt()),
    patch_: $checkedConvert(
      'patch',
      (v) =>
          (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
    ),
    wouldFire: $checkedConvert('wouldFire', (v) => (v as num).toInt()),
    perDay: $checkedConvert(
      'perDay',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => RuleBacktestResultPerDayInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    overlap: $checkedConvert(
      'overlap',
      (v) => v == null
          ? null
          : RuleBacktestResultOverlap.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
}, fieldKeyMap: const {'patch_': 'patch'});

Map<String, dynamic> _$RuleBacktestResultVariantsInnerToJson(
  RuleBacktestResultVariantsInner instance,
) => <String, dynamic>{
  'index': instance.index,
  'patch': instance.patch_,
  'wouldFire': instance.wouldFire,
  'perDay': ?instance.perDay?.map((e) => e.toJson()).toList(),
  'overlap': ?instance.overlap?.toJson(),
};
