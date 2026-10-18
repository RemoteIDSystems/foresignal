//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/edp_geometry.dart';
import 'package:foresignal_api/src/model/edp_origin.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_feature_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpFeatureItem {
  /// Returns a new [EdpFeatureItem] instance.
  EdpFeatureItem({

    required  this.stream,

    required  this.type,

    required  this.id,

     this.observedAt,

     this.properties,

     this.geometry,

     this.eventTime,

     this.validUntil,

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


  final EdpFeatureItemTypeEnum type;



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
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final EdpGeometry? geometry;



  @JsonKey(
    
    name: r'eventTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? eventTime;



  @JsonKey(
    
    name: r'validUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validUntil;



  @JsonKey(
    
    name: r'origin',
    required: false,
    includeIfNull: false,
  )


  final EdpOrigin? origin;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpFeatureItem &&
      other.stream == stream &&
      other.type == type &&
      other.id == id &&
      other.observedAt == observedAt &&
      other.properties == properties &&
      other.geometry == geometry &&
      other.eventTime == eventTime &&
      other.validUntil == validUntil &&
      other.origin == origin;

    @override
    int get hashCode =>
        stream.hashCode +
        type.hashCode +
        id.hashCode +
        observedAt.hashCode +
        properties.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        (eventTime == null ? 0 : eventTime.hashCode) +
        (validUntil == null ? 0 : validUntil.hashCode) +
        (origin == null ? 0 : origin.hashCode);

  factory EdpFeatureItem.fromJson(Map<String, dynamic> json) => _$EdpFeatureItemFromJson(json);

  Map<String, dynamic> toJson() => _$EdpFeatureItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpFeatureItemTypeEnum {
@JsonValue(r'feature')
feature(r'feature');

const EdpFeatureItemTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


