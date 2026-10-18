// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'benchmark_run.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BenchmarkRunCWProxy {
  BenchmarkRun id(String id);

  BenchmarkRun suite(BenchmarkRunSuiteEnum suite);

  BenchmarkRun suiteVersion(String suiteVersion);

  BenchmarkRun scorerVersion(String? scorerVersion);

  BenchmarkRun seed(int seed);

  BenchmarkRun components(List<String> components);

  BenchmarkRun jobStatus(BenchmarkRunJobStatusEnum jobStatus);

  BenchmarkRun progress(num? progress);

  BenchmarkRun status(BenchmarkRunStatusEnum? status);

  BenchmarkRun results(List<BenchmarkComponentResult>? results);

  BenchmarkRun manifestId(String? manifestId);

  BenchmarkRun createdBy(String? createdBy);

  BenchmarkRun startedAt(DateTime? startedAt);

  BenchmarkRun finishedAt(DateTime? finishedAt);

  BenchmarkRun error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkRun(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkRun(...).copyWith(id: 12, name: "My name")
  /// ```
  BenchmarkRun call({
    String id,
    BenchmarkRunSuiteEnum suite,
    String suiteVersion,
    String? scorerVersion,
    int seed,
    List<String> components,
    BenchmarkRunJobStatusEnum jobStatus,
    num? progress,
    BenchmarkRunStatusEnum? status,
    List<BenchmarkComponentResult>? results,
    String? manifestId,
    String? createdBy,
    DateTime? startedAt,
    DateTime? finishedAt,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBenchmarkRun.copyWith(...)` or call `instanceOfBenchmarkRun.copyWith.fieldName(value)` for a single field.
class _$BenchmarkRunCWProxyImpl implements _$BenchmarkRunCWProxy {
  const _$BenchmarkRunCWProxyImpl(this._value);

  final BenchmarkRun _value;

  @override
  BenchmarkRun id(String id) => call(id: id);

  @override
  BenchmarkRun suite(BenchmarkRunSuiteEnum suite) => call(suite: suite);

  @override
  BenchmarkRun suiteVersion(String suiteVersion) =>
      call(suiteVersion: suiteVersion);

  @override
  BenchmarkRun scorerVersion(String? scorerVersion) =>
      call(scorerVersion: scorerVersion);

  @override
  BenchmarkRun seed(int seed) => call(seed: seed);

  @override
  BenchmarkRun components(List<String> components) =>
      call(components: components);

  @override
  BenchmarkRun jobStatus(BenchmarkRunJobStatusEnum jobStatus) =>
      call(jobStatus: jobStatus);

  @override
  BenchmarkRun progress(num? progress) => call(progress: progress);

  @override
  BenchmarkRun status(BenchmarkRunStatusEnum? status) => call(status: status);

  @override
  BenchmarkRun results(List<BenchmarkComponentResult>? results) =>
      call(results: results);

  @override
  BenchmarkRun manifestId(String? manifestId) => call(manifestId: manifestId);

  @override
  BenchmarkRun createdBy(String? createdBy) => call(createdBy: createdBy);

  @override
  BenchmarkRun startedAt(DateTime? startedAt) => call(startedAt: startedAt);

  @override
  BenchmarkRun finishedAt(DateTime? finishedAt) => call(finishedAt: finishedAt);

  @override
  BenchmarkRun error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkRun(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkRun(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BenchmarkRun call({
    Object? id = const $CopyWithPlaceholder(),
    Object? suite = const $CopyWithPlaceholder(),
    Object? suiteVersion = const $CopyWithPlaceholder(),
    Object? scorerVersion = const $CopyWithPlaceholder(),
    Object? seed = const $CopyWithPlaceholder(),
    Object? components = const $CopyWithPlaceholder(),
    Object? jobStatus = const $CopyWithPlaceholder(),
    Object? progress = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? results = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return BenchmarkRun(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      suite: suite == const $CopyWithPlaceholder() || suite == null
          ? _value.suite
          // ignore: cast_nullable_to_non_nullable
          : suite as BenchmarkRunSuiteEnum,
      suiteVersion:
          suiteVersion == const $CopyWithPlaceholder() || suiteVersion == null
          ? _value.suiteVersion
          // ignore: cast_nullable_to_non_nullable
          : suiteVersion as String,
      scorerVersion: scorerVersion == const $CopyWithPlaceholder()
          ? _value.scorerVersion
          // ignore: cast_nullable_to_non_nullable
          : scorerVersion as String?,
      seed: seed == const $CopyWithPlaceholder() || seed == null
          ? _value.seed
          // ignore: cast_nullable_to_non_nullable
          : seed as int,
      components:
          components == const $CopyWithPlaceholder() || components == null
          ? _value.components
          // ignore: cast_nullable_to_non_nullable
          : components as List<String>,
      jobStatus: jobStatus == const $CopyWithPlaceholder() || jobStatus == null
          ? _value.jobStatus
          // ignore: cast_nullable_to_non_nullable
          : jobStatus as BenchmarkRunJobStatusEnum,
      progress: progress == const $CopyWithPlaceholder()
          ? _value.progress
          // ignore: cast_nullable_to_non_nullable
          : progress as num?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as BenchmarkRunStatusEnum?,
      results: results == const $CopyWithPlaceholder()
          ? _value.results
          // ignore: cast_nullable_to_non_nullable
          : results as List<BenchmarkComponentResult>?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
      createdBy: createdBy == const $CopyWithPlaceholder()
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String?,
      startedAt: startedAt == const $CopyWithPlaceholder()
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime?,
      finishedAt: finishedAt == const $CopyWithPlaceholder()
          ? _value.finishedAt
          // ignore: cast_nullable_to_non_nullable
          : finishedAt as DateTime?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $BenchmarkRunCopyWith on BenchmarkRun {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBenchmarkRun.copyWith(...)` or `instanceOfBenchmarkRun.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BenchmarkRunCWProxy get copyWith => _$BenchmarkRunCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BenchmarkRun _$BenchmarkRunFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BenchmarkRun', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'suite',
          'suiteVersion',
          'seed',
          'components',
          'jobStatus',
          'status',
        ],
      );
      final val = BenchmarkRun(
        id: $checkedConvert('id', (v) => v as String),
        suite: $checkedConvert(
          'suite',
          (v) => $enumDecode(_$BenchmarkRunSuiteEnumEnumMap, v),
        ),
        suiteVersion: $checkedConvert('suiteVersion', (v) => v as String),
        scorerVersion: $checkedConvert('scorerVersion', (v) => v as String?),
        seed: $checkedConvert('seed', (v) => (v as num).toInt()),
        components: $checkedConvert(
          'components',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        jobStatus: $checkedConvert(
          'jobStatus',
          (v) => $enumDecode(_$BenchmarkRunJobStatusEnumEnumMap, v),
        ),
        progress: $checkedConvert('progress', (v) => v as num?),
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$BenchmarkRunStatusEnumEnumMap, v),
        ),
        results: $checkedConvert(
          'results',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => BenchmarkComponentResult.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        manifestId: $checkedConvert('manifestId', (v) => v as String?),
        createdBy: $checkedConvert('createdBy', (v) => v as String?),
        startedAt: $checkedConvert(
          'startedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        finishedAt: $checkedConvert(
          'finishedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        error: $checkedConvert('error', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$BenchmarkRunToJson(BenchmarkRun instance) =>
    <String, dynamic>{
      'id': instance.id,
      'suite': _$BenchmarkRunSuiteEnumEnumMap[instance.suite]!,
      'suiteVersion': instance.suiteVersion,
      'scorerVersion': ?instance.scorerVersion,
      'seed': instance.seed,
      'components': instance.components,
      'jobStatus': _$BenchmarkRunJobStatusEnumEnumMap[instance.jobStatus]!,
      'progress': ?instance.progress,
      'status': _$BenchmarkRunStatusEnumEnumMap[instance.status],
      'results': ?instance.results?.map((e) => e.toJson()).toList(),
      'manifestId': ?instance.manifestId,
      'createdBy': ?instance.createdBy,
      'startedAt': ?instance.startedAt?.toIso8601String(),
      'finishedAt': ?instance.finishedAt?.toIso8601String(),
      'error': ?instance.error,
    };

const _$BenchmarkRunSuiteEnumEnumMap = {
  BenchmarkRunSuiteEnum.synthetic: 'synthetic',
  BenchmarkRunSuiteEnum.historical: 'historical',
  BenchmarkRunSuiteEnum.all: 'all',
};

const _$BenchmarkRunJobStatusEnumEnumMap = {
  BenchmarkRunJobStatusEnum.queued: 'queued',
  BenchmarkRunJobStatusEnum.running: 'running',
  BenchmarkRunJobStatusEnum.done: 'done',
  BenchmarkRunJobStatusEnum.failed: 'failed',
  BenchmarkRunJobStatusEnum.cancelled: 'cancelled',
};

const _$BenchmarkRunStatusEnumEnumMap = {
  BenchmarkRunStatusEnum.pass: 'pass',
  BenchmarkRunStatusEnum.warn: 'warn',
  BenchmarkRunStatusEnum.fail: 'fail',
};
