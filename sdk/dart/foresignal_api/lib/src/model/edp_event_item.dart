//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/edp_geometry.dart';
import 'package:foresignal_api/src/model/edp_origin.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_event_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpEventItem {
  /// Returns a new [EdpEventItem] instance.
  EdpEventItem({

    required  this.stream,

    required  this.type,

    required  this.id,

     this.observedAt,

     this.properties,

    required  this.eventTime,

     this.geometry,

     this.upstreamEventId,

     this.origin,
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


  final EdpEventItemTypeEnum type;



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



  @JsonKey(
    
    name: r'eventTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime eventTime;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final EdpGeometry? geometry;



  @JsonKey(
    
    name: r'upstreamEventId',
    required: false,
    includeIfNull: false,
  )


  final String? upstreamEventId;



  @JsonKey(
    
    name: r'origin',
    required: false,
    includeIfNull: false,
  )


  final EdpOrigin? origin;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpEventItem &&
      other.stream == stream &&
      other.type == type &&
      other.id == id &&
      other.observedAt == observedAt &&
      other.properties == properties &&
      other.eventTime == eventTime &&
      other.geometry == geometry &&
      other.upstreamEventId == upstreamEventId &&
      other.origin == origin;

    @override
    int get hashCode =>
        stream.hashCode +
        type.hashCode +
        id.hashCode +
        observedAt.hashCode +
        properties.hashCode +
        eventTime.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        (upstreamEventId == null ? 0 : upstreamEventId.hashCode) +
        (origin == null ? 0 : origin.hashCode);

  factory EdpEventItem.fromJson(Map<String, dynamic> json) => _$EdpEventItemFromJson(json);

  Map<String, dynamic> toJson() => _$EdpEventItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpEventItemTypeEnum {
@JsonValue(r'event')
event(r'event');

const EdpEventItemTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


