// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'benchmark_component_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BenchmarkComponentResultCWProxy {
  BenchmarkComponentResult component(String component);

  BenchmarkComponentResult componentVersion(String componentVersion);

  BenchmarkComponentResult status(BenchmarkComponentResultStatusEnum status);

  BenchmarkComponentResult metrics(Map<String, num?> metrics);

  BenchmarkComponentResult thresholds(
    List<BenchmarkComponentResultThresholdsInner>? thresholds,
  );

  BenchmarkComponentResult scenarios(
    List<BenchmarkComponentResultScenariosInner>? scenarios,
  );

  BenchmarkComponentResult caveats(List<Caveat>? caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkComponentResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkComponentResult(...).copyWith(id: 12, name: "My name")
  /// ```
  BenchmarkComponentResult call({
    String component,
    String componentVersion,
    BenchmarkComponentResultStatusEnum status,
    Map<String, num?> metrics,
    List<BenchmarkComponentResultThresholdsInner>? thresholds,
    List<BenchmarkComponentResultScenariosInner>? scenarios,
    List<Caveat>? caveats,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBenchmarkComponentResult.copyWith(...)` or call `instanceOfBenchmarkComponentResult.copyWith.fieldName(value)` for a single field.
class _$BenchmarkComponentResultCWProxyImpl
    implements _$BenchmarkComponentResultCWProxy {
  const _$BenchmarkComponentResultCWProxyImpl(this._value);

  final BenchmarkComponentResult _value;

  @override
  BenchmarkComponentResult component(String component) =>
      call(component: component);

  @override
  BenchmarkComponentResult componentVersion(String componentVersion) =>
      call(componentVersion: componentVersion);

  @override
  BenchmarkComponentResult status(BenchmarkComponentResultStatusEnum status) =>
      call(status: status);

  @override
  BenchmarkComponentResult metrics(Map<String, num?> metrics) =>
      call(metrics: metrics);

  @override
  BenchmarkComponentResult thresholds(
    List<BenchmarkComponentResultThresholdsInner>? thresholds,
  ) => call(thresholds: thresholds);

  @override
  BenchmarkComponentResult scenarios(
    List<BenchmarkComponentResultScenariosInner>? scenarios,
  ) => call(scenarios: scenarios);

  @override
  BenchmarkComponentResult caveats(List<Caveat>? caveats) =>
      call(caveats: caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkComponentResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkComponentResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BenchmarkComponentResult call({
    Object? component = const $CopyWithPlaceholder(),
    Object? componentVersion = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? metrics = const $CopyWithPlaceholder(),
    Object? thresholds = const $CopyWithPlaceholder(),
    Object? scenarios = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
  }) {
    return BenchmarkComponentResult(
      component: component == const $CopyWithPlaceholder() || component == null
          ? _value.component
          // ignore: cast_nullable_to_non_nullable
          : component as String,
      componentVersion:
          componentVersion == const $CopyWithPlaceholder() ||
              componentVersion == null
          ? _value.componentVersion
          // ignore: cast_nullable_to_non_nullable
          : componentVersion as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as BenchmarkComponentResultStatusEnum,
      metrics: metrics == const $CopyWithPlaceholder() || metrics == null
          ? _value.metrics
          // ignore: cast_nullable_to_non_nullable
          : metrics as Map<String, num?>,
      thresholds: thresholds == const $CopyWithPlaceholder()
          ? _value.thresholds
          // ignore: cast_nullable_to_non_nullable
          : thresholds as List<BenchmarkComponentResultThresholdsInner>?,
      scenarios: scenarios == const $CopyWithPlaceholder()
          ? _value.scenarios
          // ignore: cast_nullable_to_non_nullable
          : scenarios as List<BenchmarkComponentResultScenariosInner>?,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
    );
  }
}

extension $BenchmarkComponentResultCopyWith on BenchmarkComponentResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBenchmarkComponentResult.copyWith(...)` or `instanceOfBenchmarkComponentResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BenchmarkComponentResultCWProxy get copyWith =>
      _$BenchmarkComponentResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BenchmarkComponentResult _$BenchmarkComponentResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BenchmarkComponentResult', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['component', 'componentVersion', 'status', 'metrics'],
  );
  final val = BenchmarkComponentResult(
    component: $checkedConvert('component', (v) => v as String),
    componentVersion: $checkedConvert('componentVersion', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$BenchmarkComponentResultStatusEnumEnumMap, v),
    ),
    metrics: $checkedConvert(
      'metrics',
      (v) => Map<String, num?>.from(v as Map),
    ),
    thresholds: $checkedConvert(
      'thresholds',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => BenchmarkComponentResultThresholdsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    scenarios: $checkedConvert(
      'scenarios',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => BenchmarkComponentResultScenariosInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    caveats: $checkedConvert(
      'caveats',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$BenchmarkComponentResultToJson(
  BenchmarkComponentResult instance,
) => <String, dynamic>{
  'component': instance.component,
  'componentVersion': instance.componentVersion,
  'status': _$BenchmarkComponentResultStatusEnumEnumMap[instance.status]!,
  'metrics': instance.metrics,
  'thresholds': ?instance.thresholds?.map((e) => e.toJson()).toList(),
  'scenarios': ?instance.scenarios?.map((e) => e.toJson()).toList(),
  'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
};

const _$BenchmarkComponentResultStatusEnumEnumMap = {
  BenchmarkComponentResultStatusEnum.pass: 'pass',
  BenchmarkComponentResultStatusEnum.warn: 'warn',
  BenchmarkComponentResultStatusEnum.fail: 'fail',
};
