//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_lag_distribution.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorLagDistribution {
  /// Returns a new [PrecursorLagDistribution] instance.
  PrecursorLagDistribution({

     this.p25,

     this.median,

     this.p75,
  });

      /// ISO-8601 duration
  @JsonKey(
    
    name: r'p25',
    required: false,
    includeIfNull: false,
  )


  final String? p25;



      /// ISO-8601 duration
  @JsonKey(
    
    name: r'median',
    required: false,
    includeIfNull: false,
  )


  final String? median;



      /// ISO-8601 duration
  @JsonKey(
    
    name: r'p75',
    required: false,
    includeIfNull: false,
  )


  final String? p75;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorLagDistribution &&
      other.p25 == p25 &&
      other.median == median &&
      other.p75 == p75;

    @override
    int get hashCode =>
        p25.hashCode +
        median.hashCode +
        p75.hashCode;

  factory PrecursorLagDistribution.fromJson(Map<String, dynamic> json) => _$PrecursorLagDistributionFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorLagDistributionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

