//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_test_result_lag.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorTestResultLag {
  /// Returns a new [PrecursorTestResultLag] instance.
  PrecursorTestResultLag({

     this.min,

     this.p25,

     this.median,

     this.p75,

     this.max,

     this.mean,
  });

  @JsonKey(
    
    name: r'min',
    required: false,
    includeIfNull: false,
  )


  final String? min;



  @JsonKey(
    
    name: r'p25',
    required: false,
    includeIfNull: false,
  )


  final String? p25;



  @JsonKey(
    
    name: r'median',
    required: false,
    includeIfNull: false,
  )


  final String? median;



  @JsonKey(
    
    name: r'p75',
    required: false,
    includeIfNull: false,
  )


  final String? p75;



  @JsonKey(
    
    name: r'max',
    required: false,
    includeIfNull: false,
  )


  final String? max;



  @JsonKey(
    
    name: r'mean',
    required: false,
    includeIfNull: false,
  )


  final String? mean;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorTestResultLag &&
      other.min == min &&
      other.p25 == p25 &&
      other.median == median &&
      other.p75 == p75 &&
      other.max == max &&
      other.mean == mean;

    @override
    int get hashCode =>
        (min == null ? 0 : min.hashCode) +
        (p25 == null ? 0 : p25.hashCode) +
        (median == null ? 0 : median.hashCode) +
        (p75 == null ? 0 : p75.hashCode) +
        (max == null ? 0 : max.hashCode) +
        (mean == null ? 0 : mean.hashCode);

  factory PrecursorTestResultLag.fromJson(Map<String, dynamic> json) => _$PrecursorTestResultLagFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorTestResultLagToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

