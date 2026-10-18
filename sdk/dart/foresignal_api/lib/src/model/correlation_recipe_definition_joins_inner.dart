//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/correlation_recipe_definition_joins_inner_window.dart';
import 'package:foresignal_api/src/model/condition.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'correlation_recipe_definition_joins_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CorrelationRecipeDefinitionJoinsInner {
  /// Returns a new [CorrelationRecipeDefinitionJoinsInner] instance.
  CorrelationRecipeDefinitionJoinsInner({

     this.kinds,

     this.conditions,

     this.withinKm,

     this.sameRegion,

     this.sameEntity,

     this.window,

     this.role,

     this.signalFamily,

     this.weight,
  });

  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



  @JsonKey(
    
    name: r'conditions',
    required: false,
    includeIfNull: false,
  )


  final List<Condition>? conditions;



  @JsonKey(
    
    name: r'withinKm',
    required: false,
    includeIfNull: false,
  )


  final num? withinKm;



  @JsonKey(
    
    name: r'sameRegion',
    required: false,
    includeIfNull: false,
  )


  final CorrelationRecipeDefinitionJoinsInnerSameRegionEnum? sameRegion;



  @JsonKey(
    
    name: r'sameEntity',
    required: false,
    includeIfNull: false,
  )


  final bool? sameEntity;



  @JsonKey(
    
    name: r'window',
    required: false,
    includeIfNull: false,
  )


  final CorrelationRecipeDefinitionJoinsInnerWindow? window;



  @JsonKey(
    
    name: r'role',
    required: false,
    includeIfNull: false,
  )


  final CorrelationRecipeDefinitionJoinsInnerRoleEnum? role;



      /// Signal family from the shared registry (`intel/signal-families.json`, 119), e.g. `ais-behaviour`, `nav-warning`, `internet-measurement`
  @JsonKey(
    
    name: r'signalFamily',
    required: false,
    includeIfNull: false,
  )


  final String? signalFamily;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'weight',
    required: false,
    includeIfNull: false,
  )


  final num? weight;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CorrelationRecipeDefinitionJoinsInner &&
      other.kinds == kinds &&
      other.conditions == conditions &&
      other.withinKm == withinKm &&
      other.sameRegion == sameRegion &&
      other.sameEntity == sameEntity &&
      other.window == window &&
      other.role == role &&
      other.signalFamily == signalFamily &&
      other.weight == weight;

    @override
    int get hashCode =>
        kinds.hashCode +
        conditions.hashCode +
        withinKm.hashCode +
        sameRegion.hashCode +
        sameEntity.hashCode +
        window.hashCode +
        role.hashCode +
        signalFamily.hashCode +
        weight.hashCode;

  factory CorrelationRecipeDefinitionJoinsInner.fromJson(Map<String, dynamic> json) => _$CorrelationRecipeDefinitionJoinsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$CorrelationRecipeDefinitionJoinsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CorrelationRecipeDefinitionJoinsInnerSameRegionEnum {
@JsonValue(r'country')
country(r'country'),
@JsonValue(r'admin1')
admin1(r'admin1');

const CorrelationRecipeDefinitionJoinsInnerSameRegionEnum(this.value);

final String value;

@override
String toString() => value;
}


enum CorrelationRecipeDefinitionJoinsInnerRoleEnum {
@JsonValue(r'supporting')
supporting(r'supporting'),
@JsonValue(r'contradicting')
contradicting(r'contradicting'),
@JsonValue(r'context')
context(r'context');

const CorrelationRecipeDefinitionJoinsInnerRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


