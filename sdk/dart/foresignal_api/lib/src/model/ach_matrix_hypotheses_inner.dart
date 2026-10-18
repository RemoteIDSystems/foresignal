//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/icd_term.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ach_matrix_hypotheses_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AchMatrixHypothesesInner {
  /// Returns a new [AchMatrixHypothesesInner] instance.
  AchMatrixHypothesesInner({

    required  this.id,

    required  this.label,

    required  this.probability,

     this.estimativeTerm,

    required  this.inconsistencyScore,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final String label;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: true,
    includeIfNull: false,
  )


  final num probability;



  @JsonKey(
    
    name: r'estimativeTerm',
    required: false,
    includeIfNull: false,
  )


  final IcdTerm? estimativeTerm;



      /// −Σ w·inc (I = 1, II = 2)
  @JsonKey(
    
    name: r'inconsistencyScore',
    required: true,
    includeIfNull: false,
  )


  final num inconsistencyScore;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AchMatrixHypothesesInner &&
      other.id == id &&
      other.label == label &&
      other.probability == probability &&
      other.estimativeTerm == estimativeTerm &&
      other.inconsistencyScore == inconsistencyScore;

    @override
    int get hashCode =>
        id.hashCode +
        label.hashCode +
        probability.hashCode +
        estimativeTerm.hashCode +
        inconsistencyScore.hashCode;

  factory AchMatrixHypothesesInner.fromJson(Map<String, dynamic> json) => _$AchMatrixHypothesesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$AchMatrixHypothesesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

