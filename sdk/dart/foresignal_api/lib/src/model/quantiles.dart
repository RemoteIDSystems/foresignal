//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'quantiles.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Quantiles {
  /// Returns a new [Quantiles] instance.
  Quantiles({

    required  this.p05,

     this.p10,

    required  this.p25,

    required  this.p50,

    required  this.p75,

     this.p90,

    required  this.p95,
  });

  @JsonKey(
    
    name: r'p05',
    required: true,
    includeIfNull: false,
  )


  final num p05;



  @JsonKey(
    
    name: r'p10',
    required: false,
    includeIfNull: false,
  )


  final num? p10;



  @JsonKey(
    
    name: r'p25',
    required: true,
    includeIfNull: false,
  )


  final num p25;



  @JsonKey(
    
    name: r'p50',
    required: true,
    includeIfNull: false,
  )


  final num p50;



  @JsonKey(
    
    name: r'p75',
    required: true,
    includeIfNull: false,
  )


  final num p75;



  @JsonKey(
    
    name: r'p90',
    required: false,
    includeIfNull: false,
  )


  final num? p90;



  @JsonKey(
    
    name: r'p95',
    required: true,
    includeIfNull: false,
  )


  final num p95;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Quantiles &&
      other.p05 == p05 &&
      other.p10 == p10 &&
      other.p25 == p25 &&
      other.p50 == p50 &&
      other.p75 == p75 &&
      other.p90 == p90 &&
      other.p95 == p95;

    @override
    int get hashCode =>
        p05.hashCode +
        p10.hashCode +
        p25.hashCode +
        p50.hashCode +
        p75.hashCode +
        p90.hashCode +
        p95.hashCode;

  factory Quantiles.fromJson(Map<String, dynamic> json) => _$QuantilesFromJson(json);

  Map<String, dynamic> toJson() => _$QuantilesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

