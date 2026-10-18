//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'coordination_indicator.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CoordinationIndicator {
  /// Returns a new [CoordinationIndicator] instance.
  CoordinationIndicator({

    required  this.value,

    required  this.nullExpectation,

    required  this.pValue,

     this.qValue,

     this.n,

    required  this.available,
  });

  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: true,
  )


  final num? value;



  @JsonKey(
    
    name: r'nullExpectation',
    required: true,
    includeIfNull: true,
  )


  final num? nullExpectation;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'pValue',
    required: true,
    includeIfNull: true,
  )


  final num? pValue;



      /// Benjamini–Hochberg adjusted across the day's narratives
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'qValue',
    required: false,
    includeIfNull: false,
  )


  final num? qValue;



          // minimum: 0
  @JsonKey(
    
    name: r'n',
    required: false,
    includeIfNull: false,
  )


  final int? n;



      /// False when the platform does not provide the data (e.g. account creation dates)
  @JsonKey(
    
    name: r'available',
    required: true,
    includeIfNull: false,
  )


  final bool available;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CoordinationIndicator &&
      other.value == value &&
      other.nullExpectation == nullExpectation &&
      other.pValue == pValue &&
      other.qValue == qValue &&
      other.n == n &&
      other.available == available;

    @override
    int get hashCode =>
        (value == null ? 0 : value.hashCode) +
        (nullExpectation == null ? 0 : nullExpectation.hashCode) +
        (pValue == null ? 0 : pValue.hashCode) +
        (qValue == null ? 0 : qValue.hashCode) +
        n.hashCode +
        available.hashCode;

  factory CoordinationIndicator.fromJson(Map<String, dynamic> json) => _$CoordinationIndicatorFromJson(json);

  Map<String, dynamic> toJson() => _$CoordinationIndicatorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

