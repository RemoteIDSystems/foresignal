//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
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
  });

  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



      /// Parameter overrides
  @JsonKey(
    
    name: r'params',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? params;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CorrelationRecipeUpdate &&
      other.enabled == enabled &&
      other.params == params;

    @override
    int get hashCode =>
        enabled.hashCode +
        params.hashCode;

  factory CorrelationRecipeUpdate.fromJson(Map<String, dynamic> json) => _$CorrelationRecipeUpdateFromJson(json);

  Map<String, dynamic> toJson() => _$CorrelationRecipeUpdateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

