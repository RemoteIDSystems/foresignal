//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'assessment_prompt_templates_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AssessmentPromptTemplatesInner {
  /// Returns a new [AssessmentPromptTemplatesInner] instance.
  AssessmentPromptTemplatesInner({

    required  this.id,

    required  this.version,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final String version;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AssessmentPromptTemplatesInner &&
      other.id == id &&
      other.version == version;

    @override
    int get hashCode =>
        id.hashCode +
        version.hashCode;

  factory AssessmentPromptTemplatesInner.fromJson(Map<String, dynamic> json) => _$AssessmentPromptTemplatesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$AssessmentPromptTemplatesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

