//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/indicator_likelihood.dart';
import 'package:foresignal_api/src/model/indicator_signal.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'indicator.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Indicator {
  /// Returns a new [Indicator] instance.
  Indicator({

    required  this.id,

    required  this.setId,

    required  this.description,

    required  this.signal,

    required  this.signalFamily,

    required  this.credibility,

    required  this.relevance,

     this.expectedBy,

    required  this.lr,

     this.absenceLr,

    required  this.state,

     this.watchRuleId,

     this.pDetect,

     this.observedEvidence,

     this.lastStateChangeAt,

    required  this.origin,

     this.version,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// ULID identifier
  @JsonKey(
    
    name: r'setId',
    required: true,
    includeIfNull: false,
  )


  final String setId;



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



      /// 119 signal-family registry id
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


  final IndicatorCredibilityEnum credibility;



  @JsonKey(
    
    name: r'relevance',
    required: true,
    includeIfNull: false,
  )


  final IndicatorRelevanceEnum relevance;



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



  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final IndicatorStateEnum state;



      /// 017 trigger rule tagged `indicator`, `origin-hypothesis` and `ind-{id in lower case}` (prompt 125 names `indicator:{id}` and `origin:hypothesis`, but a rule tag cannot hold a colon); it routes to no channel unless a channel names it in `ruleIds`; disabled once the indicator is observed, absent or retired
  @JsonKey(
    
    name: r'watchRuleId',
    required: false,
    includeIfNull: false,
  )


  final String? watchRuleId;



      /// 116 detection probability for the family, area and window
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'pDetect',
    required: false,
    includeIfNull: false,
  )


  final num? pDetect;



  @JsonKey(
    
    name: r'observedEvidence',
    required: false,
    includeIfNull: false,
  )


  final Object? observedEvidence;



  @JsonKey(
    
    name: r'lastStateChangeAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastStateChangeAt;



  @JsonKey(
    
    name: r'origin',
    required: true,
    includeIfNull: false,
  )


  final IndicatorOriginEnum origin;



  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final int? version;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Indicator &&
      other.id == id &&
      other.setId == setId &&
      other.description == description &&
      other.signal == signal &&
      other.signalFamily == signalFamily &&
      other.credibility == credibility &&
      other.relevance == relevance &&
      other.expectedBy == expectedBy &&
      other.lr == lr &&
      other.absenceLr == absenceLr &&
      other.state == state &&
      other.watchRuleId == watchRuleId &&
      other.pDetect == pDetect &&
      other.observedEvidence == observedEvidence &&
      other.lastStateChangeAt == lastStateChangeAt &&
      other.origin == origin &&
      other.version == version;

    @override
    int get hashCode =>
        id.hashCode +
        setId.hashCode +
        description.hashCode +
        signal.hashCode +
        signalFamily.hashCode +
        credibility.hashCode +
        relevance.hashCode +
        (expectedBy == null ? 0 : expectedBy.hashCode) +
        lr.hashCode +
        absenceLr.hashCode +
        state.hashCode +
        (watchRuleId == null ? 0 : watchRuleId.hashCode) +
        (pDetect == null ? 0 : pDetect.hashCode) +
        (observedEvidence == null ? 0 : observedEvidence.hashCode) +
        (lastStateChangeAt == null ? 0 : lastStateChangeAt.hashCode) +
        origin.hashCode +
        version.hashCode;

  factory Indicator.fromJson(Map<String, dynamic> json) => _$IndicatorFromJson(json);

  Map<String, dynamic> toJson() => _$IndicatorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IndicatorCredibilityEnum {
@JsonValue(r'H')
H(r'H'),
@JsonValue(r'M')
M(r'M'),
@JsonValue(r'L')
L(r'L');

const IndicatorCredibilityEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IndicatorRelevanceEnum {
@JsonValue(r'H')
H(r'H'),
@JsonValue(r'M')
M(r'M'),
@JsonValue(r'L')
L(r'L');

const IndicatorRelevanceEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IndicatorStateEnum {
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'emerging')
emerging(r'emerging'),
@JsonValue(r'observed')
observed(r'observed'),
@JsonValue(r'absent')
absent(r'absent'),
@JsonValue(r'unknown-coverage')
unknownCoverage(r'unknown-coverage'),
@JsonValue(r'retired')
retired(r'retired'),
@JsonValue(r'reported')
reported(r'reported');

const IndicatorStateEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IndicatorOriginEnum {
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'analyst')
analyst(r'analyst');

const IndicatorOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


