// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'benchmark_status_components_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BenchmarkStatusComponentsInnerCWProxy {
  BenchmarkStatusComponentsInner component(String component);

  BenchmarkStatusComponentsInner componentVersion(String? componentVersion);

  BenchmarkStatusComponentsInner status(
    BenchmarkStatusComponentsInnerStatusEnum status,
  );

  BenchmarkStatusComponentsInner runId(String? runId);

  BenchmarkStatusComponentsInner suite(
    BenchmarkStatusComponentsInnerSuiteEnum? suite,
  );

  BenchmarkStatusComponentsInner finishedAt(DateTime? finishedAt);

  BenchmarkStatusComponentsInner versionChanged(bool? versionChanged);

  BenchmarkStatusComponentsInner gating(
    BenchmarkStatusComponentsInnerGating? gating,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkStatusComponentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkStatusComponentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  BenchmarkStatusComponentsInner call({
    String component,
    String? componentVersion,
    BenchmarkStatusComponentsInnerStatusEnum status,
    String? runId,
    BenchmarkStatusComponentsInnerSuiteEnum? suite,
    DateTime? finishedAt,
    bool? versionChanged,
    BenchmarkStatusComponentsInnerGating? gating,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBenchmarkStatusComponentsInner.copyWith(...)` or call `instanceOfBenchmarkStatusComponentsInner.copyWith.fieldName(value)` for a single field.
class _$BenchmarkStatusComponentsInnerCWProxyImpl
    implements _$BenchmarkStatusComponentsInnerCWProxy {
  const _$BenchmarkStatusComponentsInnerCWProxyImpl(this._value);

  final BenchmarkStatusComponentsInner _value;

  @override
  BenchmarkStatusComponentsInner component(String component) =>
      call(component: component);

  @override
  BenchmarkStatusComponentsInner componentVersion(String? componentVersion) =>
      call(componentVersion: componentVersion);

  @override
  BenchmarkStatusComponentsInner status(
    BenchmarkStatusComponentsInnerStatusEnum status,
  ) => call(status: status);

  @override
  BenchmarkStatusComponentsInner runId(String? runId) => call(runId: runId);

  @override
  BenchmarkStatusComponentsInner suite(
    BenchmarkStatusComponentsInnerSuiteEnum? suite,
  ) => call(suite: suite);

  @override
  BenchmarkStatusComponentsInner finishedAt(DateTime? finishedAt) =>
      call(finishedAt: finishedAt);

  @override
  BenchmarkStatusComponentsInner versionChanged(bool? versionChanged) =>
      call(versionChanged: versionChanged);

  @override
  BenchmarkStatusComponentsInner gating(
    BenchmarkStatusComponentsInnerGating? gating,
  ) => call(gating: gating);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkStatusComponentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkStatusComponentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BenchmarkStatusComponentsInner call({
    Object? component = const $CopyWithPlaceholder(),
    Object? componentVersion = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? runId = const $CopyWithPlaceholder(),
    Object? suite = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
    Object? versionChanged = const $CopyWithPlaceholder(),
    Object? gating = const $CopyWithPlaceholder(),
  }) {
    return BenchmarkStatusComponentsInner(
      component: component == const $CopyWithPlaceholder() || component == null
          ? _value.component
          // ignore: cast_nullable_to_non_nullable
          : component as String,
      componentVersion: componentVersion == const $CopyWithPlaceholder()
          ? _value.componentVersion
          // ignore: cast_nullable_to_non_nullable
          : componentVersion as String?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as BenchmarkStatusComponentsInnerStatusEnum,
      runId: runId == const $CopyWithPlaceholder()
          ? _value.runId
          // ignore: cast_nullable_to_non_nullable
          : runId as String?,
      suite: suite == const $CopyWithPlaceholder()
          ? _value.suite
          // ignore: cast_nullable_to_non_nullable
          : suite as BenchmarkStatusComponentsInnerSuiteEnum?,
      finishedAt: finishedAt == const $CopyWithPlaceholder()
          ? _value.finishedAt
          // ignore: cast_nullable_to_non_nullable
          : finishedAt as DateTime?,
      versionChanged: versionChanged == const $CopyWithPlaceholder()
          ? _value.versionChanged
          // ignore: cast_nullable_to_non_nullable
          : versionChanged as bool?,
      gating: gating == const $CopyWithPlaceholder()
          ? _value.gating
          // ignore: cast_nullable_to_non_nullable
          : gating as BenchmarkStatusComponentsInnerGating?,
    );
  }
}

extension $BenchmarkStatusComponentsInnerCopyWith
    on BenchmarkStatusComponentsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBenchmarkStatusComponentsInner.copyWith(...)` or `instanceOfBenchmarkStatusComponentsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BenchmarkStatusComponentsInnerCWProxy get copyWith =>
      _$BenchmarkStatusComponentsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BenchmarkStatusComponentsInner _$BenchmarkStatusComponentsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BenchmarkStatusComponentsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['component', 'status']);
  final val = BenchmarkStatusComponentsInner(
    component: $checkedConvert('component', (v) => v as String),
    componentVersion: $checkedConvert('componentVersion', (v) => v as String?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$BenchmarkStatusComponentsInnerStatusEnumEnumMap, v),
    ),
    runId: $checkedConvert('runId', (v) => v as String?),
    suite: $checkedConvert(
      'suite',
      (v) => $enumDecodeNullable(
        _$BenchmarkStatusComponentsInnerSuiteEnumEnumMap,
        v,
      ),
    ),
    finishedAt: $checkedConvert(
      'finishedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    versionChanged: $checkedConvert('versionChanged', (v) => v as bool?),
    gating: $checkedConvert(
      'gating',
      (v) => v == null
          ? null
          : BenchmarkStatusComponentsInnerGating.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
  );
  return val;
});

Map<String, dynamic> _$BenchmarkStatusComponentsInnerToJson(
  BenchmarkStatusComponentsInner instance,
) => <String, dynamic>{
  'component': instance.component,
  'componentVersion': ?instance.componentVersion,
  'status': _$BenchmarkStatusComponentsInnerStatusEnumEnumMap[instance.status]!,
  'runId': ?instance.runId,
  'suite': ?_$BenchmarkStatusComponentsInnerSuiteEnumEnumMap[instance.suite],
  'finishedAt': ?instance.finishedAt?.toIso8601String(),
  'versionChanged': ?instance.versionChanged,
  'gating': ?instance.gating?.toJson(),
};

const _$BenchmarkStatusComponentsInnerStatusEnumEnumMap = {
  BenchmarkStatusComponentsInnerStatusEnum.pass: 'pass',
  BenchmarkStatusComponentsInnerStatusEnum.warn: 'warn',
  BenchmarkStatusComponentsInnerStatusEnum.fail: 'fail',
  BenchmarkStatusComponentsInnerStatusEnum.unknown: 'unknown',
};

const _$BenchmarkStatusComponentsInnerSuiteEnumEnumMap = {
  BenchmarkStatusComponentsInnerSuiteEnum.synthetic: 'synthetic',
  BenchmarkStatusComponentsInnerSuiteEnum.historical: 'historical',
  BenchmarkStatusComponentsInnerSuiteEnum.all: 'all',
};
