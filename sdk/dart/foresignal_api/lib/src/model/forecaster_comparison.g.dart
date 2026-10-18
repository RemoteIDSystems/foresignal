// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecaster_comparison.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecasterComparisonCWProxy {
  ForecasterComparison a(String a);

  ForecasterComparison b(String b);

  ForecasterComparison category(String? category);

  ForecasterComparison nShared(int nShared);

  ForecasterComparison label(ForecasterComparisonLabelEnum label);

  ForecasterComparison metric(ForecasterComparisonMetricEnum metric);

  ForecasterComparison meanScoreA(num? meanScoreA);

  ForecasterComparison meanScoreB(num? meanScoreB);

  ForecasterComparison meanDifference(num? meanDifference);

  ForecasterComparison interval(StatInterval? interval);

  ForecasterComparison scorerVersion(String scorerVersion);

  ForecasterComparison computedAt(DateTime computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecasterComparison(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecasterComparison(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecasterComparison call({
    String a,
    String b,
    String? category,
    int nShared,
    ForecasterComparisonLabelEnum label,
    ForecasterComparisonMetricEnum metric,
    num? meanScoreA,
    num? meanScoreB,
    num? meanDifference,
    StatInterval? interval,
    String scorerVersion,
    DateTime computedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecasterComparison.copyWith(...)` or call `instanceOfForecasterComparison.copyWith.fieldName(value)` for a single field.
class _$ForecasterComparisonCWProxyImpl
    implements _$ForecasterComparisonCWProxy {
  const _$ForecasterComparisonCWProxyImpl(this._value);

  final ForecasterComparison _value;

  @override
  ForecasterComparison a(String a) => call(a: a);

  @override
  ForecasterComparison b(String b) => call(b: b);

  @override
  ForecasterComparison category(String? category) => call(category: category);

  @override
  ForecasterComparison nShared(int nShared) => call(nShared: nShared);

  @override
  ForecasterComparison label(ForecasterComparisonLabelEnum label) =>
      call(label: label);

  @override
  ForecasterComparison metric(ForecasterComparisonMetricEnum metric) =>
      call(metric: metric);

  @override
  ForecasterComparison meanScoreA(num? meanScoreA) =>
      call(meanScoreA: meanScoreA);

  @override
  ForecasterComparison meanScoreB(num? meanScoreB) =>
      call(meanScoreB: meanScoreB);

  @override
  ForecasterComparison meanDifference(num? meanDifference) =>
      call(meanDifference: meanDifference);

  @override
  ForecasterComparison interval(StatInterval? interval) =>
      call(interval: interval);

  @override
  ForecasterComparison scorerVersion(String scorerVersion) =>
      call(scorerVersion: scorerVersion);

  @override
  ForecasterComparison computedAt(DateTime computedAt) =>
      call(computedAt: computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecasterComparison(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecasterComparison(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecasterComparison call({
    Object? a = const $CopyWithPlaceholder(),
    Object? b = const $CopyWithPlaceholder(),
    Object? category = const $CopyWithPlaceholder(),
    Object? nShared = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? metric = const $CopyWithPlaceholder(),
    Object? meanScoreA = const $CopyWithPlaceholder(),
    Object? meanScoreB = const $CopyWithPlaceholder(),
    Object? meanDifference = const $CopyWithPlaceholder(),
    Object? interval = const $CopyWithPlaceholder(),
    Object? scorerVersion = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
  }) {
    return ForecasterComparison(
      a: a == const $CopyWithPlaceholder() || a == null
          ? _value.a
          // ignore: cast_nullable_to_non_nullable
          : a as String,
      b: b == const $CopyWithPlaceholder() || b == null
          ? _value.b
          // ignore: cast_nullable_to_non_nullable
          : b as String,
      category: category == const $CopyWithPlaceholder()
          ? _value.category
          // ignore: cast_nullable_to_non_nullable
          : category as String?,
      nShared: nShared == const $CopyWithPlaceholder() || nShared == null
          ? _value.nShared
          // ignore: cast_nullable_to_non_nullable
          : nShared as int,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as ForecasterComparisonLabelEnum,
      metric: metric == const $CopyWithPlaceholder() || metric == null
          ? _value.metric
          // ignore: cast_nullable_to_non_nullable
          : metric as ForecasterComparisonMetricEnum,
      meanScoreA: meanScoreA == const $CopyWithPlaceholder()
          ? _value.meanScoreA
          // ignore: cast_nullable_to_non_nullable
          : meanScoreA as num?,
      meanScoreB: meanScoreB == const $CopyWithPlaceholder()
          ? _value.meanScoreB
          // ignore: cast_nullable_to_non_nullable
          : meanScoreB as num?,
      meanDifference: meanDifference == const $CopyWithPlaceholder()
          ? _value.meanDifference
          // ignore: cast_nullable_to_non_nullable
          : meanDifference as num?,
      interval: interval == const $CopyWithPlaceholder()
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as StatInterval?,
      scorerVersion:
          scorerVersion == const $CopyWithPlaceholder() || scorerVersion == null
          ? _value.scorerVersion
          // ignore: cast_nullable_to_non_nullable
          : scorerVersion as String,
      computedAt:
          computedAt == const $CopyWithPlaceholder() || computedAt == null
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime,
    );
  }
}

extension $ForecasterComparisonCopyWith on ForecasterComparison {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecasterComparison.copyWith(...)` or `instanceOfForecasterComparison.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecasterComparisonCWProxy get copyWith =>
      _$ForecasterComparisonCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecasterComparison _$ForecasterComparisonFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecasterComparison', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'a',
      'b',
      'nShared',
      'label',
      'metric',
      'scorerVersion',
      'computedAt',
    ],
  );
  final val = ForecasterComparison(
    a: $checkedConvert('a', (v) => v as String),
    b: $checkedConvert('b', (v) => v as String),
    category: $checkedConvert('category', (v) => v as String?),
    nShared: $checkedConvert('nShared', (v) => (v as num).toInt()),
    label: $checkedConvert(
      'label',
      (v) => $enumDecode(_$ForecasterComparisonLabelEnumEnumMap, v),
    ),
    metric: $checkedConvert(
      'metric',
      (v) => $enumDecode(_$ForecasterComparisonMetricEnumEnumMap, v),
    ),
    meanScoreA: $checkedConvert('meanScoreA', (v) => v as num?),
    meanScoreB: $checkedConvert('meanScoreB', (v) => v as num?),
    meanDifference: $checkedConvert('meanDifference', (v) => v as num?),
    interval: $checkedConvert(
      'interval',
      (v) =>
          v == null ? null : StatInterval.fromJson(v as Map<String, dynamic>),
    ),
    scorerVersion: $checkedConvert('scorerVersion', (v) => v as String),
    computedAt: $checkedConvert(
      'computedAt',
      (v) => DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ForecasterComparisonToJson(
  ForecasterComparison instance,
) => <String, dynamic>{
  'a': instance.a,
  'b': instance.b,
  'category': ?instance.category,
  'nShared': instance.nShared,
  'label': _$ForecasterComparisonLabelEnumEnumMap[instance.label]!,
  'metric': _$ForecasterComparisonMetricEnumEnumMap[instance.metric]!,
  'meanScoreA': ?instance.meanScoreA,
  'meanScoreB': ?instance.meanScoreB,
  'meanDifference': ?instance.meanDifference,
  'interval': ?instance.interval?.toJson(),
  'scorerVersion': instance.scorerVersion,
  'computedAt': instance.computedAt.toIso8601String(),
};

const _$ForecasterComparisonLabelEnumEnumMap = {
  ForecasterComparisonLabelEnum.ok: 'ok',
  ForecasterComparisonLabelEnum.insufficient: 'insufficient',
};

const _$ForecasterComparisonMetricEnumEnumMap = {
  ForecasterComparisonMetricEnum.brier: 'brier',
};
