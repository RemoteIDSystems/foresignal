//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/severity.dart';
import 'package:foresignal_api/src/model/correlation_recipe_definition_dedup.dart';
import 'package:foresignal_api/src/model/correlation_recipe_definition_seed.dart';
import 'package:foresignal_api/src/model/correlation_recipe_definition_activate_at.dart';
import 'package:foresignal_api/src/model/correlation_recipe_definition_contradictions_inner.dart';
import 'package:foresignal_api/src/model/correlation_recipe_definition_joins_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'correlation_recipe_definition.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CorrelationRecipeDefinition {
  /// Returns a new [CorrelationRecipeDefinition] instance.
  CorrelationRecipeDefinition({

    required  this.type,

    required  this.seed,

     this.joins,

     this.minMembers,

     this.minSignalFamilies,

     this.activateAt,

     this.contradictions,

     this.severityMap,

     this.dedup,

     this.quietPeriod,

     this.reopenWindow,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final CorrelationRecipeDefinitionTypeEnum type;



  @JsonKey(
    
    name: r'seed',
    required: true,
    includeIfNull: false,
  )


  final CorrelationRecipeDefinitionSeed seed;



  @JsonKey(
    
    name: r'joins',
    required: false,
    includeIfNull: false,
  )


  final List<CorrelationRecipeDefinitionJoinsInner>? joins;



          // minimum: 1
  @JsonKey(
    
    name: r'minMembers',
    required: false,
    includeIfNull: false,
  )


  final int? minMembers;



          // minimum: 1
  @JsonKey(
    
    name: r'minSignalFamilies',
    required: false,
    includeIfNull: false,
  )


  final int? minSignalFamilies;



  @JsonKey(
    
    name: r'activateAt',
    required: false,
    includeIfNull: false,
  )


  final CorrelationRecipeDefinitionActivateAt? activateAt;



  @JsonKey(
    
    name: r'contradictions',
    required: false,
    includeIfNull: false,
  )


  final List<CorrelationRecipeDefinitionContradictionsInner>? contradictions;



      /// Rule → severity mapping
  @JsonKey(
    
    name: r'severityMap',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Severity>? severityMap;



  @JsonKey(
    
    name: r'dedup',
    required: false,
    includeIfNull: false,
  )


  final CorrelationRecipeDefinitionDedup? dedup;



      /// Resolve after this long without new supporting evidence. ISO-8601 duration or shorthand (30m, 6h, 7d).
  @JsonKey(
    
    name: r'quietPeriod',
    required: false,
    includeIfNull: false,
  )


  final String? quietPeriod;



      /// Reopen when evidence arrives within this window after resolution. ISO-8601 duration or shorthand (30m, 6h, 7d).
  @JsonKey(
    
    name: r'reopenWindow',
    required: false,
    includeIfNull: false,
  )


  final String? reopenWindow;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CorrelationRecipeDefinition &&
      other.type == type &&
      other.seed == seed &&
      other.joins == joins &&
      other.minMembers == minMembers &&
      other.minSignalFamilies == minSignalFamilies &&
      other.activateAt == activateAt &&
      other.contradictions == contradictions &&
      other.severityMap == severityMap &&
      other.dedup == dedup &&
      other.quietPeriod == quietPeriod &&
      other.reopenWindow == reopenWindow;

    @override
    int get hashCode =>
        type.hashCode +
        seed.hashCode +
        joins.hashCode +
        minMembers.hashCode +
        minSignalFamilies.hashCode +
        activateAt.hashCode +
        contradictions.hashCode +
        severityMap.hashCode +
        dedup.hashCode +
        quietPeriod.hashCode +
        reopenWindow.hashCode;

  factory CorrelationRecipeDefinition.fromJson(Map<String, dynamic> json) => _$CorrelationRecipeDefinitionFromJson(json);

  Map<String, dynamic> toJson() => _$CorrelationRecipeDefinitionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CorrelationRecipeDefinitionTypeEnum {
@JsonValue(r'maritime-disruption')
maritimeDisruption(r'maritime-disruption'),
@JsonValue(r'aviation-anomaly')
aviationAnomaly(r'aviation-anomaly'),
@JsonValue(r'infrastructure-disruption')
infrastructureDisruption(r'infrastructure-disruption'),
@JsonValue(r'weather-impact')
weatherImpact(r'weather-impact'),
@JsonValue(r'seismic')
seismic(r'seismic'),
@JsonValue(r'custom')
custom(r'custom');

const CorrelationRecipeDefinitionTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


