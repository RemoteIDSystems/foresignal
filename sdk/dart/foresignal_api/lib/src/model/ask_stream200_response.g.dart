// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ask_stream200_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AskStream200ResponseCWProxy {
  AskStream200Response tool(String tool);

  AskStream200Response status(int status);

  AskStream200Response summary(String? summary);

  AskStream200Response text(String text);

  AskStream200Response id(String id);

  AskStream200Response seq(int? seq);

  AskStream200Response title(String title);

  AskStream200Response type(String type);

  AskStream200Response productId(String? productId);

  AskStream200Response frameKey(String? frameKey);

  AskStream200Response run(String? run);

  AskStream200Response step(int? step);

  AskStream200Response validTime(DateTime? validTime);

  AskStream200Response ageMinutes(num? ageMinutes);

  AskStream200Response provenance(AskCitationProvenance? provenance);

  AskStream200Response corroboration(AskCitationCorroboration? corroboration);

  AskStream200Response message(String message);

  AskStream200Response description(String description);

  AskStream200Response payload(Map<String, Object>? payload);

  AskStream200Response result(Map<String, Object>? result);

  AskStream200Response createdAt(DateTime? createdAt);

  AskStream200Response executedAt(DateTime? executedAt);

  AskStream200Response threadId(String threadId);

  AskStream200Response answer(String answer);

  AskStream200Response citations(List<AskCitation> citations);

  AskStream200Response proposedActions(List<AskAction> proposedActions);

  AskStream200Response warnings(List<String>? warnings);

  AskStream200Response mode(AskStream200ResponseModeEnum? mode);

  AskStream200Response usage(AskResponseUsage? usage);

  AskStream200Response stopReason(
    AskStream200ResponseStopReasonEnum? stopReason,
  );

  AskStream200Response refusalCategory(String? refusalCategory);

  AskStream200Response detail(String? detail);

  AskStream200Response instance(String? instance);

  AskStream200Response code(String? code);

  AskStream200Response errors(List<ProblemErrorsInner>? errors);

  AskStream200Response purgeHorizon(int? purgeHorizon);

  AskStream200Response watermarkSeq(int? watermarkSeq);

  AskStream200Response horizon(DateTime? horizon);

  AskStream200Response capUsd(num? capUsd);

  AskStream200Response spentUsd(num? spentUsd);

  AskStream200Response estimateUsd(num? estimateUsd);

  AskStream200Response requiredScope(
    AskStream200ResponseRequiredScopeEnum? requiredScope,
  );

  AskStream200Response class_(AskStream200ResponseClass_Enum? class_);

  AskStream200Response retryAfterSeconds(int? retryAfterSeconds);

  AskStream200Response references(ProblemReferences? references);

  AskStream200Response reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskStream200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskStream200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  AskStream200Response call({
    String tool,
    int status,
    String? summary,
    String text,
    String id,
    int? seq,
    String title,
    String type,
    String? productId,
    String? frameKey,
    String? run,
    int? step,
    DateTime? validTime,
    num? ageMinutes,
    AskCitationProvenance? provenance,
    AskCitationCorroboration? corroboration,
    String message,
    String description,
    Map<String, Object>? payload,
    Map<String, Object>? result,
    DateTime? createdAt,
    DateTime? executedAt,
    String threadId,
    String answer,
    List<AskCitation> citations,
    List<AskAction> proposedActions,
    List<String>? warnings,
    AskStream200ResponseModeEnum? mode,
    AskResponseUsage? usage,
    AskStream200ResponseStopReasonEnum? stopReason,
    String? refusalCategory,
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
    AskStream200ResponseRequiredScopeEnum? requiredScope,
    AskStream200ResponseClass_Enum? class_,
    int? retryAfterSeconds,
    ProblemReferences? references,
    String? reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAskStream200Response.copyWith(...)` or call `instanceOfAskStream200Response.copyWith.fieldName(value)` for a single field.
class _$AskStream200ResponseCWProxyImpl
    implements _$AskStream200ResponseCWProxy {
  const _$AskStream200ResponseCWProxyImpl(this._value);

  final AskStream200Response _value;

  @override
  AskStream200Response tool(String tool) => call(tool: tool);

  @override
  AskStream200Response status(int status) => call(status: status);

  @override
  AskStream200Response summary(String? summary) => call(summary: summary);

  @override
  AskStream200Response text(String text) => call(text: text);

  @override
  AskStream200Response id(String id) => call(id: id);

  @override
  AskStream200Response seq(int? seq) => call(seq: seq);

  @override
  AskStream200Response title(String title) => call(title: title);

  @override
  AskStream200Response type(String type) => call(type: type);

  @override
  AskStream200Response productId(String? productId) =>
      call(productId: productId);

  @override
  AskStream200Response frameKey(String? frameKey) => call(frameKey: frameKey);

  @override
  AskStream200Response run(String? run) => call(run: run);

  @override
  AskStream200Response step(int? step) => call(step: step);

  @override
  AskStream200Response validTime(DateTime? validTime) =>
      call(validTime: validTime);

  @override
  AskStream200Response ageMinutes(num? ageMinutes) =>
      call(ageMinutes: ageMinutes);

  @override
  AskStream200Response provenance(AskCitationProvenance? provenance) =>
      call(provenance: provenance);

  @override
  AskStream200Response corroboration(AskCitationCorroboration? corroboration) =>
      call(corroboration: corroboration);

  @override
  AskStream200Response message(String message) => call(message: message);

  @override
  AskStream200Response description(String description) =>
      call(description: description);

  @override
  AskStream200Response payload(Map<String, Object>? payload) =>
      call(payload: payload);

  @override
  AskStream200Response result(Map<String, Object>? result) =>
      call(result: result);

  @override
  AskStream200Response createdAt(DateTime? createdAt) =>
      call(createdAt: createdAt);

  @override
  AskStream200Response executedAt(DateTime? executedAt) =>
      call(executedAt: executedAt);

  @override
  AskStream200Response threadId(String threadId) => call(threadId: threadId);

  @override
  AskStream200Response answer(String answer) => call(answer: answer);

  @override
  AskStream200Response citations(List<AskCitation> citations) =>
      call(citations: citations);

  @override
  AskStream200Response proposedActions(List<AskAction> proposedActions) =>
      call(proposedActions: proposedActions);

  @override
  AskStream200Response warnings(List<String>? warnings) =>
      call(warnings: warnings);

  @override
  AskStream200Response mode(AskStream200ResponseModeEnum? mode) =>
      call(mode: mode);

  @override
  AskStream200Response usage(AskResponseUsage? usage) => call(usage: usage);

  @override
  AskStream200Response stopReason(
    AskStream200ResponseStopReasonEnum? stopReason,
  ) => call(stopReason: stopReason);

  @override
  AskStream200Response refusalCategory(String? refusalCategory) =>
      call(refusalCategory: refusalCategory);

  @override
  AskStream200Response detail(String? detail) => call(detail: detail);

  @override
  AskStream200Response instance(String? instance) => call(instance: instance);

  @override
  AskStream200Response code(String? code) => call(code: code);

  @override
  AskStream200Response errors(List<ProblemErrorsInner>? errors) =>
      call(errors: errors);

  @override
  AskStream200Response purgeHorizon(int? purgeHorizon) =>
      call(purgeHorizon: purgeHorizon);

  @override
  AskStream200Response watermarkSeq(int? watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  @override
  AskStream200Response horizon(DateTime? horizon) => call(horizon: horizon);

  @override
  AskStream200Response capUsd(num? capUsd) => call(capUsd: capUsd);

  @override
  AskStream200Response spentUsd(num? spentUsd) => call(spentUsd: spentUsd);

  @override
  AskStream200Response estimateUsd(num? estimateUsd) =>
      call(estimateUsd: estimateUsd);

  @override
  AskStream200Response requiredScope(
    AskStream200ResponseRequiredScopeEnum? requiredScope,
  ) => call(requiredScope: requiredScope);

  @override
  AskStream200Response class_(AskStream200ResponseClass_Enum? class_) =>
      call(class_: class_);

  @override
  AskStream200Response retryAfterSeconds(int? retryAfterSeconds) =>
      call(retryAfterSeconds: retryAfterSeconds);

  @override
  AskStream200Response references(ProblemReferences? references) =>
      call(references: references);

  @override
  AskStream200Response reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskStream200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskStream200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AskStream200Response call({
    Object? tool = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? productId = const $CopyWithPlaceholder(),
    Object? frameKey = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? step = const $CopyWithPlaceholder(),
    Object? validTime = const $CopyWithPlaceholder(),
    Object? ageMinutes = const $CopyWithPlaceholder(),
    Object? provenance = const $CopyWithPlaceholder(),
    Object? corroboration = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? payload = const $CopyWithPlaceholder(),
    Object? result = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? executedAt = const $CopyWithPlaceholder(),
    Object? threadId = const $CopyWithPlaceholder(),
    Object? answer = const $CopyWithPlaceholder(),
    Object? citations = const $CopyWithPlaceholder(),
    Object? proposedActions = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? usage = const $CopyWithPlaceholder(),
    Object? stopReason = const $CopyWithPlaceholder(),
    Object? refusalCategory = const $CopyWithPlaceholder(),
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
    return AskStream200Response(
      tool: tool == const $CopyWithPlaceholder() || tool == null
          ? _value.tool
          // ignore: cast_nullable_to_non_nullable
          : tool as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as int,
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String?,
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      seq: seq == const $CopyWithPlaceholder()
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int?,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as String,
      productId: productId == const $CopyWithPlaceholder()
          ? _value.productId
          // ignore: cast_nullable_to_non_nullable
          : productId as String?,
      frameKey: frameKey == const $CopyWithPlaceholder()
          ? _value.frameKey
          // ignore: cast_nullable_to_non_nullable
          : frameKey as String?,
      run: run == const $CopyWithPlaceholder()
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as String?,
      step: step == const $CopyWithPlaceholder()
          ? _value.step
          // ignore: cast_nullable_to_non_nullable
          : step as int?,
      validTime: validTime == const $CopyWithPlaceholder()
          ? _value.validTime
          // ignore: cast_nullable_to_non_nullable
          : validTime as DateTime?,
      ageMinutes: ageMinutes == const $CopyWithPlaceholder()
          ? _value.ageMinutes
          // ignore: cast_nullable_to_non_nullable
          : ageMinutes as num?,
      provenance: provenance == const $CopyWithPlaceholder()
          ? _value.provenance
          // ignore: cast_nullable_to_non_nullable
          : provenance as AskCitationProvenance?,
      corroboration: corroboration == const $CopyWithPlaceholder()
          ? _value.corroboration
          // ignore: cast_nullable_to_non_nullable
          : corroboration as AskCitationCorroboration?,
      message: message == const $CopyWithPlaceholder() || message == null
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String,
      description:
          description == const $CopyWithPlaceholder() || description == null
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String,
      payload: payload == const $CopyWithPlaceholder()
          ? _value.payload
          // ignore: cast_nullable_to_non_nullable
          : payload as Map<String, Object>?,
      result: result == const $CopyWithPlaceholder()
          ? _value.result
          // ignore: cast_nullable_to_non_nullable
          : result as Map<String, Object>?,
      createdAt: createdAt == const $CopyWithPlaceholder()
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime?,
      executedAt: executedAt == const $CopyWithPlaceholder()
          ? _value.executedAt
          // ignore: cast_nullable_to_non_nullable
          : executedAt as DateTime?,
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
          : mode as AskStream200ResponseModeEnum?,
      usage: usage == const $CopyWithPlaceholder()
          ? _value.usage
          // ignore: cast_nullable_to_non_nullable
          : usage as AskResponseUsage?,
      stopReason: stopReason == const $CopyWithPlaceholder()
          ? _value.stopReason
          // ignore: cast_nullable_to_non_nullable
          : stopReason as AskStream200ResponseStopReasonEnum?,
      refusalCategory: refusalCategory == const $CopyWithPlaceholder()
          ? _value.refusalCategory
          // ignore: cast_nullable_to_non_nullable
          : refusalCategory as String?,
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
          : requiredScope as AskStream200ResponseRequiredScopeEnum?,
      class_: class_ == const $CopyWithPlaceholder()
          ? _value.class_
          // ignore: cast_nullable_to_non_nullable
          : class_ as AskStream200ResponseClass_Enum?,
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

extension $AskStream200ResponseCopyWith on AskStream200Response {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAskStream200Response.copyWith(...)` or `instanceOfAskStream200Response.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AskStream200ResponseCWProxy get copyWith =>
      _$AskStream200ResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AskStream200Response _$AskStream200ResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AskStream200Response', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'tool',
      'status',
      'text',
      'id',
      'title',
      'type',
      'message',
      'description',
      'threadId',
      'answer',
      'citations',
      'proposedActions',
    ],
  );
  final val = AskStream200Response(
    tool: $checkedConvert('tool', (v) => v as String),
    status: $checkedConvert('status', (v) => (v as num).toInt()),
    summary: $checkedConvert('summary', (v) => v as String?),
    text: $checkedConvert('text', (v) => v as String),
    id: $checkedConvert('id', (v) => v as String),
    seq: $checkedConvert('seq', (v) => (v as num?)?.toInt()),
    title: $checkedConvert('title', (v) => v as String),
    type: $checkedConvert('type', (v) => v as String),
    productId: $checkedConvert('productId', (v) => v as String?),
    frameKey: $checkedConvert('frameKey', (v) => v as String?),
    run: $checkedConvert('run', (v) => v as String?),
    step: $checkedConvert('step', (v) => (v as num?)?.toInt()),
    validTime: $checkedConvert(
      'validTime',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    ageMinutes: $checkedConvert('ageMinutes', (v) => v as num?),
    provenance: $checkedConvert(
      'provenance',
      (v) => v == null
          ? null
          : AskCitationProvenance.fromJson(v as Map<String, dynamic>),
    ),
    corroboration: $checkedConvert(
      'corroboration',
      (v) => v == null
          ? null
          : AskCitationCorroboration.fromJson(v as Map<String, dynamic>),
    ),
    message: $checkedConvert('message', (v) => v as String),
    description: $checkedConvert('description', (v) => v as String),
    payload: $checkedConvert(
      'payload',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    result: $checkedConvert(
      'result',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    createdAt: $checkedConvert(
      'createdAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    executedAt: $checkedConvert(
      'executedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
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
      (v) => $enumDecodeNullable(_$AskStream200ResponseModeEnumEnumMap, v),
    ),
    usage: $checkedConvert(
      'usage',
      (v) => v == null
          ? null
          : AskResponseUsage.fromJson(v as Map<String, dynamic>),
    ),
    stopReason: $checkedConvert(
      'stopReason',
      (v) =>
          $enumDecodeNullable(_$AskStream200ResponseStopReasonEnumEnumMap, v),
    ),
    refusalCategory: $checkedConvert('refusalCategory', (v) => v as String?),
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
      (v) => $enumDecodeNullable(
        _$AskStream200ResponseRequiredScopeEnumEnumMap,
        v,
      ),
    ),
    class_: $checkedConvert(
      'class',
      (v) => $enumDecodeNullable(_$AskStream200ResponseClass_EnumEnumMap, v),
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

Map<String, dynamic> _$AskStream200ResponseToJson(
  AskStream200Response instance,
) => <String, dynamic>{
  'tool': instance.tool,
  'status': instance.status,
  'summary': ?instance.summary,
  'text': instance.text,
  'id': instance.id,
  'seq': ?instance.seq,
  'title': instance.title,
  'type': instance.type,
  'productId': ?instance.productId,
  'frameKey': ?instance.frameKey,
  'run': ?instance.run,
  'step': ?instance.step,
  'validTime': ?instance.validTime?.toIso8601String(),
  'ageMinutes': ?instance.ageMinutes,
  'provenance': ?instance.provenance?.toJson(),
  'corroboration': ?instance.corroboration?.toJson(),
  'message': instance.message,
  'description': instance.description,
  'payload': ?instance.payload,
  'result': ?instance.result,
  'createdAt': ?instance.createdAt?.toIso8601String(),
  'executedAt': ?instance.executedAt?.toIso8601String(),
  'threadId': instance.threadId,
  'answer': instance.answer,
  'citations': instance.citations.map((e) => e.toJson()).toList(),
  'proposedActions': instance.proposedActions.map((e) => e.toJson()).toList(),
  'warnings': ?instance.warnings,
  'mode': ?_$AskStream200ResponseModeEnumEnumMap[instance.mode],
  'usage': ?instance.usage?.toJson(),
  'stopReason':
      ?_$AskStream200ResponseStopReasonEnumEnumMap[instance.stopReason],
  'refusalCategory': ?instance.refusalCategory,
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
  'requiredScope':
      ?_$AskStream200ResponseRequiredScopeEnumEnumMap[instance.requiredScope],
  'class': ?_$AskStream200ResponseClass_EnumEnumMap[instance.class_],
  'retryAfterSeconds': ?instance.retryAfterSeconds,
  'references': ?instance.references?.toJson(),
  'reason': ?instance.reason,
};

const _$AskStream200ResponseModeEnumEnumMap = {
  AskStream200ResponseModeEnum.tools: 'tools',
  AskStream200ResponseModeEnum.retrieval: 'retrieval',
};

const _$AskStream200ResponseStopReasonEnumEnumMap = {
  AskStream200ResponseStopReasonEnum.end: 'end',
  AskStream200ResponseStopReasonEnum.maxTokens: 'max_tokens',
  AskStream200ResponseStopReasonEnum.refusal: 'refusal',
  AskStream200ResponseStopReasonEnum.error: 'error',
};

const _$AskStream200ResponseRequiredScopeEnumEnumMap = {
  AskStream200ResponseRequiredScopeEnum.read: 'read',
  AskStream200ResponseRequiredScopeEnum.agent: 'agent',
  AskStream200ResponseRequiredScopeEnum.admin: 'admin',
};

const _$AskStream200ResponseClass_EnumEnumMap = {
  AskStream200ResponseClass_Enum.read: 'read',
  AskStream200ResponseClass_Enum.tiles: 'tiles',
  AskStream200ResponseClass_Enum.stream: 'stream',
  AskStream200ResponseClass_Enum.expensive: 'expensive',
  AskStream200ResponseClass_Enum.write: 'write',
  AskStream200ResponseClass_Enum.anonymous: 'anonymous',
  AskStream200ResponseClass_Enum.metrics: 'metrics',
};
