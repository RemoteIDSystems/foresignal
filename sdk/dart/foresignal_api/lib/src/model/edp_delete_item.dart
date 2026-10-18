//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_delete_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpDeleteItem {
  /// Returns a new [EdpDeleteItem] instance.
  EdpDeleteItem({

    required  this.stream,

    required  this.type,

    required  this.id,

     this.observedAt,

     this.properties,
  });

  @JsonKey(
    
    name: r'stream',
    required: true,
    includeIfNull: false,
  )


  final String stream;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final EdpDeleteItemTypeEnum type;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'observedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? observedAt;



      /// Item properties, validated against the stream's payloadSchema
  @JsonKey(
    
    name: r'properties',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? properties;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpDeleteItem &&
      other.stream == stream &&
      other.type == type &&
      other.id == id &&
      other.observedAt == observedAt &&
      other.properties == properties;

    @override
    int get hashCode =>
        stream.hashCode +
        type.hashCode +
        id.hashCode +
        observedAt.hashCode +
        properties.hashCode;

  factory EdpDeleteItem.fromJson(Map<String, dynamic> json) => _$EdpDeleteItemFromJson(json);

  Map<String, dynamic> toJson() => _$EdpDeleteItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpDeleteItemTypeEnum {
@JsonValue(r'delete')
delete(r'delete');

const EdpDeleteItemTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


