//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/precursor_spatial.dart';
import 'package:foresignal_api/src/model/precursor_window_stats.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/stat_interval.dart';
import 'package:foresignal_api/src/model/precursor_history_inner.dart';
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:foresignal_api/src/model/precursor_lag_distribution.dart';
import 'package:foresignal_api/src/model/precursor_side.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Precursor {
  /// Returns a new [Precursor] instance.
  Precursor({

    required  this.id,

    required  this.kind,

    required  this.antecedent,

    required  this.consequent,

    required  this.spatial,

     this.regionCodes,

    required  this.lagMin,

    required  this.lagMax,

     this.bestLag,

     this.lagDistribution,

     this.hitRate,

     this.hitRateInterval,

     this.baseRate,

     this.lift,

     this.correlation,

     this.correlationSign,

    required  this.nAntecedent,

    required  this.discovery,

     this.validation,

     this.lastRetest,

    required  this.batchId,

    required  this.batchSize,

    required  this.status,

     this.statusReason,

     this.operatorValidated,

    required  this.detector,

     this.firstValidatedAt,

     this.lastTestedAt,

    required  this.origin,

    required  this.description,

    required  this.associationNote,

    required  this.caveats,

     this.history,

    required  this.manifestId,

     this.createdAt,

     this.updatedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final PrecursorKindEnum kind;



  @JsonKey(
    
    name: r'antecedent',
    required: true,
    includeIfNull: false,
  )


  final PrecursorSide antecedent;



  @JsonKey(
    
    name: r'consequent',
    required: true,
    includeIfNull: false,
  )


  final PrecursorSide consequent;



  @JsonKey(
    
    name: r'spatial',
    required: true,
    includeIfNull: false,
  )


  final PrecursorSpatial spatial;



  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



      /// ISO-8601 duration
  @JsonKey(
    
    name: r'lagMin',
    required: true,
    includeIfNull: false,
  )


  final String lagMin;



      /// ISO-8601 duration
  @JsonKey(
    
    name: r'lagMax',
    required: true,
    includeIfNull: false,
  )


  final String lagMax;



      /// ISO-8601 duration
  @JsonKey(
    
    name: r'bestLag',
    required: false,
    includeIfNull: false,
  )


  final String? bestLag;



  @JsonKey(
    
    name: r'lagDistribution',
    required: false,
    includeIfNull: false,
  )


  final PrecursorLagDistribution? lagDistribution;



  @JsonKey(
    
    name: r'hitRate',
    required: false,
    includeIfNull: false,
  )


  final num? hitRate;



  @JsonKey(
    
    name: r'hitRateInterval',
    required: false,
    includeIfNull: false,
  )


  final StatInterval? hitRateInterval;



  @JsonKey(
    
    name: r'baseRate',
    required: false,
    includeIfNull: false,
  )


  final num? baseRate;



  @JsonKey(
    
    name: r'lift',
    required: false,
    includeIfNull: false,
  )


  final num? lift;



  @JsonKey(
    
    name: r'correlation',
    required: false,
    includeIfNull: false,
  )


  final num? correlation;



  @JsonKey(
    
    name: r'correlationSign',
    required: false,
    includeIfNull: false,
  )


  final PrecursorCorrelationSignEnum? correlationSign;



  @JsonKey(
    
    name: r'nAntecedent',
    required: true,
    includeIfNull: false,
  )


  final int nAntecedent;



  @JsonKey(
    
    name: r'discovery',
    required: true,
    includeIfNull: false,
  )


  final PrecursorWindowStats discovery;



  @JsonKey(
    
    name: r'validation',
    required: false,
    includeIfNull: false,
  )


  final PrecursorWindowStats? validation;



      /// Statistics of the latest monthly re-test window (newest third of the range), or null before the first re-test
  @JsonKey(
    
    name: r'lastRetest',
    required: false,
    includeIfNull: false,
  )


  final PrecursorWindowStats? lastRetest;



      /// ULID identifier
  @JsonKey(
    
    name: r'batchId',
    required: true,
    includeIfNull: false,
  )


  final String batchId;



      /// Tests in the FDR batch
  @JsonKey(
    
    name: r'batchSize',
    required: true,
    includeIfNull: false,
  )


  final int batchSize;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final PrecursorStatusEnum status;



  @JsonKey(
    
    name: r'statusReason',
    required: false,
    includeIfNull: false,
  )


  final String? statusReason;



      /// Pinned as validated by an operator (logged and labeled)
  @JsonKey(
    
    name: r'operatorValidated',
    required: false,
    includeIfNull: false,
  )


  final bool? operatorValidated;



  @JsonKey(
    
    name: r'detector',
    required: true,
    includeIfNull: false,
  )


  final DetectorRef detector;



  @JsonKey(
    
    name: r'firstValidatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? firstValidatedAt;



  @JsonKey(
    
    name: r'lastTestedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastTestedAt;



  @JsonKey(
    
    name: r'origin',
    required: true,
    includeIfNull: false,
  )


  final PrecursorOriginEnum origin;



      /// Deterministic plain-language description
  @JsonKey(
    
    name: r'description',
    required: true,
    includeIfNull: false,
  )


  final String description;



  @JsonKey(
    
    name: r'associationNote',
    required: true,
    includeIfNull: false,
  )


  final PrecursorAssociationNoteEnum associationNote;



  @JsonKey(
    
    name: r'caveats',
    required: true,
    includeIfNull: false,
  )


  final List<Caveat> caveats;



      /// Status changes, oldest first (who, when, from, to, why); operator retire/pin actions are logged here
  @JsonKey(
    
    name: r'history',
    required: false,
    includeIfNull: false,
  )


  final List<PrecursorHistoryInner>? history;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: true,
    includeIfNull: false,
  )


  final String manifestId;



  @JsonKey(
    
    name: r'createdAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Precursor &&
      other.id == id &&
      other.kind == kind &&
      other.antecedent == antecedent &&
      other.consequent == consequent &&
      other.spatial == spatial &&
      other.regionCodes == regionCodes &&
      other.lagMin == lagMin &&
      other.lagMax == lagMax &&
      other.bestLag == bestLag &&
      other.lagDistribution == lagDistribution &&
      other.hitRate == hitRate &&
      other.hitRateInterval == hitRateInterval &&
      other.baseRate == baseRate &&
      other.lift == lift &&
      other.correlation == correlation &&
      other.correlationSign == correlationSign &&
      other.nAntecedent == nAntecedent &&
      other.discovery == discovery &&
      other.validation == validation &&
      other.lastRetest == lastRetest &&
      other.batchId == batchId &&
      other.batchSize == batchSize &&
      other.status == status &&
      other.statusReason == statusReason &&
      other.operatorValidated == operatorValidated &&
      other.detector == detector &&
      other.firstValidatedAt == firstValidatedAt &&
      other.lastTestedAt == lastTestedAt &&
      other.origin == origin &&
      other.description == description &&
      other.associationNote == associationNote &&
      other.caveats == caveats &&
      other.history == history &&
      other.manifestId == manifestId &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        kind.hashCode +
        antecedent.hashCode +
        consequent.hashCode +
        spatial.hashCode +
        regionCodes.hashCode +
        lagMin.hashCode +
        lagMax.hashCode +
        (bestLag == null ? 0 : bestLag.hashCode) +
        (lagDistribution == null ? 0 : lagDistribution.hashCode) +
        (hitRate == null ? 0 : hitRate.hashCode) +
        (hitRateInterval == null ? 0 : hitRateInterval.hashCode) +
        (baseRate == null ? 0 : baseRate.hashCode) +
        (lift == null ? 0 : lift.hashCode) +
        (correlation == null ? 0 : correlation.hashCode) +
        (correlationSign == null ? 0 : correlationSign.hashCode) +
        nAntecedent.hashCode +
        discovery.hashCode +
        (validation == null ? 0 : validation.hashCode) +
        (lastRetest == null ? 0 : lastRetest.hashCode) +
        batchId.hashCode +
        batchSize.hashCode +
        status.hashCode +
        (statusReason == null ? 0 : statusReason.hashCode) +
        operatorValidated.hashCode +
        detector.hashCode +
        (firstValidatedAt == null ? 0 : firstValidatedAt.hashCode) +
        (lastTestedAt == null ? 0 : lastTestedAt.hashCode) +
        origin.hashCode +
        description.hashCode +
        associationNote.hashCode +
        caveats.hashCode +
        history.hashCode +
        manifestId.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory Precursor.fromJson(Map<String, dynamic> json) => _$PrecursorFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecursorKindEnum {
@JsonValue(r'event')
event(r'event'),
@JsonValue(r'series')
series(r'series');

const PrecursorKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PrecursorCorrelationSignEnum {
@JsonValue(r'positive')
positive(r'positive'),
@JsonValue(r'negative')
negative(r'negative');

const PrecursorCorrelationSignEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PrecursorStatusEnum {
@JsonValue(r'candidate')
candidate(r'candidate'),
@JsonValue(r'validated')
validated(r'validated'),
@JsonValue(r'stale')
stale(r'stale'),
@JsonValue(r'retired')
retired(r'retired');

const PrecursorStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PrecursorOriginEnum {
@JsonValue(r'auto')
auto(r'auto'),
@JsonValue(r'operator')
operator_(r'operator');

const PrecursorOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PrecursorAssociationNoteEnum {
@JsonValue(r'association in stored data; not causation')
associationInStoredDataSemicolonNotCausation(r'association in stored data; not causation');

const PrecursorAssociationNoteEnum(this.value);

final String value;

@override
String toString() => value;
}


