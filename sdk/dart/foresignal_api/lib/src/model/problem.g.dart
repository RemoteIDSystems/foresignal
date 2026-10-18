// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'problem.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProblemCWProxy {
  Problem type(String? type);

  Problem title(String title);

  Problem status(int status);

  Problem detail(String? detail);

  Problem instance(String? instance);

  Problem code(String? code);

  Problem errors(List<ProblemErrorsInner>? errors);

  Problem purgeHorizon(int? purgeHorizon);

  Problem watermarkSeq(int? watermarkSeq);

  Problem horizon(DateTime? horizon);

  Problem capUsd(num? capUsd);

  Problem spentUsd(num? spentUsd);

  Problem estimateUsd(num? estimateUsd);

  Problem requiredScope(ProblemRequiredScopeEnum? requiredScope);

  Problem class_(ProblemClass_Enum? class_);

  Problem retryAfterSeconds(int? retryAfterSeconds);

  Problem references(ProblemReferences? references);

  Problem reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Problem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Problem(...).copyWith(id: 12, name: "My name")
  /// ```
  Problem call({
    String? type,
    String title,
    int status,
    String? detail,
    String? instance,
    String? code,
    List<ProblemErrorsInner>? errors,
    int? purgeHorizon,
    int? watermarkSeq,
    DateTime? horizon,
    num? capUsd,
    num? spentUsd,
    num? estimateUsd,
    ProblemRequiredScopeEnum? requiredScope,
    ProblemClass_Enum? class_,
    int? retryAfterSeconds,
    ProblemReferences? references,
    String? reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProblem.copyWith(...)` or call `instanceOfProblem.copyWith.fieldName(value)` for a single field.
class _$ProblemCWProxyImpl implements _$ProblemCWProxy {
  const _$ProblemCWProxyImpl(this._value);

  final Problem _value;

  @override
  Problem type(String? type) => call(type: type);

  @override
  Problem title(String title) => call(title: title);

  @override
  Problem status(int status) => call(status: status);

  @override
  Problem detail(String? detail) => call(detail: detail);

  @override
  Problem instance(String? instance) => call(instance: instance);

  @override
  Problem code(String? code) => call(code: code);

  @override
  Problem errors(List<ProblemErrorsInner>? errors) => call(errors: errors);

  @override
  Problem purgeHorizon(int? purgeHorizon) => call(purgeHorizon: purgeHorizon);

  @override
  Problem watermarkSeq(int? watermarkSeq) => call(watermarkSeq: watermarkSeq);

  @override
  Problem horizon(DateTime? horizon) => call(horizon: horizon);

  @override
  Problem capUsd(num? capUsd) => call(capUsd: capUsd);

  @override
  Problem spentUsd(num? spentUsd) => call(spentUsd: spentUsd);

  @override
  Problem estimateUsd(num? estimateUsd) => call(estimateUsd: estimateUsd);

  @override
  Problem requiredScope(ProblemRequiredScopeEnum? requiredScope) =>
      call(requiredScope: requiredScope);

  @override
  Problem class_(ProblemClass_Enum? class_) => call(class_: class_);

  @override
  Problem retryAfterSeconds(int? retryAfterSeconds) =>
      call(retryAfterSeconds: retryAfterSeconds);

  @override
  Problem references(ProblemReferences? references) =>
      call(references: references);

  @override
  Problem reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Problem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Problem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Problem call({
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? detail = const $CopyWithPlaceholder(),
    Object? instance = const $CopyWithPlaceholder(),
    Object? code = const $CopyWithPlaceholder(),
    Object? errors = const $CopyWithPlaceholder(),
    Object? purgeHorizon = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
    Object? horizon = const $CopyWithPlaceholder(),
    Object? capUsd = const $CopyWithPlaceholder(),
    Object? spentUsd = const $CopyWithPlaceholder(),
    Object? estimateUsd = const $CopyWithPlaceholder(),
    Object? requiredScope = const $CopyWithPlaceholder(),
    Object? class_ = const $CopyWithPlaceholder(),
    Object? retryAfterSeconds = const $CopyWithPlaceholder(),
    Object? references = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return Problem(
      type: type == const $CopyWithPlaceholder()
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as String?,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as int,
      detail: detail == const $CopyWithPlaceholder()
          ? _value.detail
          // ignore: cast_nullable_to_non_nullable
          : detail as String?,
      instance: instance == const $CopyWithPlaceholder()
          ? _value.instance
          // ignore: cast_nullable_to_non_nullable
          : instance as String?,
      code: code == const $CopyWithPlaceholder()
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as String?,
      errors: errors == const $CopyWithPlaceholder()
          ? _value.errors
          // ignore: cast_nullable_to_non_nullable
          : errors as List<ProblemErrorsInner>?,
      purgeHorizon: purgeHorizon == const $CopyWithPlaceholder()
          ? _value.purgeHorizon
          // ignore: cast_nullable_to_non_nullable
          : purgeHorizon as int?,
      watermarkSeq: watermarkSeq == const $CopyWithPlaceholder()
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int?,
      horizon: horizon == const $CopyWithPlaceholder()
          ? _value.horizon
          // ignore: cast_nullable_to_non_nullable
          : horizon as DateTime?,
      capUsd: capUsd == const $CopyWithPlaceholder()
          ? _value.capUsd
          // ignore: cast_nullable_to_non_nullable
          : capUsd as num?,
      spentUsd: spentUsd == const $CopyWithPlaceholder()
          ? _value.spentUsd
          // ignore: cast_nullable_to_non_nullable
          : spentUsd as num?,
      estimateUsd: estimateUsd == const $CopyWithPlaceholder()
          ? _value.estimateUsd
          // ignore: cast_nullable_to_non_nullable
          : estimateUsd as num?,
      requiredScope: requiredScope == const $CopyWithPlaceholder()
          ? _value.requiredScope
          // ignore: cast_nullable_to_non_nullable
          : requiredScope as ProblemRequiredScopeEnum?,
      class_: class_ == const $CopyWithPlaceholder()
          ? _value.class_
          // ignore: cast_nullable_to_non_nullable
          : class_ as ProblemClass_Enum?,
      retryAfterSeconds: retryAfterSeconds == const $CopyWithPlaceholder()
          ? _value.retryAfterSeconds
          // ignore: cast_nullable_to_non_nullable
          : retryAfterSeconds as int?,
      references: references == const $CopyWithPlaceholder()
          ? _value.references
          // ignore: cast_nullable_to_non_nullable
          : references as ProblemReferences?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $ProblemCopyWith on Problem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProblem.copyWith(...)` or `instanceOfProblem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProblemCWProxy get copyWith => _$ProblemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Problem _$ProblemFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Problem', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['title', 'status']);
  final val = Problem(
    type: $checkedConvert('type', (v) => v as String?),
    title: $checkedConvert('title', (v) => v as String),
    status: $checkedConvert('status', (v) => (v as num).toInt()),
    detail: $checkedConvert('detail', (v) => v as String?),
    instance: $checkedConvert('instance', (v) => v as String?),
    code: $checkedConvert('code', (v) => v as String?),
    errors: $checkedConvert(
      'errors',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ProblemErrorsInner.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    purgeHorizon: $checkedConvert('purgeHorizon', (v) => (v as num?)?.toInt()),
    watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num?)?.toInt()),
    horizon: $checkedConvert(
      'horizon',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    capUsd: $checkedConvert('capUsd', (v) => v as num?),
    spentUsd: $checkedConvert('spentUsd', (v) => v as num?),
    estimateUsd: $checkedConvert('estimateUsd', (v) => v as num?),
    requiredScope: $checkedConvert(
      'requiredScope',
      (v) => $enumDecodeNullable(_$ProblemRequiredScopeEnumEnumMap, v),
    ),
    class_: $checkedConvert(
      'class',
      (v) => $enumDecodeNullable(_$ProblemClass_EnumEnumMap, v),
    ),
    retryAfterSeconds: $checkedConvert(
      'retryAfterSeconds',
      (v) => (v as num?)?.toInt(),
    ),
    references: $checkedConvert(
      'references',
      (v) => v == null
          ? null
          : ProblemReferences.fromJson(v as Map<String, dynamic>),
    ),
    reason: $checkedConvert('reason', (v) => v as String?),
  );
  return val;
}, fieldKeyMap: const {'class_': 'class'});

Map<String, dynamic> _$ProblemToJson(Problem instance) => <String, dynamic>{
  'type': ?instance.type,
  'title': instance.title,
  'status': instance.status,
  'detail': ?instance.detail,
  'instance': ?instance.instance,
  'code': ?instance.code,
  'errors': ?instance.errors?.map((e) => e.toJson()).toList(),
  'purgeHorizon': ?instance.purgeHorizon,
  'watermarkSeq': ?instance.watermarkSeq,
  'horizon': ?instance.horizon?.toIso8601String(),
  'capUsd': ?instance.capUsd,
  'spentUsd': ?instance.spentUsd,
  'estimateUsd': ?instance.estimateUsd,
  'requiredScope': ?_$ProblemRequiredScopeEnumEnumMap[instance.requiredScope],
  'class': ?_$ProblemClass_EnumEnumMap[instance.class_],
  'retryAfterSeconds': ?instance.retryAfterSeconds,
  'references': ?instance.references?.toJson(),
  'reason': ?instance.reason,
};

const _$ProblemRequiredScopeEnumEnumMap = {
  ProblemRequiredScopeEnum.read: 'read',
  ProblemRequiredScopeEnum.agent: 'agent',
  ProblemRequiredScopeEnum.admin: 'admin',
};

const _$ProblemClass_EnumEnumMap = {
  ProblemClass_Enum.read: 'read',
  ProblemClass_Enum.tiles: 'tiles',
  ProblemClass_Enum.stream: 'stream',
  ProblemClass_Enum.expensive: 'expensive',
  ProblemClass_Enum.write: 'write',
  ProblemClass_Enum.anonymous: 'anonymous',
  ProblemClass_Enum.metrics: 'metrics',
};
