//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_test_result_multiple_testing.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorTestResultMultipleTesting {
  /// Returns a new [PrecursorTestResultMultipleTesting] instance.
  PrecursorTestResultMultipleTesting({

     this.testsLast30d,

     this.bonferroniPValue,
  });

  @JsonKey(
    
    name: r'testsLast30d',
    required: false,
    includeIfNull: false,
  )


  final int? testsLast30d;



  @JsonKey(
    
    name: r'bonferroniPValue',
    required: false,
    includeIfNull: false,
  )


  final num? bonferroniPValue;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorTestResultMultipleTesting &&
      other.testsLast30d == testsLast30d &&
      other.bonferroniPValue == bonferroniPValue;

    @override
    int get hashCode =>
        testsLast30d.hashCode +
        (bonferroniPValue == null ? 0 : bonferroniPValue.hashCode);

  factory PrecursorTestResultMultipleTesting.fromJson(Map<String, dynamic> json) => _$PrecursorTestResultMultipleTestingFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorTestResultMultipleTestingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

