//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'indicator_likelihood.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IndicatorLikelihood {
  /// Returns a new [IndicatorLikelihood] instance.
  IndicatorLikelihood({

    required  this.class_,

    required  this.direction,

     this.value,

     this.rationale,

     this.effective,
  });

      /// Diagnosticity class: high 5.0/0.2, medium 2.5/0.4, low 1.4/0.7, neutral 1.0
  @JsonKey(
    
    name: r'class',
    required: true,
    includeIfNull: false,
  )


  final IndicatorLikelihoodClass_Enum class_;



  @JsonKey(
    
    name: r'direction',
    required: true,
    includeIfNull: false,
  )


  final IndicatorLikelihoodDirectionEnum direction;



      /// Explicit LR overriding the class value; requires a rationale
          // minimum: 0.05
          // maximum: 20
  @JsonKey(
    
    name: r'value',
    required: false,
    includeIfNull: false,
  )


  final num? value;



  @JsonKey(
    
    name: r'rationale',
    required: false,
    includeIfNull: false,
  )


  final String? rationale;



      /// LR the updater applies
  @JsonKey(
    
    name: r'effective',
    required: false,
    includeIfNull: false,
  )


  final num? effective;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IndicatorLikelihood &&
      other.class_ == class_ &&
      other.direction == direction &&
      other.value == value &&
      other.rationale == rationale &&
      other.effective == effective;

    @override
    int get hashCode =>
        class_.hashCode +
        direction.hashCode +
        (value == null ? 0 : value.hashCode) +
        (rationale == null ? 0 : rationale.hashCode) +
        effective.hashCode;

  factory IndicatorLikelihood.fromJson(Map<String, dynamic> json) => _$IndicatorLikelihoodFromJson(json);

  Map<String, dynamic> toJson() => _$IndicatorLikelihoodToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Diagnosticity class: high 5.0/0.2, medium 2.5/0.4, low 1.4/0.7, neutral 1.0
enum IndicatorLikelihoodClass_Enum {
@JsonValue(r'high')
high(r'high'),
@JsonValue(r'medium')
medium(r'medium'),
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'neutral')
neutral(r'neutral');

const IndicatorLikelihoodClass_Enum(this.value);

final String value;

@override
String toString() => value;
}


enum IndicatorLikelihoodDirectionEnum {
@JsonValue(r'supports')
supports(r'supports'),
@JsonValue(r'weakens')
weakens(r'weakens');

const IndicatorLikelihoodDirectionEnum(this.value);

final String value;

@override
String toString() => value;
}


