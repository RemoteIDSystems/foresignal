//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/correlation_recipe_definition.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'correlation_recipe_create.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CorrelationRecipeCreate {
  /// Returns a new [CorrelationRecipeCreate] instance.
  CorrelationRecipeCreate({

    required  this.id,

    required  this.definition,

     this.enabled,
  });

      /// A slug not already in use and not a bundled recipe id
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'definition',
    required: true,
    includeIfNull: false,
  )


  final CorrelationRecipeDefinition definition;



      /// Defaults to true
  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CorrelationRecipeCreate &&
      other.id == id &&
      other.definition == definition &&
      other.enabled == enabled;

    @override
    int get hashCode =>
        id.hashCode +
        definition.hashCode +
        enabled.hashCode;

  factory CorrelationRecipeCreate.fromJson(Map<String, dynamic> json) => _$CorrelationRecipeCreateFromJson(json);

  Map<String, dynamic> toJson() => _$CorrelationRecipeCreateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

