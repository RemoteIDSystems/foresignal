//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_link_scores_evidence.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLinkScoresEvidence {
  /// Returns a new [ChronicleLinkScoresEvidence] instance.
  ChronicleLinkScoresEvidence({

     this.jaccard,

     this.containment,

     this.sharedEstimate,
  });

  @JsonKey(
    
    name: r'jaccard',
    required: false,
    includeIfNull: false,
  )


  final num? jaccard;



  @JsonKey(
    
    name: r'containment',
    required: false,
    includeIfNull: false,
  )


  final num? containment;



  @JsonKey(
    
    name: r'sharedEstimate',
    required: false,
    includeIfNull: false,
  )


  final num? sharedEstimate;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLinkScoresEvidence &&
      other.jaccard == jaccard &&
      other.containment == containment &&
      other.sharedEstimate == sharedEstimate;

    @override
    int get hashCode =>
        jaccard.hashCode +
        containment.hashCode +
        sharedEstimate.hashCode;

  factory ChronicleLinkScoresEvidence.fromJson(Map<String, dynamic> json) => _$ChronicleLinkScoresEvidenceFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLinkScoresEvidenceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

