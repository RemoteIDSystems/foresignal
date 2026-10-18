//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'lead_time_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LeadTimeSummary {
  /// Returns a new [LeadTimeSummary] instance.
  LeadTimeSummary({

    required  this.n,

     this.median,

     this.p25,

     this.p75,
  });

          // minimum: 0
  @JsonKey(
    
    name: r'n',
    required: true,
    includeIfNull: false,
  )


  final int n;



  @JsonKey(
    
    name: r'median',
    required: false,
    includeIfNull: false,
  )


  final num? median;



  @JsonKey(
    
    name: r'p25',
    required: false,
    includeIfNull: false,
  )


  final num? p25;



  @JsonKey(
    
    name: r'p75',
    required: false,
    includeIfNull: false,
  )


  final num? p75;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LeadTimeSummary &&
      other.n == n &&
      other.median == median &&
      other.p25 == p25 &&
      other.p75 == p75;

    @override
    int get hashCode =>
        n.hashCode +
        (median == null ? 0 : median.hashCode) +
        (p25 == null ? 0 : p25.hashCode) +
        (p75 == null ? 0 : p75.hashCode);

  factory LeadTimeSummary.fromJson(Map<String, dynamic> json) => _$LeadTimeSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$LeadTimeSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

