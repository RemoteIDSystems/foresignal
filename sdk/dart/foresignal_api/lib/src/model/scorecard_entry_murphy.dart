//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'scorecard_entry_murphy.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ScorecardEntryMurphy {
  /// Returns a new [ScorecardEntryMurphy] instance.
  ScorecardEntryMurphy({

    required  this.reliability,

    required  this.resolution,

    required  this.uncertainty,

     this.withinBinVariance,

     this.withinBinCovariance,
  });

  @JsonKey(
    
    name: r'reliability',
    required: true,
    includeIfNull: false,
  )


  final num reliability;



  @JsonKey(
    
    name: r'resolution',
    required: true,
    includeIfNull: false,
  )


  final num resolution;



  @JsonKey(
    
    name: r'uncertainty',
    required: true,
    includeIfNull: false,
  )


  final num uncertainty;



  @JsonKey(
    
    name: r'withinBinVariance',
    required: false,
    includeIfNull: false,
  )


  final num? withinBinVariance;



  @JsonKey(
    
    name: r'withinBinCovariance',
    required: false,
    includeIfNull: false,
  )


  final num? withinBinCovariance;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ScorecardEntryMurphy &&
      other.reliability == reliability &&
      other.resolution == resolution &&
      other.uncertainty == uncertainty &&
      other.withinBinVariance == withinBinVariance &&
      other.withinBinCovariance == withinBinCovariance;

    @override
    int get hashCode =>
        reliability.hashCode +
        resolution.hashCode +
        uncertainty.hashCode +
        withinBinVariance.hashCode +
        withinBinCovariance.hashCode;

  factory ScorecardEntryMurphy.fromJson(Map<String, dynamic> json) => _$ScorecardEntryMurphyFromJson(json);

  Map<String, dynamic> toJson() => _$ScorecardEntryMurphyToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

