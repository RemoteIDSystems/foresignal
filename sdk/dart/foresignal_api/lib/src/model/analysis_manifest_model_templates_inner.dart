//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analysis_manifest_model_templates_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalysisManifestModelTemplatesInner {
  /// Returns a new [AnalysisManifestModelTemplatesInner] instance.
  AnalysisManifestModelTemplatesInner({

    required  this.templateId,

    required  this.version,
  });

  @JsonKey(
    
    name: r'templateId',
    required: true,
    includeIfNull: false,
  )


  final String templateId;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final String version;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalysisManifestModelTemplatesInner &&
      other.templateId == templateId &&
      other.version == version;

    @override
    int get hashCode =>
        templateId.hashCode +
        version.hashCode;

  factory AnalysisManifestModelTemplatesInner.fromJson(Map<String, dynamic> json) => _$AnalysisManifestModelTemplatesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$AnalysisManifestModelTemplatesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

