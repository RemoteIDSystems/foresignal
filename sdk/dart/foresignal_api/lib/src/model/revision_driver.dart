//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'revision_driver.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RevisionDriver {
  /// Returns a new [RevisionDriver] instance.
  RevisionDriver({

    required  this.kind,

     this.id,

     this.ref,

    required  this.hypothesisId,

     this.signalFamily,

     this.lr,

     this.lnLr,

     this.pDetect,

    required  this.applied,

    required  this.suppressed,

     this.suppressionReason,
  });

  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final RevisionDriverKindEnum kind;



      /// Indicator, evidence or base-rate id
  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



      /// ULID identifier
  @JsonKey(
    
    name: r'hypothesisId',
    required: true,
    includeIfNull: false,
  )


  final String hypothesisId;



  @JsonKey(
    
    name: r'signalFamily',
    required: false,
    includeIfNull: false,
  )


  final String? signalFamily;



  @JsonKey(
    
    name: r'lr',
    required: false,
    includeIfNull: false,
  )


  final num? lr;



  @JsonKey(
    
    name: r'lnLr',
    required: false,
    includeIfNull: false,
  )


  final num? lnLr;



  @JsonKey(
    
    name: r'pDetect',
    required: false,
    includeIfNull: false,
  )


  final num? pDetect;



      /// Log-odds change applied after suppression and damping
  @JsonKey(
    
    name: r'applied',
    required: true,
    includeIfNull: false,
  )


  final num applied;



  @JsonKey(
    
    name: r'suppressed',
    required: true,
    includeIfNull: false,
  )


  final bool suppressed;



  @JsonKey(
    
    name: r'suppressionReason',
    required: false,
    includeIfNull: false,
  )


  final RevisionDriverSuppressionReasonEnum? suppressionReason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RevisionDriver &&
      other.kind == kind &&
      other.id == id &&
      other.ref == ref &&
      other.hypothesisId == hypothesisId &&
      other.signalFamily == signalFamily &&
      other.lr == lr &&
      other.lnLr == lnLr &&
      other.pDetect == pDetect &&
      other.applied == applied &&
      other.suppressed == suppressed &&
      other.suppressionReason == suppressionReason;

    @override
    int get hashCode =>
        kind.hashCode +
        (id == null ? 0 : id.hashCode) +
        (ref == null ? 0 : ref.hashCode) +
        hypothesisId.hashCode +
        (signalFamily == null ? 0 : signalFamily.hashCode) +
        (lr == null ? 0 : lr.hashCode) +
        (lnLr == null ? 0 : lnLr.hashCode) +
        (pDetect == null ? 0 : pDetect.hashCode) +
        applied.hashCode +
        suppressed.hashCode +
        (suppressionReason == null ? 0 : suppressionReason.hashCode);

  factory RevisionDriver.fromJson(Map<String, dynamic> json) => _$RevisionDriverFromJson(json);

  Map<String, dynamic> toJson() => _$RevisionDriverToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RevisionDriverKindEnum {
@JsonValue(r'indicator')
indicator(r'indicator'),
@JsonValue(r'evidence')
evidence(r'evidence'),
@JsonValue(r'base-rate')
baseRate(r'base-rate'),
@JsonValue(r'review')
review(r'review'),
@JsonValue(r'operator')
operator_(r'operator');

const RevisionDriverKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum RevisionDriverSuppressionReasonEnum {
@JsonValue(r'same-evidence-group')
sameEvidenceGroup(r'same-evidence-group'),
@JsonValue(r'same-family-day')
sameFamilyDay(r'same-family-day'),
@JsonValue(r'damped')
damped(r'damped'),
@JsonValue(r'capped')
capped(r'capped');

const RevisionDriverSuppressionReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


