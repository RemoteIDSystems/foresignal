//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/regional_index_def_skill.dart';
import 'package:foresignal_api/src/model/regional_index_def_components_inner.dart';
import 'package:foresignal_api/src/model/regional_index_def_floors_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'regional_index_def.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RegionalIndexDef {
  /// Returns a new [RegionalIndexDef] instance.
  RegionalIndexDef({

    required  this.id,

    required  this.name,

     this.description,

    required  this.components,

    required  this.weights,

     this.floors,

    required  this.target,

    required  this.weightsVersion,

    required  this.weightsOrigin,

     this.autoApplyFittedWeights = false,

    required  this.enabled,

     this.builtIn,

     this.ready,

    required  this.skill,

     this.updatedAt,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'components',
    required: true,
    includeIfNull: false,
  )


  final List<RegionalIndexDefComponentsInner> components;



      /// Component key → weight (sums to 1)
  @JsonKey(
    
    name: r'weights',
    required: true,
    includeIfNull: false,
  )


  final Map<String, num> weights;



  @JsonKey(
    
    name: r'floors',
    required: false,
    includeIfNull: false,
  )


  final List<RegionalIndexDefFloorsInner>? floors;



      /// Outlook family the index should anticipate
  @JsonKey(
    
    name: r'target',
    required: true,
    includeIfNull: false,
  )


  final String target;



  @JsonKey(
    
    name: r'weightsVersion',
    required: true,
    includeIfNull: false,
  )


  final int weightsVersion;



  @JsonKey(
    
    name: r'weightsOrigin',
    required: true,
    includeIfNull: false,
  )


  final RegionalIndexDefWeightsOriginEnum weightsOrigin;



  @JsonKey(
    defaultValue: false,
    name: r'autoApplyFittedWeights',
    required: false,
    includeIfNull: false,
  )


  final bool? autoApplyFittedWeights;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'builtIn',
    required: false,
    includeIfNull: false,
  )


  final bool? builtIn;



      /// All component series exist
  @JsonKey(
    
    name: r'ready',
    required: false,
    includeIfNull: false,
  )


  final bool? ready;



  @JsonKey(
    
    name: r'skill',
    required: true,
    includeIfNull: true,
  )


  final RegionalIndexDefSkill? skill;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RegionalIndexDef &&
      other.id == id &&
      other.name == name &&
      other.description == description &&
      other.components == components &&
      other.weights == weights &&
      other.floors == floors &&
      other.target == target &&
      other.weightsVersion == weightsVersion &&
      other.weightsOrigin == weightsOrigin &&
      other.autoApplyFittedWeights == autoApplyFittedWeights &&
      other.enabled == enabled &&
      other.builtIn == builtIn &&
      other.ready == ready &&
      other.skill == skill &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        (description == null ? 0 : description.hashCode) +
        components.hashCode +
        weights.hashCode +
        floors.hashCode +
        target.hashCode +
        weightsVersion.hashCode +
        weightsOrigin.hashCode +
        autoApplyFittedWeights.hashCode +
        enabled.hashCode +
        builtIn.hashCode +
        ready.hashCode +
        (skill == null ? 0 : skill.hashCode) +
        (updatedAt == null ? 0 : updatedAt.hashCode);

  factory RegionalIndexDef.fromJson(Map<String, dynamic> json) => _$RegionalIndexDefFromJson(json);

  Map<String, dynamic> toJson() => _$RegionalIndexDefToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RegionalIndexDefWeightsOriginEnum {
@JsonValue(r'default')
default_(r'default'),
@JsonValue(r'fitted')
fitted(r'fitted'),
@JsonValue(r'operator')
operator_(r'operator');

const RegionalIndexDefWeightsOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


