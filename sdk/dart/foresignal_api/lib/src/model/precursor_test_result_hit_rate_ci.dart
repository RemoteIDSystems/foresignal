//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_test_result_hit_rate_ci.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorTestResultHitRateCi {
  /// Returns a new [PrecursorTestResultHitRateCi] instance.
  PrecursorTestResultHitRateCi({

     this.lower,

     this.upper,

     this.method,
  });

  @JsonKey(
    
    name: r'lower',
    required: false,
    includeIfNull: false,
  )


  final num? lower;



  @JsonKey(
    
    name: r'upper',
    required: false,
    includeIfNull: false,
  )


  final num? upper;



  @JsonKey(
    
    name: r'method',
    required: false,
    includeIfNull: false,
  )


  final PrecursorTestResultHitRateCiMethodEnum? method;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorTestResultHitRateCi &&
      other.lower == lower &&
      other.upper == upper &&
      other.method == method;

    @override
    int get hashCode =>
        lower.hashCode +
        upper.hashCode +
        method.hashCode;

  factory PrecursorTestResultHitRateCi.fromJson(Map<String, dynamic> json) => _$PrecursorTestResultHitRateCiFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorTestResultHitRateCiToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecursorTestResultHitRateCiMethodEnum {
@JsonValue(r'wilson-95')
wilson95(r'wilson-95');

const PrecursorTestResultHitRateCiMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


