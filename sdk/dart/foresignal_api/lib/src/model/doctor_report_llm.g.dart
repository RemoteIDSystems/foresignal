// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'doctor_report_llm.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DoctorReportLlmCWProxy {
  DoctorReportLlm status(DoctorReportLlmStatusEnum status);

  DoctorReportLlm provider(String? provider);

  DoctorReportLlm model(String? model);

  DoctorReportLlm tokens(DoctorReportLlmTokens? tokens);

  DoctorReportLlm summary(String? summary);

  DoctorReportLlm likelyCause(String? likelyCause);

  DoctorReportLlm confidence(num? confidence);

  DoctorReportLlm evidence(List<String>? evidence);

  DoctorReportLlm suggestedActions(List<String>? suggestedActions);

  DoctorReportLlm patch_(ProposedPatch? patch_);

  DoctorReportLlm contextTokens(int? contextTokens);

  DoctorReportLlm detail(DoctorReportLlmDetailEnum? detail);

  DoctorReportLlm error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorReportLlm(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorReportLlm(...).copyWith(id: 12, name: "My name")
  /// ```
  DoctorReportLlm call({
    DoctorReportLlmStatusEnum status,
    String? provider,
    String? model,
    DoctorReportLlmTokens? tokens,
    String? summary,
    String? likelyCause,
    num? confidence,
    List<String>? evidence,
    List<String>? suggestedActions,
    ProposedPatch? patch_,
    int? contextTokens,
    DoctorReportLlmDetailEnum? detail,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDoctorReportLlm.copyWith(...)` or call `instanceOfDoctorReportLlm.copyWith.fieldName(value)` for a single field.
class _$DoctorReportLlmCWProxyImpl implements _$DoctorReportLlmCWProxy {
  const _$DoctorReportLlmCWProxyImpl(this._value);

  final DoctorReportLlm _value;

  @override
  DoctorReportLlm status(DoctorReportLlmStatusEnum status) =>
      call(status: status);

  @override
  DoctorReportLlm provider(String? provider) => call(provider: provider);

  @override
  DoctorReportLlm model(String? model) => call(model: model);

  @override
  DoctorReportLlm tokens(DoctorReportLlmTokens? tokens) => call(tokens: tokens);

  @override
  DoctorReportLlm summary(String? summary) => call(summary: summary);

  @override
  DoctorReportLlm likelyCause(String? likelyCause) =>
      call(likelyCause: likelyCause);

  @override
  DoctorReportLlm confidence(num? confidence) => call(confidence: confidence);

  @override
  DoctorReportLlm evidence(List<String>? evidence) => call(evidence: evidence);

  @override
  DoctorReportLlm suggestedActions(List<String>? suggestedActions) =>
      call(suggestedActions: suggestedActions);

  @override
  DoctorReportLlm patch_(ProposedPatch? patch_) => call(patch_: patch_);

  @override
  DoctorReportLlm contextTokens(int? contextTokens) =>
      call(contextTokens: contextTokens);

  @override
  DoctorReportLlm detail(DoctorReportLlmDetailEnum? detail) =>
      call(detail: detail);

  @override
  DoctorReportLlm error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorReportLlm(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorReportLlm(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DoctorReportLlm call({
    Object? status = const $CopyWithPlaceholder(),
    Object? provider = const $CopyWithPlaceholder(),
    Object? model = const $CopyWithPlaceholder(),
    Object? tokens = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
    Object? likelyCause = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? suggestedActions = const $CopyWithPlaceholder(),
    Object? patch_ = const $CopyWithPlaceholder(),
    Object? contextTokens = const $CopyWithPlaceholder(),
    Object? detail = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return DoctorReportLlm(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as DoctorReportLlmStatusEnum,
      provider: provider == const $CopyWithPlaceholder()
          ? _value.provider
          // ignore: cast_nullable_to_non_nullable
          : provider as String?,
      model: model == const $CopyWithPlaceholder()
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as String?,
      tokens: tokens == const $CopyWithPlaceholder()
          ? _value.tokens
          // ignore: cast_nullable_to_non_nullable
          : tokens as DoctorReportLlmTokens?,
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String?,
      likelyCause: likelyCause == const $CopyWithPlaceholder()
          ? _value.likelyCause
          // ignore: cast_nullable_to_non_nullable
          : likelyCause as String?,
      confidence: confidence == const $CopyWithPlaceholder()
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num?,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<String>?,
      suggestedActions: suggestedActions == const $CopyWithPlaceholder()
          ? _value.suggestedActions
          // ignore: cast_nullable_to_non_nullable
          : suggestedActions as List<String>?,
      patch_: patch_ == const $CopyWithPlaceholder()
          ? _value.patch_
          // ignore: cast_nullable_to_non_nullable
          : patch_ as ProposedPatch?,
      contextTokens: contextTokens == const $CopyWithPlaceholder()
          ? _value.contextTokens
          // ignore: cast_nullable_to_non_nullable
          : contextTokens as int?,
      detail: detail == const $CopyWithPlaceholder()
          ? _value.detail
          // ignore: cast_nullable_to_non_nullable
          : detail as DoctorReportLlmDetailEnum?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $DoctorReportLlmCopyWith on DoctorReportLlm {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDoctorReportLlm.copyWith(...)` or `instanceOfDoctorReportLlm.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DoctorReportLlmCWProxy get copyWith => _$DoctorReportLlmCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DoctorReportLlm _$DoctorReportLlmFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DoctorReportLlm', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = DoctorReportLlm(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$DoctorReportLlmStatusEnumEnumMap, v),
        ),
        provider: $checkedConvert('provider', (v) => v as String?),
        model: $checkedConvert('model', (v) => v as String?),
        tokens: $checkedConvert(
          'tokens',
          (v) => v == null
              ? null
              : DoctorReportLlmTokens.fromJson(v as Map<String, dynamic>),
        ),
        summary: $checkedConvert('summary', (v) => v as String?),
        likelyCause: $checkedConvert('likelyCause', (v) => v as String?),
        confidence: $checkedConvert('confidence', (v) => v as num?),
        evidence: $checkedConvert(
          'evidence',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        suggestedActions: $checkedConvert(
          'suggestedActions',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        patch_: $checkedConvert(
          'patch',
          (v) => v == null
              ? null
              : ProposedPatch.fromJson(v as Map<String, dynamic>),
        ),
        contextTokens: $checkedConvert(
          'contextTokens',
          (v) => (v as num?)?.toInt(),
        ),
        detail: $checkedConvert(
          'detail',
          (v) => $enumDecodeNullable(_$DoctorReportLlmDetailEnumEnumMap, v),
        ),
        error: $checkedConvert('error', (v) => v as String?),
      );
      return val;
    }, fieldKeyMap: const {'patch_': 'patch'});

Map<String, dynamic> _$DoctorReportLlmToJson(DoctorReportLlm instance) =>
    <String, dynamic>{
      'status': _$DoctorReportLlmStatusEnumEnumMap[instance.status]!,
      'provider': ?instance.provider,
      'model': ?instance.model,
      'tokens': ?instance.tokens?.toJson(),
      'summary': ?instance.summary,
      'likelyCause': ?instance.likelyCause,
      'confidence': ?instance.confidence,
      'evidence': ?instance.evidence,
      'suggestedActions': ?instance.suggestedActions,
      'patch': ?instance.patch_?.toJson(),
      'contextTokens': ?instance.contextTokens,
      'detail': ?_$DoctorReportLlmDetailEnumEnumMap[instance.detail],
      'error': ?instance.error,
    };

const _$DoctorReportLlmStatusEnumEnumMap = {
  DoctorReportLlmStatusEnum.ok: 'ok',
  DoctorReportLlmStatusEnum.off: 'off',
  DoctorReportLlmStatusEnum.noProvider: 'no-provider',
  DoctorReportLlmStatusEnum.budgetRefused: 'budget-refused',
  DoctorReportLlmStatusEnum.timeout: 'timeout',
  DoctorReportLlmStatusEnum.failed: 'failed',
};

const _$DoctorReportLlmDetailEnumEnumMap = {
  DoctorReportLlmDetailEnum.compact: 'compact',
  DoctorReportLlmDetailEnum.full: 'full',
};
