//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'correlation_recipe_definition_activate_at.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CorrelationRecipeDefinitionActivateAt {
  /// Returns a new [CorrelationRecipeDefinitionActivateAt] instance.
  CorrelationRecipeDefinitionActivateAt({

     this.members,

     this.confidence,
  });

  @JsonKey(
    
    name: r'members',
    required: false,
    includeIfNull: false,
  )


  final int? members;



  @JsonKey(
    
    name: r'confidence',
    required: false,
    includeIfNull: false,
  )


  final num? confidence;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CorrelationRecipeDefinitionActivateAt &&
      other.members == members &&
      other.confidence == confidence;

    @override
    int get hashCode =>
        members.hashCode +
        confidence.hashCode;

  factory CorrelationRecipeDefinitionActivateAt.fromJson(Map<String, dynamic> json) => _$CorrelationRecipeDefinitionActivateAtFromJson(json);

  Map<String, dynamic> toJson() => _$CorrelationRecipeDefinitionActivateAtToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

