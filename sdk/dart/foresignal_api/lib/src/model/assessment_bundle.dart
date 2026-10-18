//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/assessment_bundle_objects_inner.dart';
import 'package:foresignal_api/src/model/assessment.dart';
import 'package:foresignal_api/src/model/analysis_manifest.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'assessment_bundle.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AssessmentBundle {
  /// Returns a new [AssessmentBundle] instance.
  AssessmentBundle({

    required  this.assessment,

    required  this.generatedAt,

    required  this.objects,

     this.manifests,
  });

  @JsonKey(
    
    name: r'assessment',
    required: true,
    includeIfNull: false,
  )


  final Assessment assessment;



  @JsonKey(
    
    name: r'generatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime generatedAt;



  @JsonKey(
    
    name: r'objects',
    required: true,
    includeIfNull: false,
  )


  final List<AssessmentBundleObjectsInner> objects;



  @JsonKey(
    
    name: r'manifests',
    required: false,
    includeIfNull: false,
  )


  final List<AnalysisManifest>? manifests;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AssessmentBundle &&
      other.assessment == assessment &&
      other.generatedAt == generatedAt &&
      other.objects == objects &&
      other.manifests == manifests;

    @override
    int get hashCode =>
        assessment.hashCode +
        generatedAt.hashCode +
        objects.hashCode +
        manifests.hashCode;

  factory AssessmentBundle.fromJson(Map<String, dynamic> json) => _$AssessmentBundleFromJson(json);

  Map<String, dynamic> toJson() => _$AssessmentBundleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

