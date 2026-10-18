//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/condition.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'correlation_recipe_definition_seed.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CorrelationRecipeDefinitionSeed {
  /// Returns a new [CorrelationRecipeDefinitionSeed] instance.
  CorrelationRecipeDefinitionSeed({

     this.kinds,

     this.sources,

     this.conditions,
  });

  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



  @JsonKey(
    
    name: r'conditions',
    required: false,
    includeIfNull: false,
  )


  final List<Condition>? conditions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CorrelationRecipeDefinitionSeed &&
      other.kinds == kinds &&
      other.sources == sources &&
      other.conditions == conditions;

    @override
    int get hashCode =>
        kinds.hashCode +
        sources.hashCode +
        conditions.hashCode;

  factory CorrelationRecipeDefinitionSeed.fromJson(Map<String, dynamic> json) => _$CorrelationRecipeDefinitionSeedFromJson(json);

  Map<String, dynamic> toJson() => _$CorrelationRecipeDefinitionSeedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

