// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_link_decision.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationLinkDecisionCWProxy {
  SituationLinkDecision seq(int seq);

  SituationLinkDecision inputRef(String inputRef);

  SituationLinkDecision memberType(
    SituationLinkDecisionMemberTypeEnum? memberType,
  );

  SituationLinkDecision signalFamily(String? signalFamily);

  SituationLinkDecision action(SituationLinkDecisionActionEnum action);

  SituationLinkDecision situationId(String? situationId);

  SituationLinkDecision seedTitle(String? seedTitle);

  SituationLinkDecision score(num? score);

  SituationLinkDecision joinReason(SituationJoinReason? joinReason);

  SituationLinkDecision candidates(
    List<SituationLinkDecisionCandidatesInner>? candidates,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationLinkDecision(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationLinkDecision(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationLinkDecision call({
    int seq,
    String inputRef,
    SituationLinkDecisionMemberTypeEnum? memberType,
    String? signalFamily,
    SituationLinkDecisionActionEnum action,
    String? situationId,
    String? seedTitle,
    num? score,
    SituationJoinReason? joinReason,
    List<SituationLinkDecisionCandidatesInner>? candidates,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationLinkDecision.copyWith(...)` or call `instanceOfSituationLinkDecision.copyWith.fieldName(value)` for a single field.
class _$SituationLinkDecisionCWProxyImpl
    implements _$SituationLinkDecisionCWProxy {
  const _$SituationLinkDecisionCWProxyImpl(this._value);

  final SituationLinkDecision _value;

  @override
  SituationLinkDecision seq(int seq) => call(seq: seq);

  @override
  SituationLinkDecision inputRef(String inputRef) => call(inputRef: inputRef);

  @override
  SituationLinkDecision memberType(
    SituationLinkDecisionMemberTypeEnum? memberType,
  ) => call(memberType: memberType);

  @override
  SituationLinkDecision signalFamily(String? signalFamily) =>
      call(signalFamily: signalFamily);

  @override
  SituationLinkDecision action(SituationLinkDecisionActionEnum action) =>
      call(action: action);

  @override
  SituationLinkDecision situationId(String? situationId) =>
      call(situationId: situationId);

  @override
  SituationLinkDecision seedTitle(String? seedTitle) =>
      call(seedTitle: seedTitle);

  @override
  SituationLinkDecision score(num? score) => call(score: score);

  @override
  SituationLinkDecision joinReason(SituationJoinReason? joinReason) =>
      call(joinReason: joinReason);

  @override
  SituationLinkDecision candidates(
    List<SituationLinkDecisionCandidatesInner>? candidates,
  ) => call(candidates: candidates);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationLinkDecision(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationLinkDecision(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationLinkDecision call({
    Object? seq = const $CopyWithPlaceholder(),
    Object? inputRef = const $CopyWithPlaceholder(),
    Object? memberType = const $CopyWithPlaceholder(),
    Object? signalFamily = const $CopyWithPlaceholder(),
    Object? action = const $CopyWithPlaceholder(),
    Object? situationId = const $CopyWithPlaceholder(),
    Object? seedTitle = const $CopyWithPlaceholder(),
    Object? score = const $CopyWithPlaceholder(),
    Object? joinReason = const $CopyWithPlaceholder(),
    Object? candidates = const $CopyWithPlaceholder(),
  }) {
    return SituationLinkDecision(
      seq: seq == const $CopyWithPlaceholder() || seq == null
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int,
      inputRef: inputRef == const $CopyWithPlaceholder() || inputRef == null
          ? _value.inputRef
          // ignore: cast_nullable_to_non_nullable
          : inputRef as String,
      memberType: memberType == const $CopyWithPlaceholder()
          ? _value.memberType
          // ignore: cast_nullable_to_non_nullable
          : memberType as SituationLinkDecisionMemberTypeEnum?,
      signalFamily: signalFamily == const $CopyWithPlaceholder()
          ? _value.signalFamily
          // ignore: cast_nullable_to_non_nullable
          : signalFamily as String?,
      action: action == const $CopyWithPlaceholder() || action == null
          ? _value.action
          // ignore: cast_nullable_to_non_nullable
          : action as SituationLinkDecisionActionEnum,
      situationId: situationId == const $CopyWithPlaceholder()
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String?,
      seedTitle: seedTitle == const $CopyWithPlaceholder()
          ? _value.seedTitle
          // ignore: cast_nullable_to_non_nullable
          : seedTitle as String?,
      score: score == const $CopyWithPlaceholder()
          ? _value.score
          // ignore: cast_nullable_to_non_nullable
          : score as num?,
      joinReason: joinReason == const $CopyWithPlaceholder()
          ? _value.joinReason
          // ignore: cast_nullable_to_non_nullable
          : joinReason as SituationJoinReason?,
      candidates: candidates == const $CopyWithPlaceholder()
          ? _value.candidates
          // ignore: cast_nullable_to_non_nullable
          : candidates as List<SituationLinkDecisionCandidatesInner>?,
    );
  }
}

extension $SituationLinkDecisionCopyWith on SituationLinkDecision {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationLinkDecision.copyWith(...)` or `instanceOfSituationLinkDecision.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationLinkDecisionCWProxy get copyWith =>
      _$SituationLinkDecisionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationLinkDecision _$SituationLinkDecisionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationLinkDecision', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['seq', 'inputRef', 'action']);
  final val = SituationLinkDecision(
    seq: $checkedConvert('seq', (v) => (v as num).toInt()),
    inputRef: $checkedConvert('inputRef', (v) => v as String),
    memberType: $checkedConvert(
      'memberType',
      (v) =>
          $enumDecodeNullable(_$SituationLinkDecisionMemberTypeEnumEnumMap, v),
    ),
    signalFamily: $checkedConvert('signalFamily', (v) => v as String?),
    action: $checkedConvert(
      'action',
      (v) => $enumDecode(_$SituationLinkDecisionActionEnumEnumMap, v),
    ),
    situationId: $checkedConvert('situationId', (v) => v as String?),
    seedTitle: $checkedConvert('seedTitle', (v) => v as String?),
    score: $checkedConvert('score', (v) => v as num?),
    joinReason: $checkedConvert(
      'joinReason',
      (v) => v == null
          ? null
          : SituationJoinReason.fromJson(v as Map<String, dynamic>),
    ),
    candidates: $checkedConvert(
      'candidates',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => SituationLinkDecisionCandidatesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$SituationLinkDecisionToJson(
  SituationLinkDecision instance,
) => <String, dynamic>{
  'seq': instance.seq,
  'inputRef': instance.inputRef,
  'memberType':
      ?_$SituationLinkDecisionMemberTypeEnumEnumMap[instance.memberType],
  'signalFamily': ?instance.signalFamily,
  'action': _$SituationLinkDecisionActionEnumEnumMap[instance.action]!,
  'situationId': ?instance.situationId,
  'seedTitle': ?instance.seedTitle,
  'score': ?instance.score,
  'joinReason': ?instance.joinReason?.toJson(),
  'candidates': ?instance.candidates?.map((e) => e.toJson()).toList(),
};

const _$SituationLinkDecisionMemberTypeEnumEnumMap = {
  SituationLinkDecisionMemberTypeEnum.intelEvent: 'intel_event',
  SituationLinkDecisionMemberTypeEnum.finding: 'finding',
  SituationLinkDecisionMemberTypeEnum.alert: 'alert',
  SituationLinkDecisionMemberTypeEnum.anomaly: 'anomaly',
  SituationLinkDecisionMemberTypeEnum.record: 'record',
  SituationLinkDecisionMemberTypeEnum.investigation: 'investigation',
};

const _$SituationLinkDecisionActionEnumEnumMap = {
  SituationLinkDecisionActionEnum.join: 'join',
  SituationLinkDecisionActionEnum.seed: 'seed',
  SituationLinkDecisionActionEnum.skip: 'skip',
  SituationLinkDecisionActionEnum.alreadyMember: 'already-member',
};
