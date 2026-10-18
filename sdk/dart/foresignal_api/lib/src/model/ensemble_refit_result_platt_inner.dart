//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ensemble_refit_result_platt_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EnsembleRefitResultPlattInner {
  /// Returns a new [EnsembleRefitResultPlattInner] instance.
  EnsembleRefitResultPlattInner({

    required  this.forecasterId,

    required  this.category,

    required  this.a,

    required  this.b,

    required  this.n,
  });

  @JsonKey(
    
    name: r'forecasterId',
    required: true,
    includeIfNull: false,
  )


  final String forecasterId;



  @JsonKey(
    
    name: r'category',
    required: true,
    includeIfNull: false,
  )


  final String category;



  @JsonKey(
    
    name: r'a',
    required: true,
    includeIfNull: false,
  )


  final num a;



  @JsonKey(
    
    name: r'b',
    required: true,
    includeIfNull: false,
  )


  final num b;



  @JsonKey(
    
    name: r'n',
    required: true,
    includeIfNull: false,
  )


  final int n;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EnsembleRefitResultPlattInner &&
      other.forecasterId == forecasterId &&
      other.category == category &&
      other.a == a &&
      other.b == b &&
      other.n == n;

    @override
    int get hashCode =>
        forecasterId.hashCode +
        category.hashCode +
        a.hashCode +
        b.hashCode +
        n.hashCode;

  factory EnsembleRefitResultPlattInner.fromJson(Map<String, dynamic> json) => _$EnsembleRefitResultPlattInnerFromJson(json);

  Map<String, dynamic> toJson() => _$EnsembleRefitResultPlattInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

