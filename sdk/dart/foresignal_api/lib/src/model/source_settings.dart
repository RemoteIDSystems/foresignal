//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ingest_policy.dart';
import 'package:foresignal_api/src/model/budget.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'source_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SourceSettings {
  /// Returns a new [SourceSettings] instance.
  SourceSettings({

    required  this.enabled,

    required  this.interval,

    required  this.params,

    required  this.budget,

     this.retention,

     this.embed,

    required  this.credentialsSet,

     this.updatedAt,

     this.ingestPolicy,

     this.effectiveIngestPolicy,

     this.effectiveRetention,

     this.statsEnabled = true,

     this.statsIncludePrivate = false,

     this.private,

     this.regionTagging,

     this.downsampleInterval,

     this.egress,

     this.reliability,
  });

  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'interval',
    required: true,
    includeIfNull: false,
  )


  final String interval;



  @JsonKey(
    
    name: r'params',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> params;



  @JsonKey(
    
    name: r'budget',
    required: true,
    includeIfNull: false,
  )


  final Budget budget;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'retention',
    required: false,
    includeIfNull: false,
  )


  final String? retention;



      /// Embed this source's records for semantic search (default true when the source declares embedFields)
  @JsonKey(
    
    name: r'embed',
    required: false,
    includeIfNull: false,
  )


  final bool? embed;



      /// True if credentials are stored (values never returned)
  @JsonKey(
    
    name: r'credentialsSet',
    required: true,
    includeIfNull: false,
  )


  final bool credentialsSet;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;



      /// Operator override; null = descriptor default
  @JsonKey(
    
    name: r'ingestPolicy',
    required: false,
    includeIfNull: false,
  )


  final IngestPolicy? ingestPolicy;



      /// Override if set, else the descriptor default
  @JsonKey(
    
    name: r'effectiveIngestPolicy',
    required: false,
    includeIfNull: false,
  )


  final IngestPolicy? effectiveIngestPolicy;



      /// Effective retention window (TRACK: `retention.trackWindow`). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'effectiveRetention',
    required: false,
    includeIfNull: false,
  )


  final String? effectiveRetention;



      /// Include in activity rollups (038)
  @JsonKey(
    defaultValue: true,
    name: r'statsEnabled',
    required: false,
    includeIfNull: false,
  )


  final bool? statsEnabled;



      /// Roll up this private source anyway (038 `stats.includePrivate[sourceId]`)
  @JsonKey(
    defaultValue: false,
    name: r'statsIncludePrivate',
    required: false,
    includeIfNull: false,
  )


  final bool? statsIncludePrivate;



      /// Operator tightening to private (039); a descriptor-private source cannot be made public
  @JsonKey(
    
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;



      /// Default eager for STANDARD, lazy for TRACK (040)
  @JsonKey(
    
    name: r'regionTagging',
    required: false,
    includeIfNull: false,
  )


  final SourceSettingsRegionTaggingEnum? regionTagging;



      /// Per-source downsample interval override (TRACK sources only). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'downsampleInterval',
    required: false,
    includeIfNull: false,
  )


  final String? downsampleInterval;



      /// Operator override of the descriptor egress; only when `egressOverridable` (else 400 EGRESS_FIXED); null = descriptor default
  @JsonKey(
    
    name: r'egress',
    required: false,
    includeIfNull: false,
  )


  final SourceSettingsEgressEnum? egress;



      /// Operator override of the descriptor reliability; null = descriptor default
  @JsonKey(
    
    name: r'reliability',
    required: false,
    includeIfNull: false,
  )


  final SourceSettingsReliabilityEnum? reliability;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SourceSettings &&
      other.enabled == enabled &&
      other.interval == interval &&
      other.params == params &&
      other.budget == budget &&
      other.retention == retention &&
      other.embed == embed &&
      other.credentialsSet == credentialsSet &&
      other.updatedAt == updatedAt &&
      other.ingestPolicy == ingestPolicy &&
      other.effectiveIngestPolicy == effectiveIngestPolicy &&
      other.effectiveRetention == effectiveRetention &&
      other.statsEnabled == statsEnabled &&
      other.statsIncludePrivate == statsIncludePrivate &&
      other.private == private &&
      other.regionTagging == regionTagging &&
      other.downsampleInterval == downsampleInterval &&
      other.egress == egress &&
      other.reliability == reliability;

    @override
    int get hashCode =>
        enabled.hashCode +
        interval.hashCode +
        params.hashCode +
        budget.hashCode +
        (retention == null ? 0 : retention.hashCode) +
        embed.hashCode +
        credentialsSet.hashCode +
        updatedAt.hashCode +
        (ingestPolicy == null ? 0 : ingestPolicy.hashCode) +
        effectiveIngestPolicy.hashCode +
        (effectiveRetention == null ? 0 : effectiveRetention.hashCode) +
        statsEnabled.hashCode +
        statsIncludePrivate.hashCode +
        private.hashCode +
        regionTagging.hashCode +
        (downsampleInterval == null ? 0 : downsampleInterval.hashCode) +
        (egress == null ? 0 : egress.hashCode) +
        (reliability == null ? 0 : reliability.hashCode);

  factory SourceSettings.fromJson(Map<String, dynamic> json) => _$SourceSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$SourceSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Default eager for STANDARD, lazy for TRACK (040)
enum SourceSettingsRegionTaggingEnum {
@JsonValue(r'eager')
eager(r'eager'),
@JsonValue(r'lazy')
lazy(r'lazy'),
@JsonValue(r'off')
off(r'off');

const SourceSettingsRegionTaggingEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Operator override of the descriptor egress; only when `egressOverridable` (else 400 EGRESS_FIXED); null = descriptor default
enum SourceSettingsEgressEnum {
@JsonValue(r'OPEN')
OPEN(r'OPEN'),
@JsonValue(r'LOCAL_ONLY')
LOCAL_ONLY(r'LOCAL_ONLY');

const SourceSettingsEgressEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Operator override of the descriptor reliability; null = descriptor default
enum SourceSettingsReliabilityEnum {
@JsonValue(r'A')
A(r'A'),
@JsonValue(r'B')
B(r'B'),
@JsonValue(r'C')
C(r'C'),
@JsonValue(r'D')
D(r'D'),
@JsonValue(r'E')
E(r'E'),
@JsonValue(r'F')
F(r'F');

const SourceSettingsReliabilityEnum(this.value);

final String value;

@override
String toString() => value;
}


