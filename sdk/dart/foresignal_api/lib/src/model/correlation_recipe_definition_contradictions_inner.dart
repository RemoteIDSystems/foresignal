//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'correlation_recipe_definition_contradictions_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CorrelationRecipeDefinitionContradictionsInner {
  /// Returns a new [CorrelationRecipeDefinitionContradictionsInner] instance.
  CorrelationRecipeDefinitionContradictionsInner({

     this.kinds,

     this.healthOf,

     this.withinKm,

     this.window,

    required  this.alternative,
  });

  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



      /// Source ids whose health or coverage drop counts
  @JsonKey(
    
    name: r'healthOf',
    required: false,
    includeIfNull: false,
  )


  final List<String>? healthOf;



  @JsonKey(
    
    name: r'withinKm',
    required: false,
    includeIfNull: false,
  )


  final num? withinKm;



      /// Window. ISO-8601 duration or shorthand (30m, 6h, 7d).
  @JsonKey(
    
    name: r'window',
    required: false,
    includeIfNull: false,
  )


  final String? window;



  @JsonKey(
    
    name: r'alternative',
    required: true,
    includeIfNull: false,
  )


  final String alternative;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CorrelationRecipeDefinitionContradictionsInner &&
      other.kinds == kinds &&
      other.healthOf == healthOf &&
      other.withinKm == withinKm &&
      other.window == window &&
      other.alternative == alternative;

    @override
    int get hashCode =>
        kinds.hashCode +
        healthOf.hashCode +
        withinKm.hashCode +
        window.hashCode +
        alternative.hashCode;

  factory CorrelationRecipeDefinitionContradictionsInner.fromJson(Map<String, dynamic> json) => _$CorrelationRecipeDefinitionContradictionsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$CorrelationRecipeDefinitionContradictionsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

