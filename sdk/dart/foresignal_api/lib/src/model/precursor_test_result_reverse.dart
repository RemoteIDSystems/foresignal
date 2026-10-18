//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_test_result_reverse.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorTestResultReverse {
  /// Returns a new [PrecursorTestResultReverse] instance.
  PrecursorTestResultReverse({

     this.hits,

     this.hitRate,

     this.lift,

     this.pValue,
  });

  @JsonKey(
    
    name: r'hits',
    required: false,
    includeIfNull: false,
  )


  final int? hits;



  @JsonKey(
    
    name: r'hitRate',
    required: false,
    includeIfNull: false,
  )


  final num? hitRate;



  @JsonKey(
    
    name: r'lift',
    required: false,
    includeIfNull: false,
  )


  final num? lift;



  @JsonKey(
    
    name: r'pValue',
    required: false,
    includeIfNull: false,
  )


  final num? pValue;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorTestResultReverse &&
      other.hits == hits &&
      other.hitRate == hitRate &&
      other.lift == lift &&
      other.pValue == pValue;

    @override
    int get hashCode =>
        hits.hashCode +
        (hitRate == null ? 0 : hitRate.hashCode) +
        (lift == null ? 0 : lift.hashCode) +
        (pValue == null ? 0 : pValue.hashCode);

  factory PrecursorTestResultReverse.fromJson(Map<String, dynamic> json) => _$PrecursorTestResultReverseFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorTestResultReverseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

