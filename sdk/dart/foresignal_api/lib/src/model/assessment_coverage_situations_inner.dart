//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/coverage_stats.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'assessment_coverage_situations_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AssessmentCoverageSituationsInner {
  /// Returns a new [AssessmentCoverageSituationsInner] instance.
  AssessmentCoverageSituationsInner({

    required  this.situationId,

    required  this.coverage,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: true,
    includeIfNull: false,
  )


  final String situationId;



  @JsonKey(
    
    name: r'coverage',
    required: true,
    includeIfNull: false,
  )


  final CoverageStats coverage;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AssessmentCoverageSituationsInner &&
      other.situationId == situationId &&
      other.coverage == coverage;

    @override
    int get hashCode =>
        situationId.hashCode +
        coverage.hashCode;

  factory AssessmentCoverageSituationsInner.fromJson(Map<String, dynamic> json) => _$AssessmentCoverageSituationsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$AssessmentCoverageSituationsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

