// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'baseline_rebuild_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BaselineRebuildResultCWProxy {
  BaselineRebuildResult ruleId(String ruleId);

  BaselineRebuildResult accepted(bool accepted);

  BaselineRebuildResult weeksAvailable(num? weeksAvailable);

  BaselineRebuildResult backfillFrom(DateTime? backfillFrom);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaselineRebuildResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaselineRebuildResult(...).copyWith(id: 12, name: "My name")
  /// ```
  BaselineRebuildResult call({
    String ruleId,
    bool accepted,
    num? weeksAvailable,
    DateTime? backfillFrom,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBaselineRebuildResult.copyWith(...)` or call `instanceOfBaselineRebuildResult.copyWith.fieldName(value)` for a single field.
class _$BaselineRebuildResultCWProxyImpl
    implements _$BaselineRebuildResultCWProxy {
  const _$BaselineRebuildResultCWProxyImpl(this._value);

  final BaselineRebuildResult _value;

  @override
  BaselineRebuildResult ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  BaselineRebuildResult accepted(bool accepted) => call(accepted: accepted);

  @override
  BaselineRebuildResult weeksAvailable(num? weeksAvailable) =>
      call(weeksAvailable: weeksAvailable);

  @override
  BaselineRebuildResult backfillFrom(DateTime? backfillFrom) =>
      call(backfillFrom: backfillFrom);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaselineRebuildResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaselineRebuildResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BaselineRebuildResult call({
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? accepted = const $CopyWithPlaceholder(),
    Object? weeksAvailable = const $CopyWithPlaceholder(),
    Object? backfillFrom = const $CopyWithPlaceholder(),
  }) {
    return BaselineRebuildResult(
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      accepted: accepted == const $CopyWithPlaceholder() || accepted == null
          ? _value.accepted
          // ignore: cast_nullable_to_non_nullable
          : accepted as bool,
      weeksAvailable: weeksAvailable == const $CopyWithPlaceholder()
          ? _value.weeksAvailable
          // ignore: cast_nullable_to_non_nullable
          : weeksAvailable as num?,
      backfillFrom: backfillFrom == const $CopyWithPlaceholder()
          ? _value.backfillFrom
          // ignore: cast_nullable_to_non_nullable
          : backfillFrom as DateTime?,
    );
  }
}

extension $BaselineRebuildResultCopyWith on BaselineRebuildResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBaselineRebuildResult.copyWith(...)` or `instanceOfBaselineRebuildResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BaselineRebuildResultCWProxy get copyWith =>
      _$BaselineRebuildResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BaselineRebuildResult _$BaselineRebuildResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BaselineRebuildResult', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['ruleId', 'accepted']);
  final val = BaselineRebuildResult(
    ruleId: $checkedConvert('ruleId', (v) => v as String),
    accepted: $checkedConvert('accepted', (v) => v as bool),
    weeksAvailable: $checkedConvert('weeksAvailable', (v) => v as num?),
    backfillFrom: $checkedConvert(
      'backfillFrom',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$BaselineRebuildResultToJson(
  BaselineRebuildResult instance,
) => <String, dynamic>{
  'ruleId': instance.ruleId,
  'accepted': instance.accepted,
  'weeksAvailable': ?instance.weeksAvailable,
  'backfillFrom': ?instance.backfillFrom?.toIso8601String(),
};
