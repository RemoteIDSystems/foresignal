//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/kinematic_state.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cpa_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CpaResult {
  /// Returns a new [CpaResult] instance.
  CpaResult({

    required  this.a,

    required  this.b,

    required  this.t0,

    required  this.tcpaSeconds,

    required  this.cpaAt,

    required  this.dcpaMeters,

     this.verticalSeparationM,

     this.horizon,

    required  this.asOf,

    required  this.stale,
  });

  @JsonKey(
    
    name: r'a',
    required: true,
    includeIfNull: false,
  )


  final KinematicState a;



  @JsonKey(
    
    name: r'b',
    required: true,
    includeIfNull: false,
  )


  final KinematicState b;



      /// Common time max(observedAt)
  @JsonKey(
    
    name: r't0',
    required: true,
    includeIfNull: false,
  )


  final DateTime t0;



      /// Clamped to [0, horizon]
  @JsonKey(
    
    name: r'tcpaSeconds',
    required: true,
    includeIfNull: false,
  )


  final num tcpaSeconds;



  @JsonKey(
    
    name: r'cpaAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime cpaAt;



  @JsonKey(
    
    name: r'dcpaMeters',
    required: true,
    includeIfNull: false,
  )


  final num dcpaMeters;



      /// When both states have altitude
  @JsonKey(
    
    name: r'verticalSeparationM',
    required: false,
    includeIfNull: false,
  )


  final num? verticalSeparationM;



      /// Search horizon. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h).
  @JsonKey(
    
    name: r'horizon',
    required: false,
    includeIfNull: false,
  )


  final String? horizon;



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
    bool operator ==(Object other) => identical(this, other) || other is CpaResult &&
      other.a == a &&
      other.b == b &&
      other.t0 == t0 &&
      other.tcpaSeconds == tcpaSeconds &&
      other.cpaAt == cpaAt &&
      other.dcpaMeters == dcpaMeters &&
      other.verticalSeparationM == verticalSeparationM &&
      other.horizon == horizon &&
      other.asOf == asOf &&
      other.stale == stale;

    @override
    int get hashCode =>
        a.hashCode +
        b.hashCode +
        t0.hashCode +
        tcpaSeconds.hashCode +
        cpaAt.hashCode +
        dcpaMeters.hashCode +
        (verticalSeparationM == null ? 0 : verticalSeparationM.hashCode) +
        horizon.hashCode +
        asOf.hashCode +
        stale.hashCode;

  factory CpaResult.fromJson(Map<String, dynamic> json) => _$CpaResultFromJson(json);

  Map<String, dynamic> toJson() => _$CpaResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

