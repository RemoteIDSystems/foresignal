//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_inference.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationInference {
  /// Returns a new [SituationInference] instance.
  SituationInference({

    required  this.summary,

     this.summaryTemplateVersion,

    required  this.statusBasis,

    required  this.severityBasis,

     this.linker,

     this.manifestId,
  });

  @JsonKey(
    
    name: r'summary',
    required: true,
    includeIfNull: false,
  )


  final String summary;



  @JsonKey(
    
    name: r'summaryTemplateVersion',
    required: false,
    includeIfNull: false,
  )


  final String? summaryTemplateVersion;



      /// Why the status is what it is, e.g. `3 signal families, 2 independent evidence groups`
  @JsonKey(
    
    name: r'statusBasis',
    required: true,
    includeIfNull: false,
  )


  final String statusBasis;



      /// Max core-member severity, or the override and who set it
  @JsonKey(
    
    name: r'severityBasis',
    required: true,
    includeIfNull: false,
  )


  final String severityBasis;



  @JsonKey(
    
    name: r'linker',
    required: false,
    includeIfNull: false,
  )


  final DetectorRef? linker;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationInference &&
      other.summary == summary &&
      other.summaryTemplateVersion == summaryTemplateVersion &&
      other.statusBasis == statusBasis &&
      other.severityBasis == severityBasis &&
      other.linker == linker &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        summary.hashCode +
        summaryTemplateVersion.hashCode +
        statusBasis.hashCode +
        severityBasis.hashCode +
        linker.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode);

  factory SituationInference.fromJson(Map<String, dynamic> json) => _$SituationInferenceFromJson(json);

  Map<String, dynamic> toJson() => _$SituationInferenceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

