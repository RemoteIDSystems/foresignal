// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_set_draft.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisSetDraftCWProxy {
  HypothesisSetDraft set_(HypothesisSetInput set_);

  HypothesisSetDraft valid(bool valid);

  HypothesisSetDraft warnings(List<HypothesisSetDraftWarningsInner> warnings);

  HypothesisSetDraft toolCalls(List<String>? toolCalls);

  HypothesisSetDraft model(String? model);

  HypothesisSetDraft effort(HypothesisSetDraftEffortEnum? effort);

  HypothesisSetDraft usage(HypothesisSetDraftUsage? usage);

  HypothesisSetDraft manifestId(String? manifestId);

  HypothesisSetDraft textOrigin(HypothesisSetDraftTextOriginEnum? textOrigin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetDraft(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetDraft(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisSetDraft call({
    HypothesisSetInput set_,
    bool valid,
    List<HypothesisSetDraftWarningsInner> warnings,
    List<String>? toolCalls,
    String? model,
    HypothesisSetDraftEffortEnum? effort,
    HypothesisSetDraftUsage? usage,
    String? manifestId,
    HypothesisSetDraftTextOriginEnum? textOrigin,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisSetDraft.copyWith(...)` or call `instanceOfHypothesisSetDraft.copyWith.fieldName(value)` for a single field.
class _$HypothesisSetDraftCWProxyImpl implements _$HypothesisSetDraftCWProxy {
  const _$HypothesisSetDraftCWProxyImpl(this._value);

  final HypothesisSetDraft _value;

  @override
  HypothesisSetDraft set_(HypothesisSetInput set_) => call(set_: set_);

  @override
  HypothesisSetDraft valid(bool valid) => call(valid: valid);

  @override
  HypothesisSetDraft warnings(List<HypothesisSetDraftWarningsInner> warnings) =>
      call(warnings: warnings);

  @override
  HypothesisSetDraft toolCalls(List<String>? toolCalls) =>
      call(toolCalls: toolCalls);

  @override
  HypothesisSetDraft model(String? model) => call(model: model);

  @override
  HypothesisSetDraft effort(HypothesisSetDraftEffortEnum? effort) =>
      call(effort: effort);

  @override
  HypothesisSetDraft usage(HypothesisSetDraftUsage? usage) =>
      call(usage: usage);

  @override
  HypothesisSetDraft manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  @override
  HypothesisSetDraft textOrigin(HypothesisSetDraftTextOriginEnum? textOrigin) =>
      call(textOrigin: textOrigin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetDraft(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetDraft(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisSetDraft call({
    Object? set_ = const $CopyWithPlaceholder(),
    Object? valid = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
    Object? toolCalls = const $CopyWithPlaceholder(),
    Object? model = const $CopyWithPlaceholder(),
    Object? effort = const $CopyWithPlaceholder(),
    Object? usage = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
  }) {
    return HypothesisSetDraft(
      set_: set_ == const $CopyWithPlaceholder() || set_ == null
          ? _value.set_
          // ignore: cast_nullable_to_non_nullable
          : set_ as HypothesisSetInput,
      valid: valid == const $CopyWithPlaceholder() || valid == null
          ? _value.valid
          // ignore: cast_nullable_to_non_nullable
          : valid as bool,
      warnings: warnings == const $CopyWithPlaceholder() || warnings == null
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as List<HypothesisSetDraftWarningsInner>,
      toolCalls: toolCalls == const $CopyWithPlaceholder()
          ? _value.toolCalls
          // ignore: cast_nullable_to_non_nullable
          : toolCalls as List<String>?,
      model: model == const $CopyWithPlaceholder()
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as String?,
      effort: effort == const $CopyWithPlaceholder()
          ? _value.effort
          // ignore: cast_nullable_to_non_nullable
          : effort as HypothesisSetDraftEffortEnum?,
      usage: usage == const $CopyWithPlaceholder()
          ? _value.usage
          // ignore: cast_nullable_to_non_nullable
          : usage as HypothesisSetDraftUsage?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as HypothesisSetDraftTextOriginEnum?,
    );
  }
}

extension $HypothesisSetDraftCopyWith on HypothesisSetDraft {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisSetDraft.copyWith(...)` or `instanceOfHypothesisSetDraft.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisSetDraftCWProxy get copyWith =>
      _$HypothesisSetDraftCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisSetDraft _$HypothesisSetDraftFromJson(Map<String, dynamic> json) =>
    $checkedCreate('HypothesisSetDraft', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['set', 'valid', 'warnings']);
      final val = HypothesisSetDraft(
        set_: $checkedConvert(
          'set',
          (v) => HypothesisSetInput.fromJson(v as Map<String, dynamic>),
        ),
        valid: $checkedConvert('valid', (v) => v as bool),
        warnings: $checkedConvert(
          'warnings',
          (v) => (v as List<dynamic>)
              .map(
                (e) => HypothesisSetDraftWarningsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        toolCalls: $checkedConvert(
          'toolCalls',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        model: $checkedConvert('model', (v) => v as String?),
        effort: $checkedConvert(
          'effort',
          (v) => $enumDecodeNullable(_$HypothesisSetDraftEffortEnumEnumMap, v),
        ),
        usage: $checkedConvert(
          'usage',
          (v) => v == null
              ? null
              : HypothesisSetDraftUsage.fromJson(v as Map<String, dynamic>),
        ),
        manifestId: $checkedConvert('manifestId', (v) => v as String?),
        textOrigin: $checkedConvert(
          'textOrigin',
          (v) =>
              $enumDecodeNullable(_$HypothesisSetDraftTextOriginEnumEnumMap, v),
        ),
      );
      return val;
    }, fieldKeyMap: const {'set_': 'set'});

Map<String, dynamic> _$HypothesisSetDraftToJson(
  HypothesisSetDraft instance,
) => <String, dynamic>{
  'set': instance.set_.toJson(),
  'valid': instance.valid,
  'warnings': instance.warnings.map((e) => e.toJson()).toList(),
  'toolCalls': ?instance.toolCalls,
  'model': ?instance.model,
  'effort': ?_$HypothesisSetDraftEffortEnumEnumMap[instance.effort],
  'usage': ?instance.usage?.toJson(),
  'manifestId': ?instance.manifestId,
  'textOrigin': ?_$HypothesisSetDraftTextOriginEnumEnumMap[instance.textOrigin],
};

const _$HypothesisSetDraftEffortEnumEnumMap = {
  HypothesisSetDraftEffortEnum.low: 'low',
  HypothesisSetDraftEffortEnum.medium: 'medium',
  HypothesisSetDraftEffortEnum.high: 'high',
};

const _$HypothesisSetDraftTextOriginEnumEnumMap = {
  HypothesisSetDraftTextOriginEnum.llm: 'llm',
};
