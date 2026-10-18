//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ach_matrix_hypotheses_inner.dart';
import 'package:foresignal_api/src/model/ach_evidence.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ach_matrix.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AchMatrix {
  /// Returns a new [AchMatrix] instance.
  AchMatrix({

    required  this.setId,

    required  this.computedAt,

    required  this.hypotheses,

    required  this.rows,

    required  this.scores,

     this.leastInconsistentHypothesisId,

     this.nonDiagnosticEvidenceIds,

     this.note = 'Audit view: probabilities come from the updater and are scored in the ledger',
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'setId',
    required: true,
    includeIfNull: false,
  )


  final String setId;



  @JsonKey(
    
    name: r'computedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime computedAt;



  @JsonKey(
    
    name: r'hypotheses',
    required: true,
    includeIfNull: false,
  )


  final List<AchMatrixHypothesesInner> hypotheses;



  @JsonKey(
    
    name: r'rows',
    required: true,
    includeIfNull: false,
  )


  final List<AchEvidence> rows;



      /// Inconsistency score per hypothesis id
  @JsonKey(
    
    name: r'scores',
    required: true,
    includeIfNull: false,
  )


  final Map<String, num> scores;



      /// ULID identifier
  @JsonKey(
    
    name: r'leastInconsistentHypothesisId',
    required: false,
    includeIfNull: false,
  )


  final String? leastInconsistentHypothesisId;



  @JsonKey(
    
    name: r'nonDiagnosticEvidenceIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? nonDiagnosticEvidenceIds;



  @JsonKey(
    defaultValue: 'Audit view: probabilities come from the updater and are scored in the ledger',
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AchMatrix &&
      other.setId == setId &&
      other.computedAt == computedAt &&
      other.hypotheses == hypotheses &&
      other.rows == rows &&
      other.scores == scores &&
      other.leastInconsistentHypothesisId == leastInconsistentHypothesisId &&
      other.nonDiagnosticEvidenceIds == nonDiagnosticEvidenceIds &&
      other.note == note;

    @override
    int get hashCode =>
        setId.hashCode +
        computedAt.hashCode +
        hypotheses.hashCode +
        rows.hashCode +
        scores.hashCode +
        (leastInconsistentHypothesisId == null ? 0 : leastInconsistentHypothesisId.hashCode) +
        nonDiagnosticEvidenceIds.hashCode +
        note.hashCode;

  factory AchMatrix.fromJson(Map<String, dynamic> json) => _$AchMatrixFromJson(json);

  Map<String, dynamic> toJson() => _$AchMatrixToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

