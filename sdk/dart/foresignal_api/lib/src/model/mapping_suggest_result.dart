//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/mapping_warning.dart';
import 'package:foresignal_api/src/model/record_mapping.dart';
import 'package:foresignal_api/src/model/mapping_preview_result.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_suggest_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingSuggestResult {
  /// Returns a new [MappingSuggestResult] instance.
  MappingSuggestResult({

    required  this.draft,

    required  this.explanation,

    required  this.warnings,

     this.preview,

     this.repairRounds,

    required  this.provider,

    required  this.model,
  });

  @JsonKey(
    
    name: r'draft',
    required: true,
    includeIfNull: false,
  )


  final RecordMapping draft;



  @JsonKey(
    
    name: r'explanation',
    required: true,
    includeIfNull: false,
  )


  final String explanation;



  @JsonKey(
    
    name: r'warnings',
    required: true,
    includeIfNull: false,
  )


  final List<MappingWarning> warnings;



  @JsonKey(
    
    name: r'preview',
    required: false,
    includeIfNull: false,
  )


  final MappingPreviewResult? preview;



          // minimum: 0
          // maximum: 2
  @JsonKey(
    
    name: r'repairRounds',
    required: false,
    includeIfNull: false,
  )


  final int? repairRounds;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingSuggestResult &&
      other.draft == draft &&
      other.explanation == explanation &&
      other.warnings == warnings &&
      other.preview == preview &&
      other.repairRounds == repairRounds &&
      other.provider == provider &&
      other.model == model;

    @override
    int get hashCode =>
        draft.hashCode +
        explanation.hashCode +
        warnings.hashCode +
        preview.hashCode +
        repairRounds.hashCode +
        provider.hashCode +
        model.hashCode;

  factory MappingSuggestResult.fromJson(Map<String, dynamic> json) => _$MappingSuggestResultFromJson(json);

  Map<String, dynamic> toJson() => _$MappingSuggestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

