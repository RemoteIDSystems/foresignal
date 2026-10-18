//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/intelligence_event.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'recipe_test_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecipeTestResult {
  /// Returns a new [RecipeTestResult] instance.
  RecipeTestResult({

    required  this.recipeId,

     this.recipeVersion,

    required  this.fromSeq,

    required  this.toSeq,

    required  this.events,

     this.truncated,
  });

  @JsonKey(
    
    name: r'recipeId',
    required: true,
    includeIfNull: false,
  )


  final String recipeId;



  @JsonKey(
    
    name: r'recipeVersion',
    required: false,
    includeIfNull: false,
  )


  final int? recipeVersion;



  @JsonKey(
    
    name: r'fromSeq',
    required: true,
    includeIfNull: false,
  )


  final int fromSeq;



  @JsonKey(
    
    name: r'toSeq',
    required: true,
    includeIfNull: false,
  )


  final int toSeq;



  @JsonKey(
    
    name: r'events',
    required: true,
    includeIfNull: false,
  )


  final List<IntelligenceEvent> events;



  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecipeTestResult &&
      other.recipeId == recipeId &&
      other.recipeVersion == recipeVersion &&
      other.fromSeq == fromSeq &&
      other.toSeq == toSeq &&
      other.events == events &&
      other.truncated == truncated;

    @override
    int get hashCode =>
        recipeId.hashCode +
        recipeVersion.hashCode +
        fromSeq.hashCode +
        toSeq.hashCode +
        events.hashCode +
        truncated.hashCode;

  factory RecipeTestResult.fromJson(Map<String, dynamic> json) => _$RecipeTestResultFromJson(json);

  Map<String, dynamic> toJson() => _$RecipeTestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

