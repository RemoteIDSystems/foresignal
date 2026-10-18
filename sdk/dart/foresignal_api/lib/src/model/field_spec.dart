//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'field_spec.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FieldSpec {
  /// Returns a new [FieldSpec] instance.
  FieldSpec({

    required  this.type,

     this.unit,

     this.unitLabel,

     this.label,

     this.description,

     this.datum,

     this.format,

     this.enumLabels,

     this.kinds,

     this.noLink = false,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final FieldSpecTypeEnum type;



      /// UCUM unit code, e.g. `m`, `km`, `[kn_i]`, `[ft_i]`, `deg`, `s`, `1`
  @JsonKey(
    
    name: r'unit',
    required: false,
    includeIfNull: false,
  )


  final String? unit;



      /// Human-readable unit label (emitted as `x-unitLabel` by getKindSchema)
  @JsonKey(
    
    name: r'unitLabel',
    required: false,
    includeIfNull: false,
  )


  final String? unitLabel;



      /// Display label
  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



      /// Vertical datum, for vertical fields only: MSL, WGS84, AGL, PRESSURE or free text
  @JsonKey(
    
    name: r'datum',
    required: false,
    includeIfNull: false,
  )


  final String? datum;



      /// Display format hint (emitted as `x-format` by getKindSchema)
  @JsonKey(
    
    name: r'format',
    required: false,
    includeIfNull: false,
  )


  final String? format;



      /// Allowed values mapped to display labels (emitted as `enum` by getKindSchema)
  @JsonKey(
    
    name: r'enumLabels',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? enumLabels;



      /// Kinds this spec applies to; empty means every kind of the source
  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



      /// Display hint: never render this value as a link (hostile URLs, 076)
  @JsonKey(
    defaultValue: false,
    name: r'noLink',
    required: false,
    includeIfNull: false,
  )


  final bool? noLink;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FieldSpec &&
      other.type == type &&
      other.unit == unit &&
      other.unitLabel == unitLabel &&
      other.label == label &&
      other.description == description &&
      other.datum == datum &&
      other.format == format &&
      other.enumLabels == enumLabels &&
      other.kinds == kinds &&
      other.noLink == noLink;

    @override
    int get hashCode =>
        type.hashCode +
        (unit == null ? 0 : unit.hashCode) +
        (unitLabel == null ? 0 : unitLabel.hashCode) +
        (label == null ? 0 : label.hashCode) +
        (description == null ? 0 : description.hashCode) +
        (datum == null ? 0 : datum.hashCode) +
        (format == null ? 0 : format.hashCode) +
        (enumLabels == null ? 0 : enumLabels.hashCode) +
        kinds.hashCode +
        noLink.hashCode;

  factory FieldSpec.fromJson(Map<String, dynamic> json) => _$FieldSpecFromJson(json);

  Map<String, dynamic> toJson() => _$FieldSpecToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum FieldSpecTypeEnum {
@JsonValue(r'STRING')
STRING(r'STRING'),
@JsonValue(r'INTEGER')
INTEGER(r'INTEGER'),
@JsonValue(r'NUMBER')
NUMBER(r'NUMBER'),
@JsonValue(r'BOOLEAN')
BOOLEAN(r'BOOLEAN'),
@JsonValue(r'TIMESTAMP')
TIMESTAMP(r'TIMESTAMP'),
@JsonValue(r'OBJECT')
OBJECT(r'OBJECT'),
@JsonValue(r'ARRAY')
ARRAY(r'ARRAY');

const FieldSpecTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


