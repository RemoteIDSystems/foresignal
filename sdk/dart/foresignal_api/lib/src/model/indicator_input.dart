//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/indicator_likelihood.dart';
import 'package:foresignal_api/src/model/indicator_signal.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'indicator_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IndicatorInput {
  /// Returns a new [IndicatorInput] instance.
  IndicatorInput({

    required  this.description,

    required  this.signal,

    required  this.signalFamily,

    required  this.credibility,

    required  this.relevance,

     this.expectedBy,

    required  this.lr,

     this.absenceLr,
  });

  @JsonKey(
    
    name: r'description',
    required: true,
    includeIfNull: false,
  )


  final String description;



  @JsonKey(
    
    name: r'signal',
    required: true,
    includeIfNull: false,
  )


  final IndicatorSignal signal;



  @JsonKey(
    
    name: r'signalFamily',
    required: true,
    includeIfNull: false,
  )


  final String signalFamily;



  @JsonKey(
    
    name: r'credibility',
    required: true,
    includeIfNull: false,
  )


  final IndicatorInputCredibilityEnum credibility;



  @JsonKey(
    
    name: r'relevance',
    required: true,
    includeIfNull: false,
  )


  final IndicatorInputRelevanceEnum relevance;



  @JsonKey(
    
    name: r'expectedBy',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expectedBy;



      /// Per hypothesis id (or label when created with its set)
  @JsonKey(
    
    name: r'lr',
    required: true,
    includeIfNull: false,
  )


  final Map<String, IndicatorLikelihood> lr;



      /// Optional absence LR overrides per hypothesis id
  @JsonKey(
    
    name: r'absenceLr',
    required: false,
    includeIfNull: false,
  )


  final Map<String, num>? absenceLr;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IndicatorInput &&
      other.description == description &&
      other.signal == signal &&
      other.signalFamily == signalFamily &&
      other.credibility == credibility &&
      other.relevance == relevance &&
      other.expectedBy == expectedBy &&
      other.lr == lr &&
      other.absenceLr == absenceLr;

    @override
    int get hashCode =>
        description.hashCode +
        signal.hashCode +
        signalFamily.hashCode +
        credibility.hashCode +
        relevance.hashCode +
        (expectedBy == null ? 0 : expectedBy.hashCode) +
        lr.hashCode +
        absenceLr.hashCode;

  factory IndicatorInput.fromJson(Map<String, dynamic> json) => _$IndicatorInputFromJson(json);

  Map<String, dynamic> toJson() => _$IndicatorInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IndicatorInputCredibilityEnum {
@JsonValue(r'H')
H(r'H'),
@JsonValue(r'M')
M(r'M'),
@JsonValue(r'L')
L(r'L');

const IndicatorInputCredibilityEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IndicatorInputRelevanceEnum {
@JsonValue(r'H')
H(r'H'),
@JsonValue(r'M')
M(r'M'),
@JsonValue(r'L')
L(r'L');

const IndicatorInputRelevanceEnum(this.value);

final String value;

@override
String toString() => value;
}


