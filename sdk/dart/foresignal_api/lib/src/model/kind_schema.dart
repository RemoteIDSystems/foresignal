//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/kind_display.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'kind_schema.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class KindSchema {
  /// Returns a new [KindSchema] instance.
  KindSchema({

     this.dollarSchema,

     this.title,

     this.type,

     this.properties,

     this.xDisplay,

     this.xConflicts,
  });

  @JsonKey(
    
    name: r'$schema',
    required: false,
    includeIfNull: false,
  )


  final String? dollarSchema;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'type',
    required: false,
    includeIfNull: false,
  )


  final String? type;



  @JsonKey(
    
    name: r'properties',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? properties;



  @JsonKey(
    
    name: r'x-display',
    required: false,
    includeIfNull: false,
  )


  final KindDisplay? xDisplay;



      /// Source ids whose fieldSpecs disagree on a field's type
  @JsonKey(
    
    name: r'x-conflicts',
    required: false,
    includeIfNull: false,
  )


  final List<String>? xConflicts;





    @override
    bool operator ==(Object other) => identical(this, other) || other is KindSchema &&
      other.dollarSchema == dollarSchema &&
      other.title == title &&
      other.type == type &&
      other.properties == properties &&
      other.xDisplay == xDisplay &&
      other.xConflicts == xConflicts;

    @override
    int get hashCode =>
        dollarSchema.hashCode +
        title.hashCode +
        type.hashCode +
        properties.hashCode +
        (xDisplay == null ? 0 : xDisplay.hashCode) +
        xConflicts.hashCode;

  factory KindSchema.fromJson(Map<String, dynamic> json) => _$KindSchemaFromJson(json);

  Map<String, dynamic> toJson() => _$KindSchemaToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

