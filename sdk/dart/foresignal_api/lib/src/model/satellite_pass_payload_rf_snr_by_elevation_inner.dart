//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'satellite_pass_payload_rf_snr_by_elevation_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SatellitePassPayloadRfSnrByElevationInner {
  /// Returns a new [SatellitePassPayloadRfSnrByElevationInner] instance.
  SatellitePassPayloadRfSnrByElevationInner({

    required  this.elevationDeg,

    required  this.snrDb,
  });

  @JsonKey(
    
    name: r'elevationDeg',
    required: true,
    includeIfNull: false,
  )


  final num elevationDeg;



  @JsonKey(
    
    name: r'snrDb',
    required: true,
    includeIfNull: false,
  )


  final num snrDb;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SatellitePassPayloadRfSnrByElevationInner &&
      other.elevationDeg == elevationDeg &&
      other.snrDb == snrDb;

    @override
    int get hashCode =>
        elevationDeg.hashCode +
        snrDb.hashCode;

  factory SatellitePassPayloadRfSnrByElevationInner.fromJson(Map<String, dynamic> json) => _$SatellitePassPayloadRfSnrByElevationInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SatellitePassPayloadRfSnrByElevationInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

