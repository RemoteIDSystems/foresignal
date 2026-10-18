// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rule_backtest_result_samples_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RuleBacktestResultSamplesInnerCWProxy {
  RuleBacktestResultSamplesInner seq(int seq);

  RuleBacktestResultSamplesInner source_(String source_);

  RuleBacktestResultSamplesInner externalId(String externalId);

  RuleBacktestResultSamplesInner observedAt(DateTime observedAt);

  RuleBacktestResultSamplesInner edge(
    RuleBacktestResultSamplesInnerEdgeEnum? edge,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResultSamplesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResultSamplesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  RuleBacktestResultSamplesInner call({
    int seq,
    String source_,
    String externalId,
    DateTime observedAt,
    RuleBacktestResultSamplesInnerEdgeEnum? edge,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRuleBacktestResultSamplesInner.copyWith(...)` or call `instanceOfRuleBacktestResultSamplesInner.copyWith.fieldName(value)` for a single field.
class _$RuleBacktestResultSamplesInnerCWProxyImpl
    implements _$RuleBacktestResultSamplesInnerCWProxy {
  const _$RuleBacktestResultSamplesInnerCWProxyImpl(this._value);

  final RuleBacktestResultSamplesInner _value;

  @override
  RuleBacktestResultSamplesInner seq(int seq) => call(seq: seq);

  @override
  RuleBacktestResultSamplesInner source_(String source_) =>
      call(source_: source_);

  @override
  RuleBacktestResultSamplesInner externalId(String externalId) =>
      call(externalId: externalId);

  @override
  RuleBacktestResultSamplesInner observedAt(DateTime observedAt) =>
      call(observedAt: observedAt);

  @override
  RuleBacktestResultSamplesInner edge(
    RuleBacktestResultSamplesInnerEdgeEnum? edge,
  ) => call(edge: edge);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleBacktestResultSamplesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleBacktestResultSamplesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RuleBacktestResultSamplesInner call({
    Object? seq = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? edge = const $CopyWithPlaceholder(),
  }) {
    return RuleBacktestResultSamplesInner(
      seq: seq == const $CopyWithPlaceholder() || seq == null
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int,
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
      edge: edge == const $CopyWithPlaceholder()
          ? _value.edge
          // ignore: cast_nullable_to_non_nullable
          : edge as RuleBacktestResultSamplesInnerEdgeEnum?,
    );
  }
}

extension $RuleBacktestResultSamplesInnerCopyWith
    on RuleBacktestResultSamplesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRuleBacktestResultSamplesInner.copyWith(...)` or `instanceOfRuleBacktestResultSamplesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RuleBacktestResultSamplesInnerCWProxy get copyWith =>
      _$RuleBacktestResultSamplesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RuleBacktestResultSamplesInner _$RuleBacktestResultSamplesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RuleBacktestResultSamplesInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['seq', 'source', 'externalId', 'observedAt'],
  );
  final val = RuleBacktestResultSamplesInner(
    seq: $checkedConvert('seq', (v) => (v as num).toInt()),
    source_: $checkedConvert('source', (v) => v as String),
    externalId: $checkedConvert('externalId', (v) => v as String),
    observedAt: $checkedConvert(
      'observedAt',
      (v) => DateTime.parse(v as String),
    ),
    edge: $checkedConvert(
      'edge',
      (v) => $enumDecodeNullable(
        _$RuleBacktestResultSamplesInnerEdgeEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$RuleBacktestResultSamplesInnerToJson(
  RuleBacktestResultSamplesInner instance,
) => <String, dynamic>{
  'seq': instance.seq,
  'source': instance.source_,
  'externalId': instance.externalId,
  'observedAt': instance.observedAt.toIso8601String(),
  'edge': ?_$RuleBacktestResultSamplesInnerEdgeEnumEnumMap[instance.edge],
};

const _$RuleBacktestResultSamplesInnerEdgeEnumEnumMap = {
  RuleBacktestResultSamplesInnerEdgeEnum.match: 'match',
  RuleBacktestResultSamplesInnerEdgeEnum.enter: 'enter',
  RuleBacktestResultSamplesInnerEdgeEnum.exit: 'exit',
  RuleBacktestResultSamplesInnerEdgeEnum.window: 'window',
};
