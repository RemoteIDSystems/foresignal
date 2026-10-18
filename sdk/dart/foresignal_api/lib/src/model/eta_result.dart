//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/eta_result_target.dart';
import 'package:foresignal_api/src/model/kinematic_state.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'eta_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EtaResult {
  /// Returns a new [EtaResult] instance.
  EtaResult({

    required  this.record,

    required  this.target,

    required  this.distanceMeters,

     this.bearingDeg,

     this.bearingDiffDeg,

    required  this.approaching,

     this.etaSeconds,

     this.etaAt,

    required  this.asOf,

    required  this.stale,
  });

  @JsonKey(
    
    name: r'record',
    required: true,
    includeIfNull: false,
  )


  final KinematicState record;



  @JsonKey(
    
    name: r'target',
    required: true,
    includeIfNull: false,
  )


  final EtaResultTarget target;



  @JsonKey(
    
    name: r'distanceMeters',
    required: true,
    includeIfNull: false,
  )


  final num distanceMeters;



  @JsonKey(
    
    name: r'bearingDeg',
    required: false,
    includeIfNull: false,
  )


  final num? bearingDeg;



  @JsonKey(
    
    name: r'bearingDiffDeg',
    required: false,
    includeIfNull: false,
  )


  final num? bearingDiffDeg;



      /// bearingDiff ≤ 45° and speed ≥ 0.5 m/s
  @JsonKey(
    
    name: r'approaching',
    required: true,
    includeIfNull: false,
  )


  final bool approaching;



  @JsonKey(
    
    name: r'etaSeconds',
    required: false,
    includeIfNull: false,
  )


  final num? etaSeconds;



  @JsonKey(
    
    name: r'etaAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? etaAt;



  @JsonKey(
    
    name: r'asOf',
    required: true,
    includeIfNull: false,
  )


  final DateTime asOf;



  @JsonKey(
    
    name: r'stale',
    required: true,
    includeIfNull: false,
  )


  final bool stale;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EtaResult &&
      other.record == record &&
      other.target == target &&
      other.distanceMeters == distanceMeters &&
      other.bearingDeg == bearingDeg &&
      other.bearingDiffDeg == bearingDiffDeg &&
      other.approaching == approaching &&
      other.etaSeconds == etaSeconds &&
      other.etaAt == etaAt &&
      other.asOf == asOf &&
      other.stale == stale;

    @override
    int get hashCode =>
        record.hashCode +
        target.hashCode +
        distanceMeters.hashCode +
        bearingDeg.hashCode +
        bearingDiffDeg.hashCode +
        approaching.hashCode +
        (etaSeconds == null ? 0 : etaSeconds.hashCode) +
        (etaAt == null ? 0 : etaAt.hashCode) +
        asOf.hashCode +
        stale.hashCode;

  factory EtaResult.fromJson(Map<String, dynamic> json) => _$EtaResultFromJson(json);

  Map<String, dynamic> toJson() => _$EtaResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

