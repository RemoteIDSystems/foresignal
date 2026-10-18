//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/assessment_coverage_situations_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'assessment_coverage.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AssessmentCoverage {
  /// Returns a new [AssessmentCoverage] instance.
  AssessmentCoverage({

     this.situations,

     this.blindspotStoryIds,

     this.divergenceFindingIds,
  });

  @JsonKey(
    
    name: r'situations',
    required: false,
    includeIfNull: false,
  )


  final List<AssessmentCoverageSituationsInner>? situations;



  @JsonKey(
    
    name: r'blindspotStoryIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? blindspotStoryIds;



  @JsonKey(
    
    name: r'divergenceFindingIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? divergenceFindingIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AssessmentCoverage &&
      other.situations == situations &&
      other.blindspotStoryIds == blindspotStoryIds &&
      other.divergenceFindingIds == divergenceFindingIds;

    @override
    int get hashCode =>
        situations.hashCode +
        blindspotStoryIds.hashCode +
        divergenceFindingIds.hashCode;

  factory AssessmentCoverage.fromJson(Map<String, dynamic> json) => _$AssessmentCoverageFromJson(json);

  Map<String, dynamic> toJson() => _$AssessmentCoverageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

