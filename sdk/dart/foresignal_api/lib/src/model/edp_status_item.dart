//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_status_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpStatusItem {
  /// Returns a new [EdpStatusItem] instance.
  EdpStatusItem({

     this.stream,

    required  this.type,

    required  this.health,

     this.message,
  });

  @JsonKey(
    
    name: r'stream',
    required: false,
    includeIfNull: false,
  )


  final String? stream;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final EdpStatusItemTypeEnum type;



  @JsonKey(
    
    name: r'health',
    required: true,
    includeIfNull: false,
  )


  final EdpStatusItemHealthEnum health;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpStatusItem &&
      other.stream == stream &&
      other.type == type &&
      other.health == health &&
      other.message == message;

    @override
    int get hashCode =>
        stream.hashCode +
        type.hashCode +
        health.hashCode +
        (message == null ? 0 : message.hashCode);

  factory EdpStatusItem.fromJson(Map<String, dynamic> json) => _$EdpStatusItemFromJson(json);

  Map<String, dynamic> toJson() => _$EdpStatusItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpStatusItemTypeEnum {
@JsonValue(r'status')
status(r'status');

const EdpStatusItemTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EdpStatusItemHealthEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'degraded')
degraded(r'degraded'),
@JsonValue(r'blocked')
blocked(r'blocked'),
@JsonValue(r'layout-changed')
layoutChanged(r'layout-changed'),
@JsonValue(r'geo-restricted')
geoRestricted(r'geo-restricted');

const EdpStatusItemHealthEnum(this.value);

final String value;

@override
String toString() => value;
}


