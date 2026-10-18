// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ask_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AskResponseCWProxy {
  AskResponse threadId(String threadId);

  AskResponse answer(String answer);

  AskResponse citations(List<AskCitation> citations);

  AskResponse proposedActions(List<AskAction> proposedActions);

  AskResponse warnings(List<String>? warnings);

  AskResponse mode(AskResponseModeEnum? mode);

  AskResponse usage(AskResponseUsage? usage);

  AskResponse stopReason(AskResponseStopReasonEnum? stopReason);

  AskResponse refusalCategory(String? refusalCategory);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskResponse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskResponse(...).copyWith(id: 12, name: "My name")
  /// ```
  AskResponse call({
    String threadId,
    String answer,
    List<AskCitation> citations,
    List<AskAction> proposedActions,
    List<String>? warnings,
    AskResponseModeEnum? mode,
    AskResponseUsage? usage,
    AskResponseStopReasonEnum? stopReason,
    String? refusalCategory,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAskResponse.copyWith(...)` or call `instanceOfAskResponse.copyWith.fieldName(value)` for a single field.
class _$AskResponseCWProxyImpl implements _$AskResponseCWProxy {
  const _$AskResponseCWProxyImpl(this._value);

  final AskResponse _value;

  @override
  AskResponse threadId(String threadId) => call(threadId: threadId);

  @override
  AskResponse answer(String answer) => call(answer: answer);

  @override
  AskResponse citations(List<AskCitation> citations) =>
      call(citations: citations);

  @override
  AskResponse proposedActions(List<AskAction> proposedActions) =>
      call(proposedActions: proposedActions);

  @override
  AskResponse warnings(List<String>? warnings) => call(warnings: warnings);

  @override
  AskResponse mode(AskResponseModeEnum? mode) => call(mode: mode);

  @override
  AskResponse usage(AskResponseUsage? usage) => call(usage: usage);

  @override
  AskResponse stopReason(AskResponseStopReasonEnum? stopReason) =>
      call(stopReason: stopReason);

  @override
  AskResponse refusalCategory(String? refusalCategory) =>
      call(refusalCategory: refusalCategory);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskResponse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskResponse(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AskResponse call({
    Object? threadId = const $CopyWithPlaceholder(),
    Object? answer = const $CopyWithPlaceholder(),
    Object? citations = const $CopyWithPlaceholder(),
    Object? proposedActions = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? usage = const $CopyWithPlaceholder(),
    Object? stopReason = const $CopyWithPlaceholder(),
    Object? refusalCategory = const $CopyWithPlaceholder(),
  }) {
    return AskResponse(
      threadId: threadId == const $CopyWithPlaceholder() || threadId == null
          ? _value.threadId
          // ignore: cast_nullable_to_non_nullable
          : threadId as String,
      answer: answer == const $CopyWithPlaceholder() || answer == null
          ? _value.answer
          // ignore: cast_nullable_to_non_nullable
          : answer as String,
      citations: citations == const $CopyWithPlaceholder() || citations == null
          ? _value.citations
          // ignore: cast_nullable_to_non_nullable
          : citations as List<AskCitation>,
      proposedActions:
          proposedActions == const $CopyWithPlaceholder() ||
              proposedActions == null
          ? _value.proposedActions
          // ignore: cast_nullable_to_non_nullable
          : proposedActions as List<AskAction>,
      warnings: warnings == const $CopyWithPlaceholder()
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as List<String>?,
      mode: mode == const $CopyWithPlaceholder()
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as AskResponseModeEnum?,
      usage: usage == const $CopyWithPlaceholder()
          ? _value.usage
          // ignore: cast_nullable_to_non_nullable
          : usage as AskResponseUsage?,
      stopReason: stopReason == const $CopyWithPlaceholder()
          ? _value.stopReason
          // ignore: cast_nullable_to_non_nullable
          : stopReason as AskResponseStopReasonEnum?,
      refusalCategory: refusalCategory == const $CopyWithPlaceholder()
          ? _value.refusalCategory
          // ignore: cast_nullable_to_non_nullable
          : refusalCategory as String?,
    );
  }
}

extension $AskResponseCopyWith on AskResponse {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAskResponse.copyWith(...)` or `instanceOfAskResponse.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AskResponseCWProxy get copyWith => _$AskResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AskResponse _$AskResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AskResponse', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['threadId', 'answer', 'citations', 'proposedActions'],
  );
  final val = AskResponse(
    threadId: $checkedConvert('threadId', (v) => v as String),
    answer: $checkedConvert('answer', (v) => v as String),
    citations: $checkedConvert(
      'citations',
      (v) => (v as List<dynamic>)
          .map((e) => AskCitation.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    proposedActions: $checkedConvert(
      'proposedActions',
      (v) => (v as List<dynamic>)
          .map((e) => AskAction.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    warnings: $checkedConvert(
      'warnings',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    mode: $checkedConvert(
      'mode',
      (v) => $enumDecodeNullable(_$AskResponseModeEnumEnumMap, v),
    ),
    usage: $checkedConvert(
      'usage',
      (v) => v == null
          ? null
          : AskResponseUsage.fromJson(v as Map<String, dynamic>),
    ),
    stopReason: $checkedConvert(
      'stopReason',
      (v) => $enumDecodeNullable(_$AskResponseStopReasonEnumEnumMap, v),
    ),
    refusalCategory: $checkedConvert('refusalCategory', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$AskResponseToJson(
  AskResponse instance,
) => <String, dynamic>{
  'threadId': instance.threadId,
  'answer': instance.answer,
  'citations': instance.citations.map((e) => e.toJson()).toList(),
  'proposedActions': instance.proposedActions.map((e) => e.toJson()).toList(),
  'warnings': ?instance.warnings,
  'mode': ?_$AskResponseModeEnumEnumMap[instance.mode],
  'usage': ?instance.usage?.toJson(),
  'stopReason': ?_$AskResponseStopReasonEnumEnumMap[instance.stopReason],
  'refusalCategory': ?instance.refusalCategory,
};

const _$AskResponseModeEnumEnumMap = {
  AskResponseModeEnum.tools: 'tools',
  AskResponseModeEnum.retrieval: 'retrieval',
};

const _$AskResponseStopReasonEnumEnumMap = {
  AskResponseStopReasonEnum.end: 'end',
  AskResponseStopReasonEnum.maxTokens: 'max_tokens',
  AskResponseStopReasonEnum.refusal: 'refusal',
  AskResponseStopReasonEnum.error: 'error',
};
