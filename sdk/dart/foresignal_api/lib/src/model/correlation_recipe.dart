//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/correlation_recipe_definition.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'correlation_recipe.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CorrelationRecipe {
  /// Returns a new [CorrelationRecipe] instance.
  CorrelationRecipe({

    required  this.id,

    required  this.version,

     this.builtIn,

     this.origin,

    required  this.enabled,

    required  this.definition,

     this.params,

     this.disabledReason,

     this.lastRunAt,

     this.lastError,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'builtIn',
    required: false,
    includeIfNull: false,
  )


  final bool? builtIn;



      /// Whether the recipe is a bundled classpath recipe or operator-authored (prompt 157)
  @JsonKey(
    
    name: r'origin',
    required: false,
    includeIfNull: false,
  )


  final CorrelationRecipeOriginEnum? origin;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'definition',
    required: true,
    includeIfNull: false,
  )


  final CorrelationRecipeDefinition definition;



      /// Operator overrides; null when there are none
  @JsonKey(
    
    name: r'params',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? params;



      /// e.g. seed kinds missing (recipe loaded disabled with a warning)
  @JsonKey(
    
    name: r'disabledReason',
    required: false,
    includeIfNull: false,
  )


  final String? disabledReason;



  @JsonKey(
    
    name: r'lastRunAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastRunAt;



  @JsonKey(
    
    name: r'lastError',
    required: false,
    includeIfNull: false,
  )


  final String? lastError;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CorrelationRecipe &&
      other.id == id &&
      other.version == version &&
      other.builtIn == builtIn &&
      other.origin == origin &&
      other.enabled == enabled &&
      other.definition == definition &&
      other.params == params &&
      other.disabledReason == disabledReason &&
      other.lastRunAt == lastRunAt &&
      other.lastError == lastError;

    @override
    int get hashCode =>
        id.hashCode +
        version.hashCode +
        builtIn.hashCode +
        origin.hashCode +
        enabled.hashCode +
        definition.hashCode +
        (params == null ? 0 : params.hashCode) +
        (disabledReason == null ? 0 : disabledReason.hashCode) +
        (lastRunAt == null ? 0 : lastRunAt.hashCode) +
        (lastError == null ? 0 : lastError.hashCode);

  factory CorrelationRecipe.fromJson(Map<String, dynamic> json) => _$CorrelationRecipeFromJson(json);

  Map<String, dynamic> toJson() => _$CorrelationRecipeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Whether the recipe is a bundled classpath recipe or operator-authored (prompt 157)
enum CorrelationRecipeOriginEnum {
@JsonValue(r'bundled')
bundled(r'bundled'),
@JsonValue(r'operator')
operator_(r'operator');

const CorrelationRecipeOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


