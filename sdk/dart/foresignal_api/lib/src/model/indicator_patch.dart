//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/indicator_likelihood.dart';
import 'package:foresignal_api/src/model/indicator_signal.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'indicator_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IndicatorPatch {
  /// Returns a new [IndicatorPatch] instance.
  IndicatorPatch({

     this.description,

     this.signal,

     this.signalFamily,

     this.credibility,

     this.relevance,

     this.expectedBy,

     this.lr,

     this.absenceLr,

     this.state,

     this.observedEvidence,
  });

  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'signal',
    required: false,
    includeIfNull: false,
  )


  final IndicatorSignal? signal;



  @JsonKey(
    
    name: r'signalFamily',
    required: false,
    includeIfNull: false,
  )


  final String? signalFamily;



  @JsonKey(
    
    name: r'credibility',
    required: false,
    includeIfNull: false,
  )


  final IndicatorPatchCredibilityEnum? credibility;



  @JsonKey(
    
    name: r'relevance',
    required: false,
    includeIfNull: false,
  )


  final IndicatorPatchRelevanceEnum? relevance;



  @JsonKey(
    
    name: r'expectedBy',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expectedBy;



      /// Per hypothesis id (or label when created with its set)
  @JsonKey(
    
    name: r'lr',
    required: false,
    includeIfNull: false,
  )


  final Map<String, IndicatorLikelihood>? lr;



      /// Optional absence LR overrides per hypothesis id
  @JsonKey(
    
    name: r'absenceLr',
    required: false,
    includeIfNull: false,
  )


  final Map<String, num>? absenceLr;



      /// Manual indicators only: record an observation or absence
  @JsonKey(
    
    name: r'state',
    required: false,
    includeIfNull: false,
  )


  final IndicatorPatchStateEnum? state;



  @JsonKey(
    
    name: r'observedEvidence',
    required: false,
    includeIfNull: false,
  )


  final Object? observedEvidence;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IndicatorPatch &&
      other.description == description &&
      other.signal == signal &&
      other.signalFamily == signalFamily &&
      other.credibility == credibility &&
      other.relevance == relevance &&
      other.expectedBy == expectedBy &&
      other.lr == lr &&
      other.absenceLr == absenceLr &&
      other.state == state &&
      other.observedEvidence == observedEvidence;

    @override
    int get hashCode =>
        description.hashCode +
        signal.hashCode +
        signalFamily.hashCode +
        credibility.hashCode +
        relevance.hashCode +
        (expectedBy == null ? 0 : expectedBy.hashCode) +
        lr.hashCode +
        absenceLr.hashCode +
        state.hashCode +
        (observedEvidence == null ? 0 : observedEvidence.hashCode);

  factory IndicatorPatch.fromJson(Map<String, dynamic> json) => _$IndicatorPatchFromJson(json);

  Map<String, dynamic> toJson() => _$IndicatorPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IndicatorPatchCredibilityEnum {
@JsonValue(r'H')
H(r'H'),
@JsonValue(r'M')
M(r'M'),
@JsonValue(r'L')
L(r'L');

const IndicatorPatchCredibilityEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IndicatorPatchRelevanceEnum {
@JsonValue(r'H')
H(r'H'),
@JsonValue(r'M')
M(r'M'),
@JsonValue(r'L')
L(r'L');

const IndicatorPatchRelevanceEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Manual indicators only: record an observation or absence
enum IndicatorPatchStateEnum {
@JsonValue(r'observed')
observed(r'observed'),
@JsonValue(r'absent')
absent(r'absent');

const IndicatorPatchStateEnum(this.value);

final String value;

@override
String toString() => value;
}


