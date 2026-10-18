//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cycle_run_roles_inner_prompt_template.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CycleRunRolesInnerPromptTemplate {
  /// Returns a new [CycleRunRolesInnerPromptTemplate] instance.
  CycleRunRolesInnerPromptTemplate({

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
    bool operator ==(Object other) => identical(this, other) || other is CycleRunRolesInnerPromptTemplate &&
      other.id == id &&
      other.version == version;

    @override
    int get hashCode =>
        id.hashCode +
        version.hashCode;

  factory CycleRunRolesInnerPromptTemplate.fromJson(Map<String, dynamic> json) => _$CycleRunRolesInnerPromptTemplateFromJson(json);

  Map<String, dynamic> toJson() => _$CycleRunRolesInnerPromptTemplateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

