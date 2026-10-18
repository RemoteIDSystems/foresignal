// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ach_matrix.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AchMatrixCWProxy {
  AchMatrix setId(String setId);

  AchMatrix computedAt(DateTime computedAt);

  AchMatrix hypotheses(List<AchMatrixHypothesesInner> hypotheses);

  AchMatrix rows(List<AchEvidence> rows);

  AchMatrix scores(Map<String, num> scores);

  AchMatrix leastInconsistentHypothesisId(
    String? leastInconsistentHypothesisId,
  );

  AchMatrix nonDiagnosticEvidenceIds(List<String>? nonDiagnosticEvidenceIds);

  AchMatrix note(String? note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AchMatrix(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AchMatrix(...).copyWith(id: 12, name: "My name")
  /// ```
  AchMatrix call({
    String setId,
    DateTime computedAt,
    List<AchMatrixHypothesesInner> hypotheses,
    List<AchEvidence> rows,
    Map<String, num> scores,
    String? leastInconsistentHypothesisId,
    List<String>? nonDiagnosticEvidenceIds,
    String? note,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAchMatrix.copyWith(...)` or call `instanceOfAchMatrix.copyWith.fieldName(value)` for a single field.
class _$AchMatrixCWProxyImpl implements _$AchMatrixCWProxy {
  const _$AchMatrixCWProxyImpl(this._value);

  final AchMatrix _value;

  @override
  AchMatrix setId(String setId) => call(setId: setId);

  @override
  AchMatrix computedAt(DateTime computedAt) => call(computedAt: computedAt);

  @override
  AchMatrix hypotheses(List<AchMatrixHypothesesInner> hypotheses) =>
      call(hypotheses: hypotheses);

  @override
  AchMatrix rows(List<AchEvidence> rows) => call(rows: rows);

  @override
  AchMatrix scores(Map<String, num> scores) => call(scores: scores);

  @override
  AchMatrix leastInconsistentHypothesisId(
    String? leastInconsistentHypothesisId,
  ) => call(leastInconsistentHypothesisId: leastInconsistentHypothesisId);

  @override
  AchMatrix nonDiagnosticEvidenceIds(List<String>? nonDiagnosticEvidenceIds) =>
      call(nonDiagnosticEvidenceIds: nonDiagnosticEvidenceIds);

  @override
  AchMatrix note(String? note) => call(note: note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AchMatrix(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AchMatrix(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AchMatrix call({
    Object? setId = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
    Object? hypotheses = const $CopyWithPlaceholder(),
    Object? rows = const $CopyWithPlaceholder(),
    Object? scores = const $CopyWithPlaceholder(),
    Object? leastInconsistentHypothesisId = const $CopyWithPlaceholder(),
    Object? nonDiagnosticEvidenceIds = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
  }) {
    return AchMatrix(
      setId: setId == const $CopyWithPlaceholder() || setId == null
          ? _value.setId
          // ignore: cast_nullable_to_non_nullable
          : setId as String,
      computedAt:
          computedAt == const $CopyWithPlaceholder() || computedAt == null
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime,
      hypotheses:
          hypotheses == const $CopyWithPlaceholder() || hypotheses == null
          ? _value.hypotheses
          // ignore: cast_nullable_to_non_nullable
          : hypotheses as List<AchMatrixHypothesesInner>,
      rows: rows == const $CopyWithPlaceholder() || rows == null
          ? _value.rows
          // ignore: cast_nullable_to_non_nullable
          : rows as List<AchEvidence>,
      scores: scores == const $CopyWithPlaceholder() || scores == null
          ? _value.scores
          // ignore: cast_nullable_to_non_nullable
          : scores as Map<String, num>,
      leastInconsistentHypothesisId:
          leastInconsistentHypothesisId == const $CopyWithPlaceholder()
          ? _value.leastInconsistentHypothesisId
          // ignore: cast_nullable_to_non_nullable
          : leastInconsistentHypothesisId as String?,
      nonDiagnosticEvidenceIds:
          nonDiagnosticEvidenceIds == const $CopyWithPlaceholder()
          ? _value.nonDiagnosticEvidenceIds
          // ignore: cast_nullable_to_non_nullable
          : nonDiagnosticEvidenceIds as List<String>?,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
    );
  }
}

extension $AchMatrixCopyWith on AchMatrix {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAchMatrix.copyWith(...)` or `instanceOfAchMatrix.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AchMatrixCWProxy get copyWith => _$AchMatrixCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AchMatrix _$AchMatrixFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AchMatrix', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['setId', 'computedAt', 'hypotheses', 'rows', 'scores'],
  );
  final val = AchMatrix(
    setId: $checkedConvert('setId', (v) => v as String),
    computedAt: $checkedConvert(
      'computedAt',
      (v) => DateTime.parse(v as String),
    ),
    hypotheses: $checkedConvert(
      'hypotheses',
      (v) => (v as List<dynamic>)
          .map(
            (e) => AchMatrixHypothesesInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    rows: $checkedConvert(
      'rows',
      (v) => (v as List<dynamic>)
          .map((e) => AchEvidence.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    scores: $checkedConvert('scores', (v) => Map<String, num>.from(v as Map)),
    leastInconsistentHypothesisId: $checkedConvert(
      'leastInconsistentHypothesisId',
      (v) => v as String?,
    ),
    nonDiagnosticEvidenceIds: $checkedConvert(
      'nonDiagnosticEvidenceIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    note: $checkedConvert(
      'note',
      (v) =>
          v as String? ??
          'Audit view: probabilities come from the updater and are scored in the ledger',
    ),
  );
  return val;
});

Map<String, dynamic> _$AchMatrixToJson(AchMatrix instance) => <String, dynamic>{
  'setId': instance.setId,
  'computedAt': instance.computedAt.toIso8601String(),
  'hypotheses': instance.hypotheses.map((e) => e.toJson()).toList(),
  'rows': instance.rows.map((e) => e.toJson()).toList(),
  'scores': instance.scores,
  'leastInconsistentHypothesisId': ?instance.leastInconsistentHypothesisId,
  'nonDiagnosticEvidenceIds': ?instance.nonDiagnosticEvidenceIds,
  'note': ?instance.note,
};
