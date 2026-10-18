//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_link_scores_evidence.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_link_scores.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLinkScores {
  /// Returns a new [ChronicleLinkScores] instance.
  ChronicleLinkScores({

     this.evidence,

     this.entities,

     this.areaTime,

     this.topics,
  });

  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final ChronicleLinkScoresEvidence? evidence;



  @JsonKey(
    
    name: r'entities',
    required: false,
    includeIfNull: false,
  )


  final ChronicleLinkScoresEvidence? entities;



  @JsonKey(
    
    name: r'area-time',
    required: false,
    includeIfNull: false,
  )


  final ChronicleLinkScoresEvidence? areaTime;



  @JsonKey(
    
    name: r'topics',
    required: false,
    includeIfNull: false,
  )


  final ChronicleLinkScoresEvidence? topics;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLinkScores &&
      other.evidence == evidence &&
      other.entities == entities &&
      other.areaTime == areaTime &&
      other.topics == topics;

    @override
    int get hashCode =>
        evidence.hashCode +
        entities.hashCode +
        areaTime.hashCode +
        topics.hashCode;

  factory ChronicleLinkScores.fromJson(Map<String, dynamic> json) => _$ChronicleLinkScoresFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLinkScoresToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

