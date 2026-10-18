//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'index_fit_result_components_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IndexFitResultComponentsInner {
  /// Returns a new [IndexFitResultComponentsInner] instance.
  IndexFitResultComponentsInner({

    required  this.key,

    required  this.coefficient,

     this.standardError,

     this.pValue,

     this.significant,

     this.currentWeight,

    required  this.proposedWeight,
  });

  @JsonKey(
    
    name: r'key',
    required: true,
    includeIfNull: false,
  )


  final String key;



  @JsonKey(
    
    name: r'coefficient',
    required: true,
    includeIfNull: false,
  )


  final num coefficient;



  @JsonKey(
    
    name: r'standardError',
    required: false,
    includeIfNull: false,
  )


  final num? standardError;



  @JsonKey(
    
    name: r'pValue',
    required: false,
    includeIfNull: false,
  )


  final num? pValue;



  @JsonKey(
    
    name: r'significant',
    required: false,
    includeIfNull: false,
  )


  final bool? significant;



  @JsonKey(
    
    name: r'currentWeight',
    required: false,
    includeIfNull: false,
  )


  final num? currentWeight;



  @JsonKey(
    
    name: r'proposedWeight',
    required: true,
    includeIfNull: false,
  )


  final num proposedWeight;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IndexFitResultComponentsInner &&
      other.key == key &&
      other.coefficient == coefficient &&
      other.standardError == standardError &&
      other.pValue == pValue &&
      other.significant == significant &&
      other.currentWeight == currentWeight &&
      other.proposedWeight == proposedWeight;

    @override
    int get hashCode =>
        key.hashCode +
        coefficient.hashCode +
        (standardError == null ? 0 : standardError.hashCode) +
        (pValue == null ? 0 : pValue.hashCode) +
        significant.hashCode +
        currentWeight.hashCode +
        proposedWeight.hashCode;

  factory IndexFitResultComponentsInner.fromJson(Map<String, dynamic> json) => _$IndexFitResultComponentsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$IndexFitResultComponentsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

