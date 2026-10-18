// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_proposal.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystProposalCWProxy {
  AnalystProposal id(String id);

  AnalystProposal runId(String runId);

  AnalystProposal profileId(String profileId);

  AnalystProposal type(AnalystProposalTypeEnum type);

  AnalystProposal payload(Map<String, Object> payload);

  AnalystProposal rationale(String? rationale);

  AnalystProposal citations(List<String>? citations);

  AnalystProposal status(AnalystProposalStatusEnum status);

  AnalystProposal autoExecuted(bool? autoExecuted);

  AnalystProposal executedRef(String? executedRef);

  AnalystProposal decidedBy(String? decidedBy);

  AnalystProposal decidedAt(DateTime? decidedAt);

  AnalystProposal reason(String? reason);

  AnalystProposal createdAt(DateTime createdAt);

  AnalystProposal expiresAt(DateTime expiresAt);

  AnalystProposal chronicleDraft(ChronicleInput? chronicleDraft);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystProposal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystProposal(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystProposal call({
    String id,
    String runId,
    String profileId,
    AnalystProposalTypeEnum type,
    Map<String, Object> payload,
    String? rationale,
    List<String>? citations,
    AnalystProposalStatusEnum status,
    bool? autoExecuted,
    String? executedRef,
    String? decidedBy,
    DateTime? decidedAt,
    String? reason,
    DateTime createdAt,
    DateTime expiresAt,
    ChronicleInput? chronicleDraft,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystProposal.copyWith(...)` or call `instanceOfAnalystProposal.copyWith.fieldName(value)` for a single field.
class _$AnalystProposalCWProxyImpl implements _$AnalystProposalCWProxy {
  const _$AnalystProposalCWProxyImpl(this._value);

  final AnalystProposal _value;

  @override
  AnalystProposal id(String id) => call(id: id);

  @override
  AnalystProposal runId(String runId) => call(runId: runId);

  @override
  AnalystProposal profileId(String profileId) => call(profileId: profileId);

  @override
  AnalystProposal type(AnalystProposalTypeEnum type) => call(type: type);

  @override
  AnalystProposal payload(Map<String, Object> payload) =>
      call(payload: payload);

  @override
  AnalystProposal rationale(String? rationale) => call(rationale: rationale);

  @override
  AnalystProposal citations(List<String>? citations) =>
      call(citations: citations);

  @override
  AnalystProposal status(AnalystProposalStatusEnum status) =>
      call(status: status);

  @override
  AnalystProposal autoExecuted(bool? autoExecuted) =>
      call(autoExecuted: autoExecuted);

  @override
  AnalystProposal executedRef(String? executedRef) =>
      call(executedRef: executedRef);

  @override
  AnalystProposal decidedBy(String? decidedBy) => call(decidedBy: decidedBy);

  @override
  AnalystProposal decidedAt(DateTime? decidedAt) => call(decidedAt: decidedAt);

  @override
  AnalystProposal reason(String? reason) => call(reason: reason);

  @override
  AnalystProposal createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  AnalystProposal expiresAt(DateTime expiresAt) => call(expiresAt: expiresAt);

  @override
  AnalystProposal chronicleDraft(ChronicleInput? chronicleDraft) =>
      call(chronicleDraft: chronicleDraft);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystProposal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystProposal(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystProposal call({
    Object? id = const $CopyWithPlaceholder(),
    Object? runId = const $CopyWithPlaceholder(),
    Object? profileId = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? payload = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
    Object? citations = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? autoExecuted = const $CopyWithPlaceholder(),
    Object? executedRef = const $CopyWithPlaceholder(),
    Object? decidedBy = const $CopyWithPlaceholder(),
    Object? decidedAt = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? chronicleDraft = const $CopyWithPlaceholder(),
  }) {
    return AnalystProposal(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      runId: runId == const $CopyWithPlaceholder() || runId == null
          ? _value.runId
          // ignore: cast_nullable_to_non_nullable
          : runId as String,
      profileId: profileId == const $CopyWithPlaceholder() || profileId == null
          ? _value.profileId
          // ignore: cast_nullable_to_non_nullable
          : profileId as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as AnalystProposalTypeEnum,
      payload: payload == const $CopyWithPlaceholder() || payload == null
          ? _value.payload
          // ignore: cast_nullable_to_non_nullable
          : payload as Map<String, Object>,
      rationale: rationale == const $CopyWithPlaceholder()
          ? _value.rationale
          // ignore: cast_nullable_to_non_nullable
          : rationale as String?,
      citations: citations == const $CopyWithPlaceholder()
          ? _value.citations
          // ignore: cast_nullable_to_non_nullable
          : citations as List<String>?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as AnalystProposalStatusEnum,
      autoExecuted: autoExecuted == const $CopyWithPlaceholder()
          ? _value.autoExecuted
          // ignore: cast_nullable_to_non_nullable
          : autoExecuted as bool?,
      executedRef: executedRef == const $CopyWithPlaceholder()
          ? _value.executedRef
          // ignore: cast_nullable_to_non_nullable
          : executedRef as String?,
      decidedBy: decidedBy == const $CopyWithPlaceholder()
          ? _value.decidedBy
          // ignore: cast_nullable_to_non_nullable
          : decidedBy as String?,
      decidedAt: decidedAt == const $CopyWithPlaceholder()
          ? _value.decidedAt
          // ignore: cast_nullable_to_non_nullable
          : decidedAt as DateTime?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      expiresAt: expiresAt == const $CopyWithPlaceholder() || expiresAt == null
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime,
      chronicleDraft: chronicleDraft == const $CopyWithPlaceholder()
          ? _value.chronicleDraft
          // ignore: cast_nullable_to_non_nullable
          : chronicleDraft as ChronicleInput?,
    );
  }
}

extension $AnalystProposalCopyWith on AnalystProposal {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystProposal.copyWith(...)` or `instanceOfAnalystProposal.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystProposalCWProxy get copyWith => _$AnalystProposalCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystProposal _$AnalystProposalFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalystProposal', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'runId',
      'profileId',
      'type',
      'payload',
      'status',
      'createdAt',
      'expiresAt',
    ],
  );
  final val = AnalystProposal(
    id: $checkedConvert('id', (v) => v as String),
    runId: $checkedConvert('runId', (v) => v as String),
    profileId: $checkedConvert('profileId', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$AnalystProposalTypeEnumEnumMap, v),
    ),
    payload: $checkedConvert(
      'payload',
      (v) =>
          (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
    ),
    rationale: $checkedConvert('rationale', (v) => v as String?),
    citations: $checkedConvert(
      'citations',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$AnalystProposalStatusEnumEnumMap, v),
    ),
    autoExecuted: $checkedConvert('autoExecuted', (v) => v as bool?),
    executedRef: $checkedConvert('executedRef', (v) => v as String?),
    decidedBy: $checkedConvert('decidedBy', (v) => v as String?),
    decidedAt: $checkedConvert(
      'decidedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    reason: $checkedConvert('reason', (v) => v as String?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    expiresAt: $checkedConvert('expiresAt', (v) => DateTime.parse(v as String)),
    chronicleDraft: $checkedConvert(
      'chronicleDraft',
      (v) =>
          v == null ? null : ChronicleInput.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$AnalystProposalToJson(AnalystProposal instance) =>
    <String, dynamic>{
      'id': instance.id,
      'runId': instance.runId,
      'profileId': instance.profileId,
      'type': _$AnalystProposalTypeEnumEnumMap[instance.type]!,
      'payload': instance.payload,
      'rationale': ?instance.rationale,
      'citations': ?instance.citations,
      'status': _$AnalystProposalStatusEnumEnumMap[instance.status]!,
      'autoExecuted': ?instance.autoExecuted,
      'executedRef': ?instance.executedRef,
      'decidedBy': ?instance.decidedBy,
      'decidedAt': ?instance.decidedAt?.toIso8601String(),
      'reason': ?instance.reason,
      'createdAt': instance.createdAt.toIso8601String(),
      'expiresAt': instance.expiresAt.toIso8601String(),
      'chronicleDraft': ?instance.chronicleDraft?.toJson(),
    };

const _$AnalystProposalTypeEnumEnumMap = {
  AnalystProposalTypeEnum.proposeWatch: 'propose_watch',
  AnalystProposalTypeEnum.proposeIntelEvent: 'propose_intel_event',
  AnalystProposalTypeEnum.proposeInvestigationItem:
      'propose_investigation_item',
  AnalystProposalTypeEnum.annotateEvent: 'annotate_event',
  AnalystProposalTypeEnum.proposeChronicle: 'propose_chronicle',
  AnalystProposalTypeEnum.proposeHypothesisSet: 'propose_hypothesis_set',
  AnalystProposalTypeEnum.proposeIndicator: 'propose_indicator',
  AnalystProposalTypeEnum.proposeQuestion: 'propose_question',
  AnalystProposalTypeEnum.proposeSituationMerge: 'propose_situation_merge',
};

const _$AnalystProposalStatusEnumEnumMap = {
  AnalystProposalStatusEnum.pending: 'pending',
  AnalystProposalStatusEnum.executed: 'executed',
  AnalystProposalStatusEnum.rejected: 'rejected',
  AnalystProposalStatusEnum.expired: 'expired',
  AnalystProposalStatusEnum.failed: 'failed',
};
