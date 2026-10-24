//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'prompt_template_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PromptTemplateSummary {
  /// Returns a new [PromptTemplateSummary] instance.
  PromptTemplateSummary({

    required  this.id,

    required  this.role,

    required  this.version,

    required  this.versions,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// The LLM role that sends it
  @JsonKey(
    
    name: r'role',
    required: true,
    includeIfNull: true,
  )


  final String? role;



      /// The newest version
  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final String version;



      /// Every bundled version, oldest first
  @JsonKey(
    
    name: r'versions',
    required: true,
    includeIfNull: false,
  )


  final List<String> versions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PromptTemplateSummary &&
      other.id == id &&
      other.role == role &&
      other.version == version &&
      other.versions == versions;

    @override
    int get hashCode =>
        id.hashCode +
        (role == null ? 0 : role.hashCode) +
        version.hashCode +
        versions.hashCode;

  factory PromptTemplateSummary.fromJson(Map<String, dynamic> json) => _$PromptTemplateSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$PromptTemplateSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

