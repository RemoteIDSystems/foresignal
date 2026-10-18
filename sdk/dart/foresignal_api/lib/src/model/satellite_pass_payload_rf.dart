//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/satellite_pass_payload_rf_snr_by_elevation_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'satellite_pass_payload_rf.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SatellitePassPayloadRf {
  /// Returns a new [SatellitePassPayloadRf] instance.
  SatellitePassPayloadRf({

     this.snrMaxDb,

     this.snrMeanDb,

     this.berMin,

     this.berMean,

     this.syncedFraction,

     this.firstLockAt,

     this.lastLockAt,

     this.firstLockElevationDeg,

     this.lastLockElevationDeg,

     this.snrByElevation,
  });

  @JsonKey(
    
    name: r'snrMaxDb',
    required: false,
    includeIfNull: false,
  )


  final num? snrMaxDb;



  @JsonKey(
    
    name: r'snrMeanDb',
    required: false,
    includeIfNull: false,
  )


  final num? snrMeanDb;



  @JsonKey(
    
    name: r'berMin',
    required: false,
    includeIfNull: false,
  )


  final num? berMin;



  @JsonKey(
    
    name: r'berMean',
    required: false,
    includeIfNull: false,
  )


  final num? berMean;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'syncedFraction',
    required: false,
    includeIfNull: false,
  )


  final num? syncedFraction;



  @JsonKey(
    
    name: r'firstLockAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? firstLockAt;



  @JsonKey(
    
    name: r'lastLockAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastLockAt;



  @JsonKey(
    
    name: r'firstLockElevationDeg',
    required: false,
    includeIfNull: false,
  )


  final num? firstLockElevationDeg;



  @JsonKey(
    
    name: r'lastLockElevationDeg',
    required: false,
    includeIfNull: false,
  )


  final num? lastLockElevationDeg;



      /// About 60 points
  @JsonKey(
    
    name: r'snrByElevation',
    required: false,
    includeIfNull: false,
  )


  final List<SatellitePassPayloadRfSnrByElevationInner>? snrByElevation;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SatellitePassPayloadRf &&
      other.snrMaxDb == snrMaxDb &&
      other.snrMeanDb == snrMeanDb &&
      other.berMin == berMin &&
      other.berMean == berMean &&
      other.syncedFraction == syncedFraction &&
      other.firstLockAt == firstLockAt &&
      other.lastLockAt == lastLockAt &&
      other.firstLockElevationDeg == firstLockElevationDeg &&
      other.lastLockElevationDeg == lastLockElevationDeg &&
      other.snrByElevation == snrByElevation;

    @override
    int get hashCode =>
        snrMaxDb.hashCode +
        snrMeanDb.hashCode +
        berMin.hashCode +
        berMean.hashCode +
        syncedFraction.hashCode +
        (firstLockAt == null ? 0 : firstLockAt.hashCode) +
        (lastLockAt == null ? 0 : lastLockAt.hashCode) +
        firstLockElevationDeg.hashCode +
        lastLockElevationDeg.hashCode +
        snrByElevation.hashCode;

  factory SatellitePassPayloadRf.fromJson(Map<String, dynamic> json) => _$SatellitePassPayloadRfFromJson(json);

  Map<String, dynamic> toJson() => _$SatellitePassPayloadRfToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

