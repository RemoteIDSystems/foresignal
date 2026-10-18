//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/intel_board_latest_assessments_inner.dart';
import 'package:foresignal_api/src/model/intel_board_calibration.dart';
import 'package:foresignal_api/src/model/intel_board_benchmarks_inner.dart';
import 'package:foresignal_api/src/model/intel_board_indicators_observed_inner.dart';
import 'package:foresignal_api/src/model/intel_board_band_changes_inner.dart';
import 'package:foresignal_api/src/model/intel_board_situations_inner.dart';
import 'package:foresignal_api/src/model/intel_board_forecasts_due_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_board.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelBoard {
  /// Returns a new [IntelBoard] instance.
  IntelBoard({

    required  this.generatedAt,

    required  this.situations,

    required  this.bandChanges,

    required  this.indicatorsObserved,

    required  this.forecastsDue,

    required  this.latestAssessments,

     this.calibration,

     this.benchmarks,

     this.truncated,
  });

  @JsonKey(
    
    name: r'generatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime generatedAt;



  @JsonKey(
    
    name: r'situations',
    required: true,
    includeIfNull: false,
  )


  final List<IntelBoardSituationsInner> situations;



  @JsonKey(
    
    name: r'bandChanges',
    required: true,
    includeIfNull: false,
  )


  final List<IntelBoardBandChangesInner> bandChanges;



  @JsonKey(
    
    name: r'indicatorsObserved',
    required: true,
    includeIfNull: false,
  )


  final List<IntelBoardIndicatorsObservedInner> indicatorsObserved;



  @JsonKey(
    
    name: r'forecastsDue',
    required: true,
    includeIfNull: false,
  )


  final List<IntelBoardForecastsDueInner> forecastsDue;



  @JsonKey(
    
    name: r'latestAssessments',
    required: true,
    includeIfNull: false,
  )


  final List<IntelBoardLatestAssessmentsInner> latestAssessments;



  @JsonKey(
    
    name: r'calibration',
    required: false,
    includeIfNull: false,
  )


  final IntelBoardCalibration? calibration;



  @JsonKey(
    
    name: r'benchmarks',
    required: false,
    includeIfNull: false,
  )


  final List<IntelBoardBenchmarksInner>? benchmarks;



      /// Sections truncated to stay within 8 KB
  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final List<String>? truncated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelBoard &&
      other.generatedAt == generatedAt &&
      other.situations == situations &&
      other.bandChanges == bandChanges &&
      other.indicatorsObserved == indicatorsObserved &&
      other.forecastsDue == forecastsDue &&
      other.latestAssessments == latestAssessments &&
      other.calibration == calibration &&
      other.benchmarks == benchmarks &&
      other.truncated == truncated;

    @override
    int get hashCode =>
        generatedAt.hashCode +
        situations.hashCode +
        bandChanges.hashCode +
        indicatorsObserved.hashCode +
        forecastsDue.hashCode +
        latestAssessments.hashCode +
        calibration.hashCode +
        benchmarks.hashCode +
        truncated.hashCode;

  factory IntelBoard.fromJson(Map<String, dynamic> json) => _$IntelBoardFromJson(json);

  Map<String, dynamic> toJson() => _$IntelBoardToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

