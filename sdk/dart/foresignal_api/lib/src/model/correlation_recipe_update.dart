//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/correlation_recipe_definition.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'correlation_recipe_update.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CorrelationRecipeUpdate {
  /// Returns a new [CorrelationRecipeUpdate] instance.
  CorrelationRecipeUpdate({

     this.enabled,

     this.params,

     this.definition,
  });

  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



      /// Parameter overrides; null removes them
  @JsonKey(
    
    name: r'params',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? params;



      /// A full operator-authored definition to replace (prompt 157); rejected for a bundled recipe, whose definition is managed by the classpath
  @JsonKey(
    
    name: r'definition',
    required: false,
    includeIfNull: false,
  )


  final CorrelationRecipeDefinition? definition;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CorrelationRecipeUpdate &&
      other.enabled == enabled &&
      other.params == params &&
      other.definition == definition;

    @override
    int get hashCode =>
        enabled.hashCode +
        (params == null ? 0 : params.hashCode) +
        definition.hashCode;

  factory CorrelationRecipeUpdate.fromJson(Map<String, dynamic> json) => _$CorrelationRecipeUpdateFromJson(json);

  Map<String, dynamic> toJson() => _$CorrelationRecipeUpdateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

