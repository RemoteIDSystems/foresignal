//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/mapping_field_unit.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_field.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingField {
  /// Returns a new [MappingField] instance.
  MappingField({

    required  this.name,

    required  this.path,

    required  this.type,

     this.unit,

     this.scale,

     this.offset,

     this.default_,

     this.sentinels,
  });

      /// Top-level payload key
  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// JSONPath, or `$pairs.<key>`
  @JsonKey(
    
    name: r'path',
    required: true,
    includeIfNull: false,
  )


  final String path;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final MappingFieldTypeEnum type;



  @JsonKey(
    
    name: r'unit',
    required: false,
    includeIfNull: false,
  )


  final MappingFieldUnit? unit;



  @JsonKey(
    
    name: r'scale',
    required: false,
    includeIfNull: false,
  )


  final num? scale;



  @JsonKey(
    
    name: r'offset',
    required: false,
    includeIfNull: false,
  )


  final num? offset;



      /// Value used when the path is absent or holds a sentinel
  @JsonKey(
    
    name: r'default',
    required: false,
    includeIfNull: false,
  )


  final Object? default_;



      /// Input values treated as absent (e.g. AIS heading 511), compared before conversion
  @JsonKey(
    
    name: r'sentinels',
    required: false,
    includeIfNull: false,
  )


  final List<Object>? sentinels;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingField &&
      other.name == name &&
      other.path == path &&
      other.type == type &&
      other.unit == unit &&
      other.scale == scale &&
      other.offset == offset &&
      other.default_ == default_ &&
      other.sentinels == sentinels;

    @override
    int get hashCode =>
        name.hashCode +
        path.hashCode +
        type.hashCode +
        unit.hashCode +
        scale.hashCode +
        offset.hashCode +
        (default_ == null ? 0 : default_.hashCode) +
        sentinels.hashCode;

  factory MappingField.fromJson(Map<String, dynamic> json) => _$MappingFieldFromJson(json);

  Map<String, dynamic> toJson() => _$MappingFieldToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MappingFieldTypeEnum {
@JsonValue(r'number')
number(r'number'),
@JsonValue(r'integer')
integer(r'integer'),
@JsonValue(r'string')
string(r'string'),
@JsonValue(r'boolean')
boolean(r'boolean');

const MappingFieldTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


