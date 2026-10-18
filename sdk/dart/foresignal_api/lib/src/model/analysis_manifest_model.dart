//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/analysis_manifest_model_templates_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analysis_manifest_model.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalysisManifestModel {
  /// Returns a new [AnalysisManifestModel] instance.
  AnalysisManifestModel({

    required  this.provider,

    required  this.model,

     this.effort,

     this.promptHash,

     this.toolsHash,

     this.samples,

     this.templates,
  });

  @JsonKey(
    
    name: r'provider',
    required: true,
    includeIfNull: false,
  )


  final String provider;



  @JsonKey(
    
    name: r'model',
    required: true,
    includeIfNull: false,
  )


  final String model;



  @JsonKey(
    
    name: r'effort',
    required: false,
    includeIfNull: false,
  )


  final String? effort;



  @JsonKey(
    
    name: r'promptHash',
    required: false,
    includeIfNull: false,
  )


  final String? promptHash;



  @JsonKey(
    
    name: r'toolsHash',
    required: false,
    includeIfNull: false,
  )


  final String? toolsHash;



  @JsonKey(
    
    name: r'samples',
    required: false,
    includeIfNull: false,
  )


  final int? samples;



  @JsonKey(
    
    name: r'templates',
    required: false,
    includeIfNull: false,
  )


  final List<AnalysisManifestModelTemplatesInner>? templates;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalysisManifestModel &&
      other.provider == provider &&
      other.model == model &&
      other.effort == effort &&
      other.promptHash == promptHash &&
      other.toolsHash == toolsHash &&
      other.samples == samples &&
      other.templates == templates;

    @override
    int get hashCode =>
        provider.hashCode +
        model.hashCode +
        (effort == null ? 0 : effort.hashCode) +
        promptHash.hashCode +
        (toolsHash == null ? 0 : toolsHash.hashCode) +
        (samples == null ? 0 : samples.hashCode) +
        templates.hashCode;

  factory AnalysisManifestModel.fromJson(Map<String, dynamic> json) => _$AnalysisManifestModelFromJson(json);

  Map<String, dynamic> toJson() => _$AnalysisManifestModelToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

