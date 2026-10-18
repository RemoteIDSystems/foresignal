// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rule_backtest_result_top_entities_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RuleBacktestResultTopEntitiesInnerCWProxy {
  RuleBacktestResultTopEntitiesInner sourceId(String? sourceId);

  RuleBacktestResultTopEntitiesInner externalId(String? externalId);

  RuleBacktestResultTopEntitiesInner entityId(String? entityId);

  RuleBacktestResultTopEntitiesInner count(int count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResultTopEntitiesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResultTopEntitiesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  RuleBacktestResultTopEntitiesInner call({
    String? sourceId,
    String? externalId,
    String? entityId,
    int count,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRuleBacktestResultTopEntitiesInner.copyWith(...)` or call `instanceOfRuleBacktestResultTopEntitiesInner.copyWith.fieldName(value)` for a single field.
class _$RuleBacktestResultTopEntitiesInnerCWProxyImpl
    implements _$RuleBacktestResultTopEntitiesInnerCWProxy {
  const _$RuleBacktestResultTopEntitiesInnerCWProxyImpl(this._value);

  final RuleBacktestResultTopEntitiesInner _value;

  @override
  RuleBacktestResultTopEntitiesInner sourceId(String? sourceId) =>
      call(sourceId: sourceId);

  @override
  RuleBacktestResultTopEntitiesInner externalId(String? externalId) =>
      call(externalId: externalId);

  @override
  RuleBacktestResultTopEntitiesInner entityId(String? entityId) =>
      call(entityId: entityId);

  @override
  RuleBacktestResultTopEntitiesInner count(int count) => call(count: count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResultTopEntitiesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResultTopEntitiesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RuleBacktestResultTopEntitiesInner call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? entityId = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
  }) {
    return RuleBacktestResultTopEntitiesInner(
      sourceId: sourceId == const $CopyWithPlaceholder()
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String?,
      externalId: externalId == const $CopyWithPlaceholder()
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String?,
      entityId: entityId == const $CopyWithPlaceholder()
          ? _value.entityId
          // ignore: cast_nullable_to_non_nullable
          : entityId as String?,
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
    );
  }
}

extension $RuleBacktestResultTopEntitiesInnerCopyWith
    on RuleBacktestResultTopEntitiesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRuleBacktestResultTopEntitiesInner.copyWith(...)` or `instanceOfRuleBacktestResultTopEntitiesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RuleBacktestResultTopEntitiesInnerCWProxy get copyWith =>
      _$RuleBacktestResultTopEntitiesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RuleBacktestResultTopEntitiesInner _$RuleBacktestResultTopEntitiesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RuleBacktestResultTopEntitiesInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['count']);
  final val = RuleBacktestResultTopEntitiesInner(
    sourceId: $checkedConvert('sourceId', (v) => v as String?),
    externalId: $checkedConvert('externalId', (v) => v as String?),
    entityId: $checkedConvert('entityId', (v) => v as String?),
    count: $checkedConvert('count', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$RuleBacktestResultTopEntitiesInnerToJson(
  RuleBacktestResultTopEntitiesInner instance,
) => <String, dynamic>{
  'sourceId': ?instance.sourceId,
  'externalId': ?instance.externalId,
  'entityId': ?instance.entityId,
  'count': instance.count,
};
