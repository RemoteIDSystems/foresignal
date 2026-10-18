//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/confidence_level.dart';
import 'package:foresignal_api/src/model/observability_estimate.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'detectability.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Detectability {
  /// Returns a new [Detectability] instance.
  Detectability({

    required  this.family,

     this.regionCode,

     this.cell,

     this.band,

    required  this.at,

    required  this.p,

    required  this.lo,

    required  this.hi,

    required  this.methods,

    required  this.sources,

     this.confidence,

    required  this.absenceOk,

     this.reason,

     this.estimates,
  });

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



  @JsonKey(
    
    name: r'cell',
    required: false,
    includeIfNull: false,
  )


  final String? cell;



  @JsonKey(
    
    name: r'band',
    required: false,
    includeIfNull: false,
  )


  final String? band;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'p',
    required: true,
    includeIfNull: false,
  )


  final num p;



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
    
    name: r'methods',
    required: true,
    includeIfNull: false,
  )


  final List<DetectabilityMethodsEnum> methods;



  @JsonKey(
    
    name: r'sources',
    required: true,
    includeIfNull: false,
  )


  final List<String> sources;



  @JsonKey(
    
    name: r'confidence',
    required: false,
    includeIfNull: false,
  )


  final ConfidenceLevel? confidence;



      /// Healthy sources and p ≥ absenceMinDetect: an absence here may be read as 'nothing happened'
  @JsonKey(
    
    name: r'absenceOk',
    required: true,
    includeIfNull: false,
  )


  final bool absenceOk;



      /// When absenceOk is false, e.g. LOW_DETECTABILITY or a coverage reason
  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;



      /// Per-source estimates (include=estimates)
  @JsonKey(
    
    name: r'estimates',
    required: false,
    includeIfNull: false,
  )


  final List<ObservabilityEstimate>? estimates;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Detectability &&
      other.family == family &&
      other.regionCode == regionCode &&
      other.cell == cell &&
      other.band == band &&
      other.at == at &&
      other.p == p &&
      other.lo == lo &&
      other.hi == hi &&
      other.methods == methods &&
      other.sources == sources &&
      other.confidence == confidence &&
      other.absenceOk == absenceOk &&
      other.reason == reason &&
      other.estimates == estimates;

    @override
    int get hashCode =>
        family.hashCode +
        (regionCode == null ? 0 : regionCode.hashCode) +
        (cell == null ? 0 : cell.hashCode) +
        (band == null ? 0 : band.hashCode) +
        at.hashCode +
        p.hashCode +
        lo.hashCode +
        hi.hashCode +
        methods.hashCode +
        sources.hashCode +
        confidence.hashCode +
        absenceOk.hashCode +
        (reason == null ? 0 : reason.hashCode) +
        estimates.hashCode;

  factory Detectability.fromJson(Map<String, dynamic> json) => _$DetectabilityFromJson(json);

  Map<String, dynamic> toJson() => _$DetectabilityToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DetectabilityMethodsEnum {
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

const DetectabilityMethodsEnum(this.value);

final String value;

@override
String toString() => value;
}


