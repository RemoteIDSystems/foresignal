// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cycle_run_roles_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CycleRunRolesInnerCWProxy {
  CycleRunRolesInner role(CycleRunRolesInnerRoleEnum role);

  CycleRunRolesInner status(CycleRunRolesInnerStatusEnum status);

  CycleRunRolesInner reason(String? reason);

  CycleRunRolesInner provider(String? provider);

  CycleRunRolesInner model(String? model);

  CycleRunRolesInner effort(Object? effort);

  CycleRunRolesInner inputTokens(int? inputTokens);

  CycleRunRolesInner outputTokens(int? outputTokens);

  CycleRunRolesInner cachedTokens(int? cachedTokens);

  CycleRunRolesInner forecastsSubmitted(int? forecastsSubmitted);

  CycleRunRolesInner proposalIds(List<String>? proposalIds);

  CycleRunRolesInner promptTemplate(
    CycleRunRolesInnerPromptTemplate? promptTemplate,
  );

  CycleRunRolesInner warnings(List<String>? warnings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CycleRunRolesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CycleRunRolesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  CycleRunRolesInner call({
    CycleRunRolesInnerRoleEnum role,
    CycleRunRolesInnerStatusEnum status,
    String? reason,
    String? provider,
    String? model,
    Object? effort,
    int? inputTokens,
    int? outputTokens,
    int? cachedTokens,
    int? forecastsSubmitted,
    List<String>? proposalIds,
    CycleRunRolesInnerPromptTemplate? promptTemplate,
    List<String>? warnings,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCycleRunRolesInner.copyWith(...)` or call `instanceOfCycleRunRolesInner.copyWith.fieldName(value)` for a single field.
class _$CycleRunRolesInnerCWProxyImpl implements _$CycleRunRolesInnerCWProxy {
  const _$CycleRunRolesInnerCWProxyImpl(this._value);

  final CycleRunRolesInner _value;

  @override
  CycleRunRolesInner role(CycleRunRolesInnerRoleEnum role) => call(role: role);

  @override
  CycleRunRolesInner status(CycleRunRolesInnerStatusEnum status) =>
      call(status: status);

  @override
  CycleRunRolesInner reason(String? reason) => call(reason: reason);

  @override
  CycleRunRolesInner provider(String? provider) => call(provider: provider);

  @override
  CycleRunRolesInner model(String? model) => call(model: model);

  @override
  CycleRunRolesInner effort(Object? effort) => call(effort: effort);

  @override
  CycleRunRolesInner inputTokens(int? inputTokens) =>
      call(inputTokens: inputTokens);

  @override
  CycleRunRolesInner outputTokens(int? outputTokens) =>
      call(outputTokens: outputTokens);

  @override
  CycleRunRolesInner cachedTokens(int? cachedTokens) =>
      call(cachedTokens: cachedTokens);

  @override
  CycleRunRolesInner forecastsSubmitted(int? forecastsSubmitted) =>
      call(forecastsSubmitted: forecastsSubmitted);

  @override
  CycleRunRolesInner proposalIds(List<String>? proposalIds) =>
      call(proposalIds: proposalIds);

  @override
  CycleRunRolesInner promptTemplate(
    CycleRunRolesInnerPromptTemplate? promptTemplate,
  ) => call(promptTemplate: promptTemplate);

  @override
  CycleRunRolesInner warnings(List<String>? warnings) =>
      call(warnings: warnings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CycleRunRolesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CycleRunRolesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CycleRunRolesInner call({
    Object? role = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? provider = const $CopyWithPlaceholder(),
    Object? model = const $CopyWithPlaceholder(),
    Object? effort = const $CopyWithPlaceholder(),
    Object? inputTokens = const $CopyWithPlaceholder(),
    Object? outputTokens = const $CopyWithPlaceholder(),
    Object? cachedTokens = const $CopyWithPlaceholder(),
    Object? forecastsSubmitted = const $CopyWithPlaceholder(),
    Object? proposalIds = const $CopyWithPlaceholder(),
    Object? promptTemplate = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
  }) {
    return CycleRunRolesInner(
      role: role == const $CopyWithPlaceholder() || role == null
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as CycleRunRolesInnerRoleEnum,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as CycleRunRolesInnerStatusEnum,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
      provider: provider == const $CopyWithPlaceholder()
          ? _value.provider
          // ignore: cast_nullable_to_non_nullable
          : provider as String?,
      model: model == const $CopyWithPlaceholder()
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as String?,
      effort: effort == const $CopyWithPlaceholder()
          ? _value.effort
          // ignore: cast_nullable_to_non_nullable
          : effort as Object?,
      inputTokens: inputTokens == const $CopyWithPlaceholder()
          ? _value.inputTokens
          // ignore: cast_nullable_to_non_nullable
          : inputTokens as int?,
      outputTokens: outputTokens == const $CopyWithPlaceholder()
          ? _value.outputTokens
          // ignore: cast_nullable_to_non_nullable
          : outputTokens as int?,
      cachedTokens: cachedTokens == const $CopyWithPlaceholder()
          ? _value.cachedTokens
          // ignore: cast_nullable_to_non_nullable
          : cachedTokens as int?,
      forecastsSubmitted: forecastsSubmitted == const $CopyWithPlaceholder()
          ? _value.forecastsSubmitted
          // ignore: cast_nullable_to_non_nullable
          : forecastsSubmitted as int?,
      proposalIds: proposalIds == const $CopyWithPlaceholder()
          ? _value.proposalIds
          // ignore: cast_nullable_to_non_nullable
          : proposalIds as List<String>?,
      promptTemplate: promptTemplate == const $CopyWithPlaceholder()
          ? _value.promptTemplate
          // ignore: cast_nullable_to_non_nullable
          : promptTemplate as CycleRunRolesInnerPromptTemplate?,
      warnings: warnings == const $CopyWithPlaceholder()
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as List<String>?,
    );
  }
}

extension $CycleRunRolesInnerCopyWith on CycleRunRolesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCycleRunRolesInner.copyWith(...)` or `instanceOfCycleRunRolesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CycleRunRolesInnerCWProxy get copyWith =>
      _$CycleRunRolesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CycleRunRolesInner _$CycleRunRolesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CycleRunRolesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['role', 'status']);
  final val = CycleRunRolesInner(
    role: $checkedConvert(
      'role',
      (v) => $enumDecode(_$CycleRunRolesInnerRoleEnumEnumMap, v),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$CycleRunRolesInnerStatusEnumEnumMap, v),
    ),
    reason: $checkedConvert('reason', (v) => v as String?),
    provider: $checkedConvert('provider', (v) => v as String?),
    model: $checkedConvert('model', (v) => v as String?),
    effort: $checkedConvert('effort', (v) => v),
    inputTokens: $checkedConvert('inputTokens', (v) => (v as num?)?.toInt()),
    outputTokens: $checkedConvert('outputTokens', (v) => (v as num?)?.toInt()),
    cachedTokens: $checkedConvert('cachedTokens', (v) => (v as num?)?.toInt()),
    forecastsSubmitted: $checkedConvert(
      'forecastsSubmitted',
      (v) => (v as num?)?.toInt(),
    ),
    proposalIds: $checkedConvert(
      'proposalIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    promptTemplate: $checkedConvert(
      'promptTemplate',
      (v) => v == null
          ? null
          : CycleRunRolesInnerPromptTemplate.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
    warnings: $checkedConvert(
      'warnings',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$CycleRunRolesInnerToJson(CycleRunRolesInner instance) =>
    <String, dynamic>{
      'role': _$CycleRunRolesInnerRoleEnumEnumMap[instance.role]!,
      'status': _$CycleRunRolesInnerStatusEnumEnumMap[instance.status]!,
      'reason': ?instance.reason,
      'provider': ?instance.provider,
      'model': ?instance.model,
      'effort': ?instance.effort,
      'inputTokens': ?instance.inputTokens,
      'outputTokens': ?instance.outputTokens,
      'cachedTokens': ?instance.cachedTokens,
      'forecastsSubmitted': ?instance.forecastsSubmitted,
      'proposalIds': ?instance.proposalIds,
      'promptTemplate': ?instance.promptTemplate?.toJson(),
      'warnings': ?instance.warnings,
    };

const _$CycleRunRolesInnerRoleEnumEnumMap = {
  CycleRunRolesInnerRoleEnum.analyst: 'analyst',
  CycleRunRolesInnerRoleEnum.redTeam: 'red-team',
  CycleRunRolesInnerRoleEnum.forecaster: 'forecaster',
  CycleRunRolesInnerRoleEnum.proposer: 'proposer',
};

const _$CycleRunRolesInnerStatusEnumEnumMap = {
  CycleRunRolesInnerStatusEnum.ok: 'ok',
  CycleRunRolesInnerStatusEnum.skipped: 'skipped',
  CycleRunRolesInnerStatusEnum.failed: 'failed',
  CycleRunRolesInnerStatusEnum.notConfigured: 'not-configured',
};
