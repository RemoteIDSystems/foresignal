// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'index_fit_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndexFitResultCWProxy {
  IndexFitResult id(String id);

  IndexFitResult indexId(String indexId);

  IndexFitResult status(IndexFitResultStatusEnum status);

  IndexFitResult startedAt(DateTime? startedAt);

  IndexFitResult finishedAt(DateTime? finishedAt);

  IndexFitResult method(String? method);

  IndexFitResult lambda(num? lambda);

  IndexFitResult regions(int? regions);

  IndexFitResult origins(int? origins);

  IndexFitResult proposedWeights(Object? proposedWeights);

  IndexFitResult weightsVersion(int? weightsVersion);

  IndexFitResult skill(RegionalIndexDefSkill? skill);

  IndexFitResult components(List<IndexFitResultComponentsInner>? components);

  IndexFitResult applied(bool? applied);

  IndexFitResult appliedAt(DateTime? appliedAt);

  IndexFitResult manifestId(String? manifestId);

  IndexFitResult error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndexFitResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndexFitResult(...).copyWith(id: 12, name: "My name")
  /// ```
  IndexFitResult call({
    String id,
    String indexId,
    IndexFitResultStatusEnum status,
    DateTime? startedAt,
    DateTime? finishedAt,
    String? method,
    num? lambda,
    int? regions,
    int? origins,
    Object? proposedWeights,
    int? weightsVersion,
    RegionalIndexDefSkill? skill,
    List<IndexFitResultComponentsInner>? components,
    bool? applied,
    DateTime? appliedAt,
    String? manifestId,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndexFitResult.copyWith(...)` or call `instanceOfIndexFitResult.copyWith.fieldName(value)` for a single field.
class _$IndexFitResultCWProxyImpl implements _$IndexFitResultCWProxy {
  const _$IndexFitResultCWProxyImpl(this._value);

  final IndexFitResult _value;

  @override
  IndexFitResult id(String id) => call(id: id);

  @override
  IndexFitResult indexId(String indexId) => call(indexId: indexId);

  @override
  IndexFitResult status(IndexFitResultStatusEnum status) =>
      call(status: status);

  @override
  IndexFitResult startedAt(DateTime? startedAt) => call(startedAt: startedAt);

  @override
  IndexFitResult finishedAt(DateTime? finishedAt) =>
      call(finishedAt: finishedAt);

  @override
  IndexFitResult method(String? method) => call(method: method);

  @override
  IndexFitResult lambda(num? lambda) => call(lambda: lambda);

  @override
  IndexFitResult regions(int? regions) => call(regions: regions);

  @override
  IndexFitResult origins(int? origins) => call(origins: origins);

  @override
  IndexFitResult proposedWeights(Object? proposedWeights) =>
      call(proposedWeights: proposedWeights);

  @override
  IndexFitResult weightsVersion(int? weightsVersion) =>
      call(weightsVersion: weightsVersion);

  @override
  IndexFitResult skill(RegionalIndexDefSkill? skill) => call(skill: skill);

  @override
  IndexFitResult components(List<IndexFitResultComponentsInner>? components) =>
      call(components: components);

  @override
  IndexFitResult applied(bool? applied) => call(applied: applied);

  @override
  IndexFitResult appliedAt(DateTime? appliedAt) => call(appliedAt: appliedAt);

  @override
  IndexFitResult manifestId(String? manifestId) => call(manifestId: manifestId);

  @override
  IndexFitResult error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndexFitResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndexFitResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IndexFitResult call({
    Object? id = const $CopyWithPlaceholder(),
    Object? indexId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? lambda = const $CopyWithPlaceholder(),
    Object? regions = const $CopyWithPlaceholder(),
    Object? origins = const $CopyWithPlaceholder(),
    Object? proposedWeights = const $CopyWithPlaceholder(),
    Object? weightsVersion = const $CopyWithPlaceholder(),
    Object? skill = const $CopyWithPlaceholder(),
    Object? components = const $CopyWithPlaceholder(),
    Object? applied = const $CopyWithPlaceholder(),
    Object? appliedAt = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return IndexFitResult(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      indexId: indexId == const $CopyWithPlaceholder() || indexId == null
          ? _value.indexId
          // ignore: cast_nullable_to_non_nullable
          : indexId as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as IndexFitResultStatusEnum,
      startedAt: startedAt == const $CopyWithPlaceholder()
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime?,
      finishedAt: finishedAt == const $CopyWithPlaceholder()
          ? _value.finishedAt
          // ignore: cast_nullable_to_non_nullable
          : finishedAt as DateTime?,
      method: method == const $CopyWithPlaceholder()
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as String?,
      lambda: lambda == const $CopyWithPlaceholder()
          ? _value.lambda
          // ignore: cast_nullable_to_non_nullable
          : lambda as num?,
      regions: regions == const $CopyWithPlaceholder()
          ? _value.regions
          // ignore: cast_nullable_to_non_nullable
          : regions as int?,
      origins: origins == const $CopyWithPlaceholder()
          ? _value.origins
          // ignore: cast_nullable_to_non_nullable
          : origins as int?,
      proposedWeights: proposedWeights == const $CopyWithPlaceholder()
          ? _value.proposedWeights
          // ignore: cast_nullable_to_non_nullable
          : proposedWeights as Object?,
      weightsVersion: weightsVersion == const $CopyWithPlaceholder()
          ? _value.weightsVersion
          // ignore: cast_nullable_to_non_nullable
          : weightsVersion as int?,
      skill: skill == const $CopyWithPlaceholder()
          ? _value.skill
          // ignore: cast_nullable_to_non_nullable
          : skill as RegionalIndexDefSkill?,
      components: components == const $CopyWithPlaceholder()
          ? _value.components
          // ignore: cast_nullable_to_non_nullable
          : components as List<IndexFitResultComponentsInner>?,
      applied: applied == const $CopyWithPlaceholder()
          ? _value.applied
          // ignore: cast_nullable_to_non_nullable
          : applied as bool?,
      appliedAt: appliedAt == const $CopyWithPlaceholder()
          ? _value.appliedAt
          // ignore: cast_nullable_to_non_nullable
          : appliedAt as DateTime?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $IndexFitResultCopyWith on IndexFitResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndexFitResult.copyWith(...)` or `instanceOfIndexFitResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndexFitResultCWProxy get copyWith => _$IndexFitResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IndexFitResult _$IndexFitResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IndexFitResult', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'indexId', 'status']);
      final val = IndexFitResult(
        id: $checkedConvert('id', (v) => v as String),
        indexId: $checkedConvert('indexId', (v) => v as String),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$IndexFitResultStatusEnumEnumMap, v),
        ),
        startedAt: $checkedConvert(
          'startedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        finishedAt: $checkedConvert(
          'finishedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        method: $checkedConvert(
          'method',
          (v) => v as String? ?? 'L2-regularised NB regression, rolling origin',
        ),
        lambda: $checkedConvert('lambda', (v) => v as num?),
        regions: $checkedConvert('regions', (v) => (v as num?)?.toInt()),
        origins: $checkedConvert('origins', (v) => (v as num?)?.toInt()),
        proposedWeights: $checkedConvert('proposedWeights', (v) => v),
        weightsVersion: $checkedConvert(
          'weightsVersion',
          (v) => (v as num?)?.toInt(),
        ),
        skill: $checkedConvert(
          'skill',
          (v) => v == null
              ? null
              : RegionalIndexDefSkill.fromJson(v as Map<String, dynamic>),
        ),
        components: $checkedConvert(
          'components',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => IndexFitResultComponentsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        applied: $checkedConvert('applied', (v) => v as bool?),
        appliedAt: $checkedConvert(
          'appliedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        manifestId: $checkedConvert('manifestId', (v) => v as String?),
        error: $checkedConvert('error', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$IndexFitResultToJson(IndexFitResult instance) =>
    <String, dynamic>{
      'id': instance.id,
      'indexId': instance.indexId,
      'status': _$IndexFitResultStatusEnumEnumMap[instance.status]!,
      'startedAt': ?instance.startedAt?.toIso8601String(),
      'finishedAt': ?instance.finishedAt?.toIso8601String(),
      'method': ?instance.method,
      'lambda': ?instance.lambda,
      'regions': ?instance.regions,
      'origins': ?instance.origins,
      'proposedWeights': ?instance.proposedWeights,
      'weightsVersion': ?instance.weightsVersion,
      'skill': ?instance.skill?.toJson(),
      'components': ?instance.components?.map((e) => e.toJson()).toList(),
      'applied': ?instance.applied,
      'appliedAt': ?instance.appliedAt?.toIso8601String(),
      'manifestId': ?instance.manifestId,
      'error': ?instance.error,
    };

const _$IndexFitResultStatusEnumEnumMap = {
  IndexFitResultStatusEnum.queued: 'queued',
  IndexFitResultStatusEnum.running: 'running',
  IndexFitResultStatusEnum.done: 'done',
  IndexFitResultStatusEnum.failed: 'failed',
};
