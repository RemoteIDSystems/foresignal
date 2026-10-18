//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/confidence_level.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'observability_estimate.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ObservabilityEstimate {
  /// Returns a new [ObservabilityEstimate] instance.
  ObservabilityEstimate({

    required  this.sourceId,

    required  this.family,

     this.regionCode,

     this.cell,

     this.band,

    required  this.periodStart,

    required  this.periodEnd,

    required  this.pDetect,

    required  this.lo,

    required  this.hi,

    required  this.n,

    required  this.method,

     this.confidence,

    required  this.computedAt,

     this.manifestId,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'family',
    required: true,
    includeIfNull: false,
  )


  final String family;



  @JsonKey(
    
    name: r'regionCode',
    required: false,
    includeIfNull: false,
  )


  final String? regionCode;



      /// H3 res-4 cell index
  @JsonKey(
    
    name: r'cell',
    required: false,
    includeIfNull: false,
  )


  final String? cell;



      /// Threshold bucket, e.g. M4.5-5
  @JsonKey(
    
    name: r'band',
    required: false,
    includeIfNull: false,
  )


  final String? band;



  @JsonKey(
    
    name: r'periodStart',
    required: true,
    includeIfNull: false,
  )


  final DateTime periodStart;



  @JsonKey(
    
    name: r'periodEnd',
    required: true,
    includeIfNull: false,
  )


  final DateTime periodEnd;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'pDetect',
    required: true,
    includeIfNull: false,
  )


  final num pDetect;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'lo',
    required: true,
    includeIfNull: false,
  )


  final num lo;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'hi',
    required: true,
    includeIfNull: false,
  )


  final num hi;



  @JsonKey(
    
    name: r'n',
    required: true,
    includeIfNull: false,
  )


  final int n;



  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final ObservabilityEstimateMethodEnum method;



  @JsonKey(
    
    name: r'confidence',
    required: false,
    includeIfNull: false,
  )


  final ConfidenceLevel? confidence;



  @JsonKey(
    
    name: r'computedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime computedAt;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ObservabilityEstimate &&
      other.sourceId == sourceId &&
      other.family == family &&
      other.regionCode == regionCode &&
      other.cell == cell &&
      other.band == band &&
      other.periodStart == periodStart &&
      other.periodEnd == periodEnd &&
      other.pDetect == pDetect &&
      other.lo == lo &&
      other.hi == hi &&
      other.n == n &&
      other.method == method &&
      other.confidence == confidence &&
      other.computedAt == computedAt &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        sourceId.hashCode +
        family.hashCode +
        (regionCode == null ? 0 : regionCode.hashCode) +
        (cell == null ? 0 : cell.hashCode) +
        (band == null ? 0 : band.hashCode) +
        periodStart.hashCode +
        periodEnd.hashCode +
        pDetect.hashCode +
        lo.hashCode +
        hi.hashCode +
        n.hashCode +
        method.hashCode +
        confidence.hashCode +
        computedAt.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode);

  factory ObservabilityEstimate.fromJson(Map<String, dynamic> json) => _$ObservabilityEstimateFromJson(json);

  Map<String, dynamic> toJson() => _$ObservabilityEstimateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ObservabilityEstimateMethodEnum {
@JsonValue(r'capture-recapture')
captureRecapture(r'capture-recapture'),
@JsonValue(r'presence-proxy')
presenceProxy(r'presence-proxy'),
@JsonValue(r'receiver-range')
receiverRange(r'receiver-range'),
@JsonValue(r'declared')
declared(r'declared'),
@JsonValue(r'platform-volume')
platformVolume(r'platform-volume');

const ObservabilityEstimateMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


