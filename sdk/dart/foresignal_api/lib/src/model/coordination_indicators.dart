//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:foresignal_api/src/model/coordination_indicator.dart';
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'coordination_indicators.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CoordinationIndicators {
  /// Returns a new [CoordinationIndicators] instance.
  CoordinationIndicators({

    required  this.narrativeId,

    required  this.window,

    required  this.synchrony,

    required  this.textDuplication,

    required  this.accountAgeSkew,

    required  this.coRepostBursts,

    required  this.significantCount,

     this.findingId,

     this.statement,

    required  this.detector,

    required  this.computedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'narrativeId',
    required: true,
    includeIfNull: false,
  )


  final String narrativeId;



  @JsonKey(
    
    name: r'window',
    required: true,
    includeIfNull: false,
  )


  final TimeRange window;



  @JsonKey(
    
    name: r'synchrony',
    required: true,
    includeIfNull: false,
  )


  final CoordinationIndicator synchrony;



  @JsonKey(
    
    name: r'textDuplication',
    required: true,
    includeIfNull: false,
  )


  final CoordinationIndicator textDuplication;



  @JsonKey(
    
    name: r'accountAgeSkew',
    required: true,
    includeIfNull: false,
  )


  final CoordinationIndicator accountAgeSkew;



  @JsonKey(
    
    name: r'coRepostBursts',
    required: true,
    includeIfNull: false,
  )


  final CoordinationIndicator coRepostBursts;



      /// Indicators with q < 0.01
          // minimum: 0
          // maximum: 4
  @JsonKey(
    
    name: r'significantCount',
    required: true,
    includeIfNull: false,
  )


  final int significantCount;



      /// `social-coordination` finding when ≥ 2 indicators are significant
  @JsonKey(
    
    name: r'findingId',
    required: false,
    includeIfNull: false,
  )


  final String? findingId;



  @JsonKey(
    
    name: r'statement',
    required: false,
    includeIfNull: false,
  )


  final String? statement;



  @JsonKey(
    
    name: r'detector',
    required: true,
    includeIfNull: false,
  )


  final DetectorRef detector;



  @JsonKey(
    
    name: r'computedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime computedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CoordinationIndicators &&
      other.narrativeId == narrativeId &&
      other.window == window &&
      other.synchrony == synchrony &&
      other.textDuplication == textDuplication &&
      other.accountAgeSkew == accountAgeSkew &&
      other.coRepostBursts == coRepostBursts &&
      other.significantCount == significantCount &&
      other.findingId == findingId &&
      other.statement == statement &&
      other.detector == detector &&
      other.computedAt == computedAt;

    @override
    int get hashCode =>
        narrativeId.hashCode +
        window.hashCode +
        synchrony.hashCode +
        textDuplication.hashCode +
        accountAgeSkew.hashCode +
        coRepostBursts.hashCode +
        significantCount.hashCode +
        (findingId == null ? 0 : findingId.hashCode) +
        (statement == null ? 0 : statement.hashCode) +
        detector.hashCode +
        computedAt.hashCode;

  factory CoordinationIndicators.fromJson(Map<String, dynamic> json) => _$CoordinationIndicatorsFromJson(json);

  Map<String, dynamic> toJson() => _$CoordinationIndicatorsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

