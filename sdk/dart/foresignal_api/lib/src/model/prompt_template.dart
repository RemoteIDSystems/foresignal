//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'prompt_template.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PromptTemplate {
  /// Returns a new [PromptTemplate] instance.
  PromptTemplate({

    required  this.id,

    required  this.version,

     this.role,

    required  this.sha256,

    required  this.text,

     this.versions,

     this.instructions,
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



  @JsonKey(
    
    name: r'role',
    required: false,
    includeIfNull: false,
  )


  final String? role;



  @JsonKey(
    
    name: r'sha256',
    required: true,
    includeIfNull: false,
  )


  final String sha256;



  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;



      /// All available versions
  @JsonKey(
    
    name: r'versions',
    required: false,
    includeIfNull: false,
  )


  final List<String>? versions;



      /// Operator instructions; returned to agent scope only
  @JsonKey(
    
    name: r'instructions',
    required: false,
    includeIfNull: false,
  )


  final String? instructions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PromptTemplate &&
      other.id == id &&
      other.version == version &&
      other.role == role &&
      other.sha256 == sha256 &&
      other.text == text &&
      other.versions == versions &&
      other.instructions == instructions;

    @override
    int get hashCode =>
        id.hashCode +
        version.hashCode +
        (role == null ? 0 : role.hashCode) +
        sha256.hashCode +
        text.hashCode +
        versions.hashCode +
        (instructions == null ? 0 : instructions.hashCode);

  factory PromptTemplate.fromJson(Map<String, dynamic> json) => _$PromptTemplateFromJson(json);

  Map<String, dynamic> toJson() => _$PromptTemplateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

