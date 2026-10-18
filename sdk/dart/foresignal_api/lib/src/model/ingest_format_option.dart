//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_format_option.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestFormatOption {
  /// Returns a new [IngestFormatOption] instance.
  IngestFormatOption({

    required  this.name,

    required  this.type,

     this.required_,

     this.default_,

     this.description,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final IngestFormatOptionTypeEnum type;



  @JsonKey(
    
    name: r'required',
    required: false,
    includeIfNull: false,
  )


  final bool? required_;



  @JsonKey(
    
    name: r'default',
    required: false,
    includeIfNull: false,
  )


  final Object? default_;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestFormatOption &&
      other.name == name &&
      other.type == type &&
      other.required_ == required_ &&
      other.default_ == default_ &&
      other.description == description;

    @override
    int get hashCode =>
        name.hashCode +
        type.hashCode +
        required_.hashCode +
        (default_ == null ? 0 : default_.hashCode) +
        description.hashCode;

  factory IngestFormatOption.fromJson(Map<String, dynamic> json) => _$IngestFormatOptionFromJson(json);

  Map<String, dynamic> toJson() => _$IngestFormatOptionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IngestFormatOptionTypeEnum {
@JsonValue(r'string')
string(r'string'),
@JsonValue(r'integer')
integer(r'integer'),
@JsonValue(r'number')
number(r'number'),
@JsonValue(r'boolean')
boolean(r'boolean'),
@JsonValue(r'object')
object(r'object'),
@JsonValue(r'array')
array(r'array');

const IngestFormatOptionTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


