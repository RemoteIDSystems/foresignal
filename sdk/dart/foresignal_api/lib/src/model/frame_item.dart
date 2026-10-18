//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'frame_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FrameItem {
  /// Returns a new [FrameItem] instance.
  FrameItem({

    required  this.id,

    required  this.kind,

     this.title,

    required  this.observedAt,

    required  this.version,

     this.lon,

     this.lat,

     this.geometry,

     this.props,
  });

      /// <sourceId>:<externalId>
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final String kind;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'observedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime observedAt;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'lon',
    required: false,
    includeIfNull: false,
  )


  final num? lon;



  @JsonKey(
    
    name: r'lat',
    required: false,
    includeIfNull: false,
  )


  final num? lat;



      /// Only with geometry=full
  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



      /// tileProperties of the version
  @JsonKey(
    
    name: r'props',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? props;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FrameItem &&
      other.id == id &&
      other.kind == kind &&
      other.title == title &&
      other.observedAt == observedAt &&
      other.version == version &&
      other.lon == lon &&
      other.lat == lat &&
      other.geometry == geometry &&
      other.props == props;

    @override
    int get hashCode =>
        id.hashCode +
        kind.hashCode +
        (title == null ? 0 : title.hashCode) +
        observedAt.hashCode +
        version.hashCode +
        (lon == null ? 0 : lon.hashCode) +
        (lat == null ? 0 : lat.hashCode) +
        (geometry == null ? 0 : geometry.hashCode) +
        props.hashCode;

  factory FrameItem.fromJson(Map<String, dynamic> json) => _$FrameItemFromJson(json);

  Map<String, dynamic> toJson() => _$FrameItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

