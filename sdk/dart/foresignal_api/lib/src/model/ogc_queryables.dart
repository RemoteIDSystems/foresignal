//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_queryables.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcQueryables {
  /// Returns a new [OgcQueryables] instance.
  OgcQueryables({

     this.dollarSchema,

     this.dollarId,

    required  this.type,

     this.title,

    required  this.properties,

     this.additionalProperties,
  });

  @JsonKey(
    
    name: r'$schema',
    required: false,
    includeIfNull: false,
  )


  final String? dollarSchema;



  @JsonKey(
    
    name: r'$id',
    required: false,
    includeIfNull: false,
  )


  final String? dollarId;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final OgcQueryablesTypeEnum type;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'properties',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Map<String, Object>> properties;



  @JsonKey(
    
    name: r'additionalProperties',
    required: false,
    includeIfNull: false,
  )


  final bool? additionalProperties;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcQueryables &&
      other.dollarSchema == dollarSchema &&
      other.dollarId == dollarId &&
      other.type == type &&
      other.title == title &&
      other.properties == properties &&
      other.additionalProperties == additionalProperties;

    @override
    int get hashCode =>
        dollarSchema.hashCode +
        dollarId.hashCode +
        type.hashCode +
        title.hashCode +
        properties.hashCode +
        additionalProperties.hashCode;

  factory OgcQueryables.fromJson(Map<String, dynamic> json) => _$OgcQueryablesFromJson(json);

  Map<String, dynamic> toJson() => _$OgcQueryablesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OgcQueryablesTypeEnum {
@JsonValue(r'object')
object(r'object');

const OgcQueryablesTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


