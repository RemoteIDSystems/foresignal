//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exposure_area.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExposureArea {
  /// Returns a new [ExposureArea] instance.
  ExposureArea({

    required  this.source_,

     this.placeId,

     this.eventId,

     this.geometry,

     this.areaKm2,
  });

  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final ExposureAreaSource_Enum source_;



  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



      /// ULID identifier
  @JsonKey(
    
    name: r'eventId',
    required: false,
    includeIfNull: false,
  )


  final String? eventId;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



  @JsonKey(
    
    name: r'areaKm2',
    required: false,
    includeIfNull: false,
  )


  final num? areaKm2;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExposureArea &&
      other.source_ == source_ &&
      other.placeId == placeId &&
      other.eventId == eventId &&
      other.geometry == geometry &&
      other.areaKm2 == areaKm2;

    @override
    int get hashCode =>
        source_.hashCode +
        (placeId == null ? 0 : placeId.hashCode) +
        (eventId == null ? 0 : eventId.hashCode) +
        (geometry == null ? 0 : geometry.hashCode) +
        areaKm2.hashCode;

  factory ExposureArea.fromJson(Map<String, dynamic> json) => _$ExposureAreaFromJson(json);

  Map<String, dynamic> toJson() => _$ExposureAreaToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExposureAreaSource_Enum {
@JsonValue(r'geometry')
geometry(r'geometry'),
@JsonValue(r'place')
place(r'place'),
@JsonValue(r'event')
event(r'event');

const ExposureAreaSource_Enum(this.value);

final String value;

@override
String toString() => value;
}


