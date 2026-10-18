//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'correlation_recipe_definition_dedup.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CorrelationRecipeDefinitionDedup {
  /// Returns a new [CorrelationRecipeDefinitionDedup] instance.
  CorrelationRecipeDefinitionDedup({

     this.radiusKm,

     this.window,
  });

  @JsonKey(
    
    name: r'radiusKm',
    required: false,
    includeIfNull: false,
  )


  final num? radiusKm;



      /// Overlap window. ISO-8601 duration or shorthand (30m, 6h, 7d).
  @JsonKey(
    
    name: r'window',
    required: false,
    includeIfNull: false,
  )


  final String? window;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CorrelationRecipeDefinitionDedup &&
      other.radiusKm == radiusKm &&
      other.window == window;

    @override
    int get hashCode =>
        radiusKm.hashCode +
        window.hashCode;

  factory CorrelationRecipeDefinitionDedup.fromJson(Map<String, dynamic> json) => _$CorrelationRecipeDefinitionDedupFromJson(json);

  Map<String, dynamic> toJson() => _$CorrelationRecipeDefinitionDedupToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

