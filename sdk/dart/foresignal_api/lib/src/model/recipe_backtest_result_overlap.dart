//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'recipe_backtest_result_overlap.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecipeBacktestResultOverlap {
  /// Returns a new [RecipeBacktestResultOverlap] instance.
  RecipeBacktestResultOverlap({

     this.matched,

     this.onlyBacktest,

     this.onlyActual,
  });

  @JsonKey(
    
    name: r'matched',
    required: false,
    includeIfNull: false,
  )


  final int? matched;



  @JsonKey(
    
    name: r'onlyBacktest',
    required: false,
    includeIfNull: false,
  )


  final int? onlyBacktest;



  @JsonKey(
    
    name: r'onlyActual',
    required: false,
    includeIfNull: false,
  )


  final int? onlyActual;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecipeBacktestResultOverlap &&
      other.matched == matched &&
      other.onlyBacktest == onlyBacktest &&
      other.onlyActual == onlyActual;

    @override
    int get hashCode =>
        matched.hashCode +
        onlyBacktest.hashCode +
        onlyActual.hashCode;

  factory RecipeBacktestResultOverlap.fromJson(Map<String, dynamic> json) => _$RecipeBacktestResultOverlapFromJson(json);

  Map<String, dynamic> toJson() => _$RecipeBacktestResultOverlapToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

