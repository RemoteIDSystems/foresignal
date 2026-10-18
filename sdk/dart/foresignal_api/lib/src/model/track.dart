//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/track_properties.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'track.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Track {
  /// Returns a new [Track] instance.
  Track({

    required  this.type,

    required  this.geometry,

    required  this.properties,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final TrackTypeEnum type;



      /// MultiLineString; segments split at gaps, tombstones, versions outside the caller's restriction and the antimeridian; a lone point is repeated; null when the window holds no position
  @JsonKey(
    
    name: r'geometry',
    required: true,
    includeIfNull: true,
  )


  final Geometry? geometry;



  @JsonKey(
    
    name: r'properties',
    required: true,
    includeIfNull: false,
  )


  final TrackProperties properties;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Track &&
      other.type == type &&
      other.geometry == geometry &&
      other.properties == properties;

    @override
    int get hashCode =>
        type.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        properties.hashCode;

  factory Track.fromJson(Map<String, dynamic> json) => _$TrackFromJson(json);

  Map<String, dynamic> toJson() => _$TrackToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TrackTypeEnum {
@JsonValue(r'Feature')
feature(r'Feature');

const TrackTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


