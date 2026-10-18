// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_link_decision_candidates_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationLinkDecisionCandidatesInnerCWProxy {
  SituationLinkDecisionCandidatesInner situationId(String situationId);

  SituationLinkDecisionCandidatesInner score(num score);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationLinkDecisionCandidatesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationLinkDecisionCandidatesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationLinkDecisionCandidatesInner call({String situationId, num score});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationLinkDecisionCandidatesInner.copyWith(...)` or call `instanceOfSituationLinkDecisionCandidatesInner.copyWith.fieldName(value)` for a single field.
class _$SituationLinkDecisionCandidatesInnerCWProxyImpl
    implements _$SituationLinkDecisionCandidatesInnerCWProxy {
  const _$SituationLinkDecisionCandidatesInnerCWProxyImpl(this._value);

  final SituationLinkDecisionCandidatesInner _value;

  @override
  SituationLinkDecisionCandidatesInner situationId(String situationId) =>
      call(situationId: situationId);

  @override
  SituationLinkDecisionCandidatesInner score(num score) => call(score: score);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationLinkDecisionCandidatesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationLinkDecisionCandidatesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationLinkDecisionCandidatesInner call({
    Object? situationId = const $CopyWithPlaceholder(),
    Object? score = const $CopyWithPlaceholder(),
  }) {
    return SituationLinkDecisionCandidatesInner(
      situationId:
          situationId == const $CopyWithPlaceholder() || situationId == null
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String,
      score: score == const $CopyWithPlaceholder() || score == null
          ? _value.score
          // ignore: cast_nullable_to_non_nullable
          : score as num,
    );
  }
}

extension $SituationLinkDecisionCandidatesInnerCopyWith
    on SituationLinkDecisionCandidatesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationLinkDecisionCandidatesInner.copyWith(...)` or `instanceOfSituationLinkDecisionCandidatesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationLinkDecisionCandidatesInnerCWProxy get copyWith =>
      _$SituationLinkDecisionCandidatesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationLinkDecisionCandidatesInner
_$SituationLinkDecisionCandidatesInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SituationLinkDecisionCandidatesInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['situationId', 'score']);
      final val = SituationLinkDecisionCandidatesInner(
        situationId: $checkedConvert('situationId', (v) => v as String),
        score: $checkedConvert('score', (v) => v as num),
      );
      return val;
    });

Map<String, dynamic> _$SituationLinkDecisionCandidatesInnerToJson(
  SituationLinkDecisionCandidatesInner instance,
) => <String, dynamic>{
  'situationId': instance.situationId,
  'score': instance.score,
};
