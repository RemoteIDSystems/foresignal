//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'correlation_recipe_definition_joins_inner_window.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CorrelationRecipeDefinitionJoinsInnerWindow {
  /// Returns a new [CorrelationRecipeDefinitionJoinsInnerWindow] instance.
  CorrelationRecipeDefinitionJoinsInnerWindow({

     this.before,

     this.after,
  });

      /// Before the seed. ISO-8601 duration or shorthand (30m, 6h, 7d).
  @JsonKey(
    
    name: r'before',
    required: false,
    includeIfNull: false,
  )


  final String? before;



      /// After the seed. ISO-8601 duration or shorthand (30m, 6h, 7d).
  @JsonKey(
    
    name: r'after',
    required: false,
    includeIfNull: false,
  )


  final String? after;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CorrelationRecipeDefinitionJoinsInnerWindow &&
      other.before == before &&
      other.after == after;

    @override
    int get hashCode =>
        before.hashCode +
        after.hashCode;

  factory CorrelationRecipeDefinitionJoinsInnerWindow.fromJson(Map<String, dynamic> json) => _$CorrelationRecipeDefinitionJoinsInnerWindowFromJson(json);

  Map<String, dynamic> toJson() => _$CorrelationRecipeDefinitionJoinsInnerWindowToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

