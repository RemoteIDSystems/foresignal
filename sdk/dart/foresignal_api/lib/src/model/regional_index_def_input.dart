//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/regional_index_def_components_inner.dart';
import 'package:foresignal_api/src/model/regional_index_def_floors_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'regional_index_def_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RegionalIndexDefInput {
  /// Returns a new [RegionalIndexDefInput] instance.
  RegionalIndexDefInput({

    required  this.name,

     this.description,

    required  this.components,

    required  this.weights,

     this.floors,

    required  this.target,

    required  this.enabled,

     this.autoApplyFittedWeights = false,
  });

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



  @JsonKey(
    
    name: r'target',
    required: true,
    includeIfNull: false,
  )


  final String target;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    defaultValue: false,
    name: r'autoApplyFittedWeights',
    required: false,
    includeIfNull: false,
  )


  final bool? autoApplyFittedWeights;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RegionalIndexDefInput &&
      other.name == name &&
      other.description == description &&
      other.components == components &&
      other.weights == weights &&
      other.floors == floors &&
      other.target == target &&
      other.enabled == enabled &&
      other.autoApplyFittedWeights == autoApplyFittedWeights;

    @override
    int get hashCode =>
        name.hashCode +
        (description == null ? 0 : description.hashCode) +
        components.hashCode +
        weights.hashCode +
        floors.hashCode +
        target.hashCode +
        enabled.hashCode +
        autoApplyFittedWeights.hashCode;

  factory RegionalIndexDefInput.fromJson(Map<String, dynamic> json) => _$RegionalIndexDefInputFromJson(json);

  Map<String, dynamic> toJson() => _$RegionalIndexDefInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

