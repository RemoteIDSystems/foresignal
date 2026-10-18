//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/base_rate_definition_condition.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'base_rate_definition.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BaseRateDefinition {
  /// Returns a new [BaseRateDefinition] instance.
  BaseRateDefinition({

    required  this.family,

     this.regionCodes,

     this.regionClass,

    required  this.window,

     this.condition,
  });

      /// Signal family plus subtype, e.g. `conflict:battles`, `seismic:m5`, `disaster:gdacs-orange+`
  @JsonKey(
    
    name: r'family',
    required: true,
    includeIfNull: false,
  )


  final String family;



      /// Regions (exclusive with `regionClass`)
  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



      /// 040 region class, e.g. a continent or income group (exclusive with `regionCodes`)
  @JsonKey(
    
    name: r'regionClass',
    required: false,
    includeIfNull: false,
  )


  final String? regionClass;



  @JsonKey(
    
    name: r'window',
    required: true,
    includeIfNull: false,
  )


  final BaseRateDefinitionWindowEnum window;



  @JsonKey(
    
    name: r'condition',
    required: false,
    includeIfNull: false,
  )


  final BaseRateDefinitionCondition? condition;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BaseRateDefinition &&
      other.family == family &&
      other.regionCodes == regionCodes &&
      other.regionClass == regionClass &&
      other.window == window &&
      other.condition == condition;

    @override
    int get hashCode =>
        family.hashCode +
        regionCodes.hashCode +
        (regionClass == null ? 0 : regionClass.hashCode) +
        window.hashCode +
        (condition == null ? 0 : condition.hashCode);

  factory BaseRateDefinition.fromJson(Map<String, dynamic> json) => _$BaseRateDefinitionFromJson(json);

  Map<String, dynamic> toJson() => _$BaseRateDefinitionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BaseRateDefinitionWindowEnum {
@JsonValue(r'P7D')
P7D(r'P7D'),
@JsonValue(r'P28D')
P28D(r'P28D'),
@JsonValue(r'P90D')
P90D(r'P90D');

const BaseRateDefinitionWindowEnum(this.value);

final String value;

@override
String toString() => value;
}


