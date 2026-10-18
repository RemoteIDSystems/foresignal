//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/recipe_backtest_result_overlap.dart';
import 'package:foresignal_api/src/model/recipe_backtest_result_events_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'recipe_backtest_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecipeBacktestResult {
  /// Returns a new [RecipeBacktestResult] instance.
  RecipeBacktestResult({

    required  this.recipeId,

     this.recipeVersion,

    required  this.events,

     this.countsByType,

     this.overlap,
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
    
    name: r'events',
    required: true,
    includeIfNull: false,
  )


  final List<RecipeBacktestResultEventsInner> events;



  @JsonKey(
    
    name: r'countsByType',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? countsByType;



  @JsonKey(
    
    name: r'overlap',
    required: false,
    includeIfNull: false,
  )


  final RecipeBacktestResultOverlap? overlap;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecipeBacktestResult &&
      other.recipeId == recipeId &&
      other.recipeVersion == recipeVersion &&
      other.events == events &&
      other.countsByType == countsByType &&
      other.overlap == overlap;

    @override
    int get hashCode =>
        recipeId.hashCode +
        recipeVersion.hashCode +
        events.hashCode +
        countsByType.hashCode +
        overlap.hashCode;

  factory RecipeBacktestResult.fromJson(Map<String, dynamic> json) => _$RecipeBacktestResultFromJson(json);

  Map<String, dynamic> toJson() => _$RecipeBacktestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

