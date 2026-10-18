//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'channel_type_config_fields_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChannelTypeConfigFieldsInner {
  /// Returns a new [ChannelTypeConfigFieldsInner] instance.
  ChannelTypeConfigFieldsInner({

    required  this.name,

    required  this.type,

    required  this.required_,

     this.description,

     this.default_,

     this.allowed,
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


  final ChannelTypeConfigFieldsInnerTypeEnum type;



  @JsonKey(
    
    name: r'required',
    required: true,
    includeIfNull: false,
  )


  final bool required_;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'default',
    required: false,
    includeIfNull: false,
  )


  final Object? default_;



  @JsonKey(
    
    name: r'allowed',
    required: false,
    includeIfNull: false,
  )


  final List<String>? allowed;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChannelTypeConfigFieldsInner &&
      other.name == name &&
      other.type == type &&
      other.required_ == required_ &&
      other.description == description &&
      other.default_ == default_ &&
      other.allowed == allowed;

    @override
    int get hashCode =>
        name.hashCode +
        type.hashCode +
        required_.hashCode +
        description.hashCode +
        (default_ == null ? 0 : default_.hashCode) +
        allowed.hashCode;

  factory ChannelTypeConfigFieldsInner.fromJson(Map<String, dynamic> json) => _$ChannelTypeConfigFieldsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ChannelTypeConfigFieldsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChannelTypeConfigFieldsInnerTypeEnum {
@JsonValue(r'string')
string(r'string'),
@JsonValue(r'url')
url(r'url'),
@JsonValue(r'integer')
integer(r'integer'),
@JsonValue(r'number')
number(r'number'),
@JsonValue(r'boolean')
boolean(r'boolean'),
@JsonValue(r'string-list')
stringList(r'string-list'),
@JsonValue(r'map')
map(r'map'),
@JsonValue(r'enum')
enum_(r'enum');

const ChannelTypeConfigFieldsInnerTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


