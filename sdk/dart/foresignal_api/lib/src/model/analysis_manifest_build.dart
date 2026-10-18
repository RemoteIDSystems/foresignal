//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analysis_manifest_build.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalysisManifestBuild {
  /// Returns a new [AnalysisManifestBuild] instance.
  AnalysisManifestBuild({

    required  this.version,

     this.gitCommit,
  });

  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final String version;



  @JsonKey(
    
    name: r'gitCommit',
    required: false,
    includeIfNull: false,
  )


  final String? gitCommit;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalysisManifestBuild &&
      other.version == version &&
      other.gitCommit == gitCommit;

    @override
    int get hashCode =>
        version.hashCode +
        gitCommit.hashCode;

  factory AnalysisManifestBuild.fromJson(Map<String, dynamic> json) => _$AnalysisManifestBuildFromJson(json);

  Map<String, dynamic> toJson() => _$AnalysisManifestBuildToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

