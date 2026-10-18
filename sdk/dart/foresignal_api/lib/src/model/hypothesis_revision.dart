//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/hypothesis_probability_state.dart';
import 'package:foresignal_api/src/model/revision_driver.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_revision.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisRevision {
  /// Returns a new [HypothesisRevision] instance.
  HypothesisRevision({

    required  this.id,

    required  this.setId,

    required  this.at,

    required  this.cause,

    required  this.actor,

    required  this.asOfSeq,

    required  this.before,

    required  this.after,

    required  this.drivers,

     this.citations,

    required  this.rationale,

     this.rationaleOrigin,

     this.bandChanged,

     this.overrideValue,

     this.ledgerForecastId,

    required  this.updaterVersion,
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
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'cause',
    required: true,
    includeIfNull: false,
  )


  final HypothesisRevisionCauseEnum cause;



      /// Principal, `indicator-bayes` or component id
  @JsonKey(
    
    name: r'actor',
    required: true,
    includeIfNull: false,
  )


  final String actor;



  @JsonKey(
    
    name: r'asOfSeq',
    required: true,
    includeIfNull: false,
  )


  final int asOfSeq;



  @JsonKey(
    
    name: r'before',
    required: true,
    includeIfNull: false,
  )


  final List<HypothesisProbabilityState> before;



  @JsonKey(
    
    name: r'after',
    required: true,
    includeIfNull: false,
  )


  final List<HypothesisProbabilityState> after;



  @JsonKey(
    
    name: r'drivers',
    required: true,
    includeIfNull: false,
  )


  final List<RevisionDriver> drivers;



  @JsonKey(
    
    name: r'citations',
    required: false,
    includeIfNull: false,
  )


  final List<String>? citations;



  @JsonKey(
    
    name: r'rationale',
    required: true,
    includeIfNull: false,
  )


  final String rationale;



  @JsonKey(
    
    name: r'rationaleOrigin',
    required: false,
    includeIfNull: false,
  )


  final HypothesisRevisionRationaleOriginEnum? rationaleOrigin;



  @JsonKey(
    
    name: r'bandChanged',
    required: false,
    includeIfNull: false,
  )


  final bool? bandChanged;



      /// Includes a logged operator override
  @JsonKey(
    
    name: r'override',
    required: false,
    includeIfNull: false,
  )


  final bool? overrideValue;



      /// ULID identifier
  @JsonKey(
    
    name: r'ledgerForecastId',
    required: false,
    includeIfNull: false,
  )


  final String? ledgerForecastId;



  @JsonKey(
    
    name: r'updaterVersion',
    required: true,
    includeIfNull: false,
  )


  final String updaterVersion;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisRevision &&
      other.id == id &&
      other.setId == setId &&
      other.at == at &&
      other.cause == cause &&
      other.actor == actor &&
      other.asOfSeq == asOfSeq &&
      other.before == before &&
      other.after == after &&
      other.drivers == drivers &&
      other.citations == citations &&
      other.rationale == rationale &&
      other.rationaleOrigin == rationaleOrigin &&
      other.bandChanged == bandChanged &&
      other.overrideValue == overrideValue &&
      other.ledgerForecastId == ledgerForecastId &&
      other.updaterVersion == updaterVersion;

    @override
    int get hashCode =>
        id.hashCode +
        setId.hashCode +
        at.hashCode +
        cause.hashCode +
        actor.hashCode +
        asOfSeq.hashCode +
        before.hashCode +
        after.hashCode +
        drivers.hashCode +
        citations.hashCode +
        rationale.hashCode +
        rationaleOrigin.hashCode +
        bandChanged.hashCode +
        overrideValue.hashCode +
        (ledgerForecastId == null ? 0 : ledgerForecastId.hashCode) +
        updaterVersion.hashCode;

  factory HypothesisRevision.fromJson(Map<String, dynamic> json) => _$HypothesisRevisionFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisRevisionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum HypothesisRevisionCauseEnum {
@JsonValue(r'indicator-observed')
indicatorObserved(r'indicator-observed'),
@JsonValue(r'indicator-absent')
indicatorAbsent(r'indicator-absent'),
@JsonValue(r'evidence-added')
evidenceAdded(r'evidence-added'),
@JsonValue(r'evidence-removed')
evidenceRemoved(r'evidence-removed'),
@JsonValue(r'base-rate-refresh')
baseRateRefresh(r'base-rate-refresh'),
@JsonValue(r'review')
review(r'review'),
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'created')
created(r'created');

const HypothesisRevisionCauseEnum(this.value);

final String value;

@override
String toString() => value;
}


enum HypothesisRevisionRationaleOriginEnum {
@JsonValue(r'deterministic')
deterministic(r'deterministic'),
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'analyst')
analyst(r'analyst');

const HypothesisRevisionRationaleOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


