//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/intel_calibration_bins_inner.dart';
import 'package:foresignal_api/src/model/intel_calibration_curve_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_calibration.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelCalibration {
  /// Returns a new [IntelCalibration] instance.
  IntelCalibration({

    required  this.scope,

    required  this.calibrated,

    required  this.sampleN,

    required  this.minSamples,

     this.brier,

     this.bins,

     this.curve,

     this.fittedAt,

     this.updatedAt,
  });

  @JsonKey(
    
    name: r'scope',
    required: true,
    includeIfNull: false,
  )


  final IntelCalibrationScopeEnum scope;



      /// True once at least `minSamples` outcomes have resolved; until then the raw score is reported unchanged
  @JsonKey(
    
    name: r'calibrated',
    required: true,
    includeIfNull: false,
  )


  final bool calibrated;



      /// Resolved outcomes behind the fit
  @JsonKey(
    
    name: r'sampleN',
    required: true,
    includeIfNull: false,
  )


  final int sampleN;



      /// Outcomes needed before a scope is calibrated
  @JsonKey(
    
    name: r'minSamples',
    required: true,
    includeIfNull: false,
  )


  final int minSamples;



      /// Mean Brier score of the raw scores against the outcomes
  @JsonKey(
    
    name: r'brier',
    required: false,
    includeIfNull: false,
  )


  final num? brier;



      /// The 10-bin reliability table
  @JsonKey(
    
    name: r'bins',
    required: false,
    includeIfNull: false,
  )


  final List<IntelCalibrationBinsInner>? bins;



      /// The CORP (PAV) recalibration curve, one point per distinct raw score
  @JsonKey(
    
    name: r'curve',
    required: false,
    includeIfNull: false,
  )


  final List<IntelCalibrationCurveInner>? curve;



  @JsonKey(
    
    name: r'fittedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? fittedAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelCalibration &&
      other.scope == scope &&
      other.calibrated == calibrated &&
      other.sampleN == sampleN &&
      other.minSamples == minSamples &&
      other.brier == brier &&
      other.bins == bins &&
      other.curve == curve &&
      other.fittedAt == fittedAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        scope.hashCode +
        calibrated.hashCode +
        sampleN.hashCode +
        minSamples.hashCode +
        (brier == null ? 0 : brier.hashCode) +
        bins.hashCode +
        curve.hashCode +
        (fittedAt == null ? 0 : fittedAt.hashCode) +
        (updatedAt == null ? 0 : updatedAt.hashCode);

  factory IntelCalibration.fromJson(Map<String, dynamic> json) => _$IntelCalibrationFromJson(json);

  Map<String, dynamic> toJson() => _$IntelCalibrationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IntelCalibrationScopeEnum {
@JsonValue(r'intel-event')
intelEvent(r'intel-event'),
@JsonValue(r'precursor-active')
precursorActive(r'precursor-active');

const IntelCalibrationScopeEnum(this.value);

final String value;

@override
String toString() => value;
}


