//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/analysis_manifest_inputs.dart';
import 'package:foresignal_api/src/model/analysis_manifest_model.dart';
import 'package:foresignal_api/src/model/analysis_manifest_build.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analysis_manifest.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalysisManifest {
  /// Returns a new [AnalysisManifest] instance.
  AnalysisManifest({

    required  this.id,

    required  this.component,

    required  this.componentVersion,

    required  this.build,

    required  this.config,

    required  this.configHash,

    required  this.registries,

     this.asOfSeq,

    required  this.inputs,

     this.model,

    required  this.contentHash,

    required  this.createdAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'component',
    required: true,
    includeIfNull: false,
  )


  final String component;



  @JsonKey(
    
    name: r'componentVersion',
    required: true,
    includeIfNull: false,
  )


  final String componentVersion;



  @JsonKey(
    
    name: r'build',
    required: true,
    includeIfNull: false,
  )


  final AnalysisManifestBuild build;



  @JsonKey(
    
    name: r'config',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> config;



  @JsonKey(
    
    name: r'configHash',
    required: true,
    includeIfNull: false,
  )


  final String configHash;



      /// Registry file name → sha256, e.g. signal-families.json, originators.json
  @JsonKey(
    
    name: r'registries',
    required: true,
    includeIfNull: false,
  )


  final Map<String, String> registries;



  @JsonKey(
    
    name: r'asOfSeq',
    required: false,
    includeIfNull: false,
  )


  final int? asOfSeq;



  @JsonKey(
    
    name: r'inputs',
    required: true,
    includeIfNull: false,
  )


  final AnalysisManifestInputs inputs;



  @JsonKey(
    
    name: r'model',
    required: false,
    includeIfNull: false,
  )


  final AnalysisManifestModel? model;



  @JsonKey(
    
    name: r'contentHash',
    required: true,
    includeIfNull: false,
  )


  final String contentHash;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalysisManifest &&
      other.id == id &&
      other.component == component &&
      other.componentVersion == componentVersion &&
      other.build == build &&
      other.config == config &&
      other.configHash == configHash &&
      other.registries == registries &&
      other.asOfSeq == asOfSeq &&
      other.inputs == inputs &&
      other.model == model &&
      other.contentHash == contentHash &&
      other.createdAt == createdAt;

    @override
    int get hashCode =>
        id.hashCode +
        component.hashCode +
        componentVersion.hashCode +
        build.hashCode +
        config.hashCode +
        configHash.hashCode +
        registries.hashCode +
        (asOfSeq == null ? 0 : asOfSeq.hashCode) +
        inputs.hashCode +
        (model == null ? 0 : model.hashCode) +
        contentHash.hashCode +
        createdAt.hashCode;

  factory AnalysisManifest.fromJson(Map<String, dynamic> json) => _$AnalysisManifestFromJson(json);

  Map<String, dynamic> toJson() => _$AnalysisManifestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

