//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/intel_event_log_entry.dart';
import 'package:foresignal_api/src/model/intelligence_event_speculation.dart';
import 'package:foresignal_api/src/model/severity.dart';
import 'package:foresignal_api/src/model/exposure.dart';
import 'package:foresignal_api/src/model/intelligence_event_evidence.dart';
import 'package:foresignal_api/src/model/intelligence_event_context.dart';
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:foresignal_api/src/model/intelligence_event_inference.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intelligence_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelligenceEvent {
  /// Returns a new [IntelligenceEvent] instance.
  IntelligenceEvent({

    required  this.id,

    required  this.type,

    required  this.title,

    required  this.status,

    required  this.severity,

     this.severityOverridden,

     this.private,

    required  this.origin,

     this.recipeId,

     this.recipeVersion,

    required  this.detector,

     this.mergedInto,

     this.notes,

    required  this.version,

    required  this.createdAt,

    required  this.updatedAt,

    required  this.evidence,

    required  this.inference,

    required  this.speculation,

     this.context,

     this.exposure,

     this.log,

     this.manifestId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final IntelligenceEventTypeEnum type;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final IntelligenceEventStatusEnum status;



  @JsonKey(
    
    name: r'severity',
    required: true,
    includeIfNull: false,
  )


  final Severity severity;



      /// True when an operator override sets the severity; `severity: null` in `IntelEventPatch` clears it and restores the computed severity
  @JsonKey(
    
    name: r'severityOverridden',
    required: false,
    includeIfNull: false,
  )


  final bool? severityOverridden;



      /// True when any member comes from a private source
  @JsonKey(
    
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;



  @JsonKey(
    
    name: r'origin',
    required: true,
    includeIfNull: false,
  )


  final IntelligenceEventOriginEnum origin;



  @JsonKey(
    
    name: r'recipeId',
    required: false,
    includeIfNull: false,
  )


  final String? recipeId;



  @JsonKey(
    
    name: r'recipeVersion',
    required: false,
    includeIfNull: false,
  )


  final int? recipeVersion;



  @JsonKey(
    
    name: r'detector',
    required: true,
    includeIfNull: false,
  )


  final DetectorRef detector;



      /// ULID identifier
  @JsonKey(
    
    name: r'mergedInto',
    required: false,
    includeIfNull: false,
  )


  final String? mergedInto;



      /// Operator or agent notes (markdown)
  @JsonKey(
    
    name: r'notes',
    required: false,
    includeIfNull: false,
  )


  final String? notes;



      /// Optimistic-lock version; send it as If-Match on PATCH
  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;



  @JsonKey(
    
    name: r'evidence',
    required: true,
    includeIfNull: false,
  )


  final IntelligenceEventEvidence evidence;



  @JsonKey(
    
    name: r'inference',
    required: true,
    includeIfNull: false,
  )


  final IntelligenceEventInference inference;



  @JsonKey(
    
    name: r'speculation',
    required: true,
    includeIfNull: false,
  )


  final IntelligenceEventSpeculation speculation;



  @JsonKey(
    
    name: r'context',
    required: false,
    includeIfNull: false,
  )


  final IntelligenceEventContext? context;



  @JsonKey(
    
    name: r'exposure',
    required: false,
    includeIfNull: false,
  )


  final Exposure? exposure;



  @JsonKey(
    
    name: r'log',
    required: false,
    includeIfNull: false,
  )


  final List<IntelEventLogEntry>? log;



      /// The analysis manifest of this output (prompt 116); null for outputs written before it
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelligenceEvent &&
      other.id == id &&
      other.type == type &&
      other.title == title &&
      other.status == status &&
      other.severity == severity &&
      other.severityOverridden == severityOverridden &&
      other.private == private &&
      other.origin == origin &&
      other.recipeId == recipeId &&
      other.recipeVersion == recipeVersion &&
      other.detector == detector &&
      other.mergedInto == mergedInto &&
      other.notes == notes &&
      other.version == version &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt &&
      other.evidence == evidence &&
      other.inference == inference &&
      other.speculation == speculation &&
      other.context == context &&
      other.exposure == exposure &&
      other.log == log &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        title.hashCode +
        status.hashCode +
        severity.hashCode +
        severityOverridden.hashCode +
        private.hashCode +
        origin.hashCode +
        (recipeId == null ? 0 : recipeId.hashCode) +
        (recipeVersion == null ? 0 : recipeVersion.hashCode) +
        detector.hashCode +
        (mergedInto == null ? 0 : mergedInto.hashCode) +
        (notes == null ? 0 : notes.hashCode) +
        version.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode +
        evidence.hashCode +
        inference.hashCode +
        speculation.hashCode +
        context.hashCode +
        (exposure == null ? 0 : exposure.hashCode) +
        log.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode);

  factory IntelligenceEvent.fromJson(Map<String, dynamic> json) => _$IntelligenceEventFromJson(json);

  Map<String, dynamic> toJson() => _$IntelligenceEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IntelligenceEventTypeEnum {
@JsonValue(r'maritime-disruption')
maritimeDisruption(r'maritime-disruption'),
@JsonValue(r'aviation-anomaly')
aviationAnomaly(r'aviation-anomaly'),
@JsonValue(r'infrastructure-disruption')
infrastructureDisruption(r'infrastructure-disruption'),
@JsonValue(r'weather-impact')
weatherImpact(r'weather-impact'),
@JsonValue(r'seismic')
seismic(r'seismic'),
@JsonValue(r'custom')
custom(r'custom');

const IntelligenceEventTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IntelligenceEventStatusEnum {
@JsonValue(r'emerging')
emerging(r'emerging'),
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'resolved')
resolved(r'resolved'),
@JsonValue(r'dismissed')
dismissed(r'dismissed');

const IntelligenceEventStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IntelligenceEventOriginEnum {
@JsonValue(r'recipe')
recipe(r'recipe'),
@JsonValue(r'analyst')
analyst(r'analyst'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'operator')
operator_(r'operator');

const IntelligenceEventOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


