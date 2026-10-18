//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'smoke_polygon_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SmokePolygonPayload {
  /// Returns a new [SmokePolygonPayload] instance.
  SmokePolygonPayload({

    required  this.density,

    required  this.densityRank,

     this.start,

     this.end,

     this.satellite,

    required  this.label,
  });

  @JsonKey(
    
    name: r'density',
    required: true,
    includeIfNull: false,
  )


  final SmokePolygonPayloadDensityEnum density;



          // minimum: 1
          // maximum: 3
  @JsonKey(
    
    name: r'densityRank',
    required: true,
    includeIfNull: false,
  )


  final int densityRank;



  @JsonKey(
    
    name: r'start',
    required: false,
    includeIfNull: false,
  )


  final DateTime? start;



  @JsonKey(
    
    name: r'end',
    required: false,
    includeIfNull: false,
  )


  final DateTime? end;



  @JsonKey(
    
    name: r'satellite',
    required: false,
    includeIfNull: false,
  )


  final String? satellite;



  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final String label;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SmokePolygonPayload &&
      other.density == density &&
      other.densityRank == densityRank &&
      other.start == start &&
      other.end == end &&
      other.satellite == satellite &&
      other.label == label;

    @override
    int get hashCode =>
        density.hashCode +
        densityRank.hashCode +
        start.hashCode +
        end.hashCode +
        satellite.hashCode +
        label.hashCode;

  factory SmokePolygonPayload.fromJson(Map<String, dynamic> json) => _$SmokePolygonPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$SmokePolygonPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SmokePolygonPayloadDensityEnum {
@JsonValue(r'light')
light(r'light'),
@JsonValue(r'medium')
medium(r'medium'),
@JsonValue(r'heavy')
heavy(r'heavy');

const SmokePolygonPayloadDensityEnum(this.value);

final String value;

@override
String toString() => value;
}


