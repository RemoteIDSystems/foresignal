//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/credential_field.dart';
import 'package:foresignal_api/src/model/field_threshold.dart';
import 'package:foresignal_api/src/model/ingest_policy.dart';
import 'package:foresignal_api/src/model/field_spec.dart';
import 'package:foresignal_api/src/model/source_access.dart';
import 'package:foresignal_api/src/model/budget.dart';
import 'package:foresignal_api/src/model/history_tier.dart';
import 'package:foresignal_api/src/model/licence_terms.dart';
import 'package:foresignal_api/src/model/param_spec.dart';
import 'package:foresignal_api/src/model/declared_observability.dart';
import 'package:foresignal_api/src/model/motion_spec.dart';
import 'package:foresignal_api/src/model/kind_display.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'source_descriptor.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SourceDescriptor {
  /// Returns a new [SourceDescriptor] instance.
  SourceDescriptor({

    required  this.id,

    required  this.name,

     this.description,

    required  this.kinds,

     this.homepage,

     this.attribution,

    required  this.mode,

     this.custom,

    required  this.credentialFields,

    required  this.params,

     this.minInterval,

     this.defaultInterval,

    required  this.defaultBudget,

     this.recordTtl,

     this.zeroResultsNormal,

     this.tileProperties,

     this.searchFields,

     this.embedFields,

     this.rankField,

     this.licence,

     this.termsUrl,

     this.historyTier,

     this.observedAtMode,

     this.ttlAnchor,

     this.defaultIngestPolicy,

     this.volatileFields,

     this.significantFields,

     this.motion,

     this.thresholds,

     this.defaultRetention,

     this.kindRetention,

     this.skewTolerance,

     this.allowNullIsland = false,

     this.products,

     this.titleField,

     this.entityKeys,

     this.fieldSpecs,

     this.kindDisplay,

     this.aggBelowZoom,

     this.startField,

     this.endField,

     this.private = false,

     this.privatePrecisionDecimals = 4,

     this.acceptsRemoteMedia,

     this.egress,

     this.egressOverridable = false,

     this.llmNotice,

     this.licenceTerms,

     this.reliability,

     this.derivedFrom,

     this.originator,

     this.upstreamRefFields,

     this.observability,

     this.access,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'kinds',
    required: true,
    includeIfNull: false,
  )


  final List<String> kinds;



  @JsonKey(
    
    name: r'homepage',
    required: false,
    includeIfNull: false,
  )


  final String? homepage;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final String? attribution;



      /// Collection mode; `derived` sources are fed by ChangeBus or frame events and never polled
  @JsonKey(
    
    name: r'mode',
    required: true,
    includeIfNull: false,
  )


  final SourceDescriptorModeEnum mode;



      /// Runtime-defined custom push source
  @JsonKey(
    
    name: r'custom',
    required: false,
    includeIfNull: false,
  )


  final bool? custom;



  @JsonKey(
    
    name: r'credentialFields',
    required: true,
    includeIfNull: false,
  )


  final List<CredentialField> credentialFields;



  @JsonKey(
    
    name: r'params',
    required: true,
    includeIfNull: false,
  )


  final List<ParamSpec> params;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'minInterval',
    required: false,
    includeIfNull: false,
  )


  final String? minInterval;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'defaultInterval',
    required: false,
    includeIfNull: false,
  )


  final String? defaultInterval;



  @JsonKey(
    
    name: r'defaultBudget',
    required: true,
    includeIfNull: false,
  )


  final Budget defaultBudget;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'recordTtl',
    required: false,
    includeIfNull: false,
  )


  final String? recordTtl;



  @JsonKey(
    
    name: r'zeroResultsNormal',
    required: false,
    includeIfNull: false,
  )


  final bool? zeroResultsNormal;



  @JsonKey(
    
    name: r'tileProperties',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tileProperties;



  @JsonKey(
    
    name: r'searchFields',
    required: false,
    includeIfNull: false,
  )


  final List<String>? searchFields;



  @JsonKey(
    
    name: r'embedFields',
    required: false,
    includeIfNull: false,
  )


  final List<String>? embedFields;



  @JsonKey(
    
    name: r'rankField',
    required: false,
    includeIfNull: false,
  )


  final String? rankField;



      /// Licence identifier (snapshotted on fetch rows)
  @JsonKey(
    
    name: r'licence',
    required: false,
    includeIfNull: false,
  )


  final String? licence;



  @JsonKey(
    
    name: r'termsUrl',
    required: false,
    includeIfNull: false,
  )


  final String? termsUrl;



  @JsonKey(
    
    name: r'historyTier',
    required: false,
    includeIfNull: false,
  )


  final HistoryTier? historyTier;



      /// Fixed time base: upstream timestamps, or receipt time for every record (TIME_IMPUTED)
  @JsonKey(
    
    name: r'observedAtMode',
    required: false,
    includeIfNull: false,
  )


  final SourceDescriptorObservedAtModeEnum? observedAtMode;



      /// Axis for TTL and keyframes (TRACK must be OBSERVED; STANDARD defaults to RECEIVED)
  @JsonKey(
    
    name: r'ttlAnchor',
    required: false,
    includeIfNull: false,
  )


  final SourceDescriptorTtlAnchorEnum? ttlAnchor;



      /// The descriptor's policy with the Core-Data-Model §5.1 defaults filled in (ALL keyframe = ttl − pollInterval − 30s or `none`; edgeMinInterval = minInterval / 2)
  @JsonKey(
    
    name: r'defaultIngestPolicy',
    required: false,
    includeIfNull: false,
  )


  final IngestPolicy? defaultIngestPolicy;



      /// Top-level payload keys left out of hashes and kinematic state
  @JsonKey(
    
    name: r'volatileFields',
    required: false,
    includeIfNull: false,
  )


  final List<String>? volatileFields;



      /// Keys whose change persists SIGNIFICANT; empty = every non-volatile key
  @JsonKey(
    
    name: r'significantFields',
    required: false,
    includeIfNull: false,
  )


  final List<String>? significantFields;



  @JsonKey(
    
    name: r'motion',
    required: false,
    includeIfNull: false,
  )


  final MotionSpec? motion;



  @JsonKey(
    
    name: r'thresholds',
    required: false,
    includeIfNull: false,
  )


  final List<FieldThreshold>? thresholds;



      /// STANDARD only. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'defaultRetention',
    required: false,
    includeIfNull: false,
  )


  final String? defaultRetention;



      /// Per-kind default retention (STANDARD only)
  @JsonKey(
    
    name: r'kindRetention',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? kindRetention;



      /// Future-clamp tolerance before FUTURE_CLAMPED is flagged. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'skewTolerance',
    required: false,
    includeIfNull: false,
  )


  final String? skewTolerance;



      /// Keep exact (0,0) points instead of nulling them
  @JsonKey(
    defaultValue: false,
    name: r'allowNullIsland',
    required: false,
    includeIfNull: false,
  )


  final bool? allowNullIsland;



      /// Raster product ids served by this source
  @JsonKey(
    
    name: r'products',
    required: false,
    includeIfNull: false,
  )


  final List<String>? products;



      /// Payload key used as record title
  @JsonKey(
    
    name: r'titleField',
    required: false,
    includeIfNull: false,
  )


  final String? titleField;



      /// Entity key type → top-level payload field (icao24, registration, mmsi, imo, callsign …)
  @JsonKey(
    
    name: r'entityKeys',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? entityKeys;



      /// Declared payload fields keyed by top-level name
  @JsonKey(
    
    name: r'fieldSpecs',
    required: false,
    includeIfNull: false,
  )


  final Map<String, FieldSpec>? fieldSpecs;



      /// Display hints keyed by kind
  @JsonKey(
    
    name: r'kindDisplay',
    required: false,
    includeIfNull: false,
  )


  final Map<String, KindDisplay>? kindDisplay;



      /// Tiles aggregate below this zoom with agg=auto; null = never
          // minimum: 0
          // maximum: 22
  @JsonKey(
    
    name: r'aggBelowZoom',
    required: false,
    includeIfNull: false,
  )


  final int? aggBelowZoom;



      /// Payload key of an interval start (with endField)
  @JsonKey(
    
    name: r'startField',
    required: false,
    includeIfNull: false,
  )


  final String? startField;



      /// Payload key of an interval end (with startField)
  @JsonKey(
    
    name: r'endField',
    required: false,
    includeIfNull: false,
  )


  final String? endField;



      /// Personal data: excluded from anonymous reads, tiles and other egress paths
  @JsonKey(
    defaultValue: false,
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;



      /// Coordinate decimals kept for private sources
          // minimum: 2
          // maximum: 7
  @JsonKey(
    defaultValue: 4,
    name: r'privatePrecisionDecimals',
    required: false,
    includeIfNull: false,
  )


  final int? privatePrecisionDecimals;



      /// Source accepts `POST /v1/ingest/{id}/media|rasters` (satdump-* family)
  @JsonKey(
    
    name: r'acceptsRemoteMedia',
    required: false,
    includeIfNull: false,
  )


  final bool? acceptsRemoteMedia;



      /// LOCAL_ONLY: served to this instance's own API clients (reads, tiles, SSE, MCP, an export to the caller) and notifications, never re-served by sinks (110), federation (113), share links (112) or feeds and feed keys; off-box archive exports (111) refuse it too. Set by the upstream's terms: opensky-states is LOCAL_ONLY (062), the ODbL adsb.lol sources are OPEN. MLAT positions are never redistributable regardless (069).
  @JsonKey(
    
    name: r'egress',
    required: false,
    includeIfNull: false,
  )


  final SourceDescriptorEgressEnum? egress;



      /// The operator may override `egress` in SourceSettings (local receivers, 069)
  @JsonKey(
    defaultValue: false,
    name: r'egressOverridable',
    required: false,
    includeIfNull: false,
  )


  final bool? egressOverridable;



      /// Notice appended whenever this source's records are placed in an LLM context (090; e.g. OpenFEMA eligibility notice)
  @JsonKey(
    
    name: r'llmNotice',
    required: false,
    includeIfNull: false,
  )


  final String? llmNotice;



      /// Structured terms; derived from the catalogue when only `licence` is set and matches an id
  @JsonKey(
    
    name: r'licenceTerms',
    required: false,
    includeIfNull: false,
  )


  final LicenceTerms? licenceTerms;



      /// Default reliability (F unless set); overridable per source
  @JsonKey(
    
    name: r'reliability',
    required: false,
    includeIfNull: false,
  )


  final SourceDescriptorReliabilityEnum? reliability;



      /// DERIVED sources: input source ids; the most restrictive input terms apply
  @JsonKey(
    
    name: r'derivedFrom',
    required: false,
    includeIfNull: false,
  )


  final List<String>? derivedFrom;



      /// Originator id override (default from intel/originators.json)
  @JsonKey(
    
    name: r'originator',
    required: false,
    includeIfNull: false,
  )


  final String? originator;



      /// Payload fields carrying the upstream's own event id ('field', or 'field@namespace' so differently named fields of two sources compare; '$externalId' is the record's external id) or, prefixed 'cite:', the cited original source (a URL, domain or name). Empty means the default from intel/originators.json; GET /v1/sources shows the fields in force.
  @JsonKey(
    
    name: r'upstreamRefFields',
    required: false,
    includeIfNull: false,
  )


  final List<String>? upstreamRefFields;



      /// Declared observability per kind
  @JsonKey(
    
    name: r'observability',
    required: false,
    includeIfNull: false,
  )


  final DeclaredObservability? observability;



  @JsonKey(
    
    name: r'access',
    required: false,
    includeIfNull: false,
  )


  final SourceAccess? access;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SourceDescriptor &&
      other.id == id &&
      other.name == name &&
      other.description == description &&
      other.kinds == kinds &&
      other.homepage == homepage &&
      other.attribution == attribution &&
      other.mode == mode &&
      other.custom == custom &&
      other.credentialFields == credentialFields &&
      other.params == params &&
      other.minInterval == minInterval &&
      other.defaultInterval == defaultInterval &&
      other.defaultBudget == defaultBudget &&
      other.recordTtl == recordTtl &&
      other.zeroResultsNormal == zeroResultsNormal &&
      other.tileProperties == tileProperties &&
      other.searchFields == searchFields &&
      other.embedFields == embedFields &&
      other.rankField == rankField &&
      other.licence == licence &&
      other.termsUrl == termsUrl &&
      other.historyTier == historyTier &&
      other.observedAtMode == observedAtMode &&
      other.ttlAnchor == ttlAnchor &&
      other.defaultIngestPolicy == defaultIngestPolicy &&
      other.volatileFields == volatileFields &&
      other.significantFields == significantFields &&
      other.motion == motion &&
      other.thresholds == thresholds &&
      other.defaultRetention == defaultRetention &&
      other.kindRetention == kindRetention &&
      other.skewTolerance == skewTolerance &&
      other.allowNullIsland == allowNullIsland &&
      other.products == products &&
      other.titleField == titleField &&
      other.entityKeys == entityKeys &&
      other.fieldSpecs == fieldSpecs &&
      other.kindDisplay == kindDisplay &&
      other.aggBelowZoom == aggBelowZoom &&
      other.startField == startField &&
      other.endField == endField &&
      other.private == private &&
      other.privatePrecisionDecimals == privatePrecisionDecimals &&
      other.acceptsRemoteMedia == acceptsRemoteMedia &&
      other.egress == egress &&
      other.egressOverridable == egressOverridable &&
      other.llmNotice == llmNotice &&
      other.licenceTerms == licenceTerms &&
      other.reliability == reliability &&
      other.derivedFrom == derivedFrom &&
      other.originator == originator &&
      other.upstreamRefFields == upstreamRefFields &&
      other.observability == observability &&
      other.access == access;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        description.hashCode +
        kinds.hashCode +
        homepage.hashCode +
        attribution.hashCode +
        mode.hashCode +
        custom.hashCode +
        credentialFields.hashCode +
        params.hashCode +
        minInterval.hashCode +
        defaultInterval.hashCode +
        defaultBudget.hashCode +
        (recordTtl == null ? 0 : recordTtl.hashCode) +
        zeroResultsNormal.hashCode +
        tileProperties.hashCode +
        searchFields.hashCode +
        embedFields.hashCode +
        (rankField == null ? 0 : rankField.hashCode) +
        (licence == null ? 0 : licence.hashCode) +
        (termsUrl == null ? 0 : termsUrl.hashCode) +
        historyTier.hashCode +
        observedAtMode.hashCode +
        ttlAnchor.hashCode +
        defaultIngestPolicy.hashCode +
        volatileFields.hashCode +
        significantFields.hashCode +
        (motion == null ? 0 : motion.hashCode) +
        thresholds.hashCode +
        (defaultRetention == null ? 0 : defaultRetention.hashCode) +
        kindRetention.hashCode +
        skewTolerance.hashCode +
        allowNullIsland.hashCode +
        products.hashCode +
        (titleField == null ? 0 : titleField.hashCode) +
        entityKeys.hashCode +
        fieldSpecs.hashCode +
        kindDisplay.hashCode +
        (aggBelowZoom == null ? 0 : aggBelowZoom.hashCode) +
        (startField == null ? 0 : startField.hashCode) +
        (endField == null ? 0 : endField.hashCode) +
        private.hashCode +
        privatePrecisionDecimals.hashCode +
        acceptsRemoteMedia.hashCode +
        egress.hashCode +
        egressOverridable.hashCode +
        (llmNotice == null ? 0 : llmNotice.hashCode) +
        (licenceTerms == null ? 0 : licenceTerms.hashCode) +
        reliability.hashCode +
        derivedFrom.hashCode +
        (originator == null ? 0 : originator.hashCode) +
        upstreamRefFields.hashCode +
        (observability == null ? 0 : observability.hashCode) +
        access.hashCode;

  factory SourceDescriptor.fromJson(Map<String, dynamic> json) => _$SourceDescriptorFromJson(json);

  Map<String, dynamic> toJson() => _$SourceDescriptorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Collection mode; `derived` sources are fed by ChangeBus or frame events and never polled
enum SourceDescriptorModeEnum {
@JsonValue(r'polling')
polling(r'polling'),
@JsonValue(r'streaming')
streaming(r'streaming'),
@JsonValue(r'push')
push(r'push'),
@JsonValue(r'derived')
derived(r'derived');

const SourceDescriptorModeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Fixed time base: upstream timestamps, or receipt time for every record (TIME_IMPUTED)
enum SourceDescriptorObservedAtModeEnum {
@JsonValue(r'UPSTREAM')
UPSTREAM(r'UPSTREAM'),
@JsonValue(r'RECEIVED')
RECEIVED(r'RECEIVED');

const SourceDescriptorObservedAtModeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Axis for TTL and keyframes (TRACK must be OBSERVED; STANDARD defaults to RECEIVED)
enum SourceDescriptorTtlAnchorEnum {
@JsonValue(r'OBSERVED')
OBSERVED(r'OBSERVED'),
@JsonValue(r'RECEIVED')
RECEIVED(r'RECEIVED');

const SourceDescriptorTtlAnchorEnum(this.value);

final String value;

@override
String toString() => value;
}


/// LOCAL_ONLY: served to this instance's own API clients (reads, tiles, SSE, MCP, an export to the caller) and notifications, never re-served by sinks (110), federation (113), share links (112) or feeds and feed keys; off-box archive exports (111) refuse it too. Set by the upstream's terms: opensky-states is LOCAL_ONLY (062), the ODbL adsb.lol sources are OPEN. MLAT positions are never redistributable regardless (069).
enum SourceDescriptorEgressEnum {
@JsonValue(r'OPEN')
OPEN(r'OPEN'),
@JsonValue(r'LOCAL_ONLY')
LOCAL_ONLY(r'LOCAL_ONLY');

const SourceDescriptorEgressEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Default reliability (F unless set); overridable per source
enum SourceDescriptorReliabilityEnum {
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

const SourceDescriptorReliabilityEnum(this.value);

final String value;

@override
String toString() => value;
}


