//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/field_spec.dart';
import 'package:foresignal_api/src/model/signature_verifier_config.dart';
import 'package:foresignal_api/src/model/record_mapping.dart';
import 'package:foresignal_api/src/model/licence_terms.dart';
import 'package:foresignal_api/src/model/custom_source_formats.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'custom_source.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CustomSource {
  /// Returns a new [CustomSource] instance.
  CustomSource({

    required  this.id,

    required  this.name,

     this.description,

    required  this.kinds,

     this.attribution,

     this.recordTtl,

     this.tileProperties,

     this.searchFields,

     this.embedFields,

     this.rankField,

     this.rateLimitPerMinute = 60,

     this.observedAtMode,

     this.ttlAnchor,

     this.volatileFields,

     this.significantFields,

     this.titleField,

     this.fieldSpecs,

     this.private = false,

     this.licence,

     this.recordsPerMinute = 6000,

     this.formats,

     this.verifiers,

     this.profile,

     this.mapping,

     this.preset,

     this.presetParams,

     this.allowPathKey = false,

     this.ingestAllowCidrs,

     this.captureSamples = false,

     this.licenceTerms,

    required  this.createdAt,

     this.lastIngestAt,

     this.ingestKeyPrefix,
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
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final String? attribution;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'recordTtl',
    required: false,
    includeIfNull: false,
  )


  final String? recordTtl;



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



          // minimum: 1
          // maximum: 6000
  @JsonKey(
    defaultValue: 60,
    name: r'rateLimitPerMinute',
    required: false,
    includeIfNull: false,
  )


  final int? rateLimitPerMinute;



      /// Time base; changeable only while the source has no heads (409 OBSERVED_AT_MODE_LOCKED). Under UPSTREAM a record without observedAt is rejected MISSING_OBSERVED_AT.
  @JsonKey(
    
    name: r'observedAtMode',
    required: false,
    includeIfNull: false,
  )


  final CustomSourceObservedAtModeEnum? observedAtMode;



  @JsonKey(
    
    name: r'ttlAnchor',
    required: false,
    includeIfNull: false,
  )


  final CustomSourceTtlAnchorEnum? ttlAnchor;



      /// Excluded from the hash; editing triggers lazy recipe migration
  @JsonKey(
    
    name: r'volatileFields',
    required: false,
    includeIfNull: false,
  )


  final List<String>? volatileFields;



      /// Required for SAMPLED/LATEST policies
  @JsonKey(
    
    name: r'significantFields',
    required: false,
    includeIfNull: false,
  )


  final List<String>? significantFields;



  @JsonKey(
    
    name: r'titleField',
    required: false,
    includeIfNull: false,
  )


  final String? titleField;



      /// Keys are top-level payload names
  @JsonKey(
    
    name: r'fieldSpecs',
    required: false,
    includeIfNull: false,
  )


  final Map<String, FieldSpec>? fieldSpecs;



      /// Private source (039); cannot be unset for profile personal-location
  @JsonKey(
    defaultValue: false,
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;



  @JsonKey(
    
    name: r'licence',
    required: false,
    includeIfNull: false,
  )


  final String? licence;



      /// Records/min cap per source (429 INGEST_RECORD_RATE)
          // minimum: 1
          // maximum: 60000
  @JsonKey(
    defaultValue: 6000,
    name: r'recordsPerMinute',
    required: false,
    includeIfNull: false,
  )


  final int? recordsPerMinute;



  @JsonKey(
    
    name: r'formats',
    required: false,
    includeIfNull: false,
  )


  final CustomSourceFormats? formats;



      /// Signature verifier per format id
  @JsonKey(
    
    name: r'verifiers',
    required: false,
    includeIfNull: false,
  )


  final Map<String, SignatureVerifierConfig>? verifiers;



      /// Preset defaults (039): personal-location = private, STANDARD, kind tracker, UPSTREAM + OBSERVED anchor, 24h TTL, 7d retention, SAMPLED policy, no embedding, no enrichment
  @JsonKey(
    
    name: r'profile',
    required: false,
    includeIfNull: false,
  )


  final CustomSourceProfileEnum? profile;



      /// RecordMapping used by the `mapping` ingest format and the device-preset formats (replaces 024's interim JSON-Pointer mapping under formats.mapping). Compiled against this input's kinds and observedAtMode (400 MAPPING_INVALID with per-path errors); editing it publishes the custom-source change event
  @JsonKey(
    
    name: r'mapping',
    required: false,
    includeIfNull: false,
  )


  final RecordMapping? mapping;



      /// Create only: a MappingPreset id (`GET /v1/mappings/presets`) whose defaults fill every member the input leaves out (kinds, fieldSpecs, observedAtMode, recordTtl, significantFields, volatileFields, private, allowPathKey, tileProperties, titleField, mapping, format options) and whose format becomes the default of `POST /v1/ingest/{sourceId}`. On update it may only repeat the stored value
  @JsonKey(
    
    name: r'preset',
    required: false,
    includeIfNull: false,
  )


  final String? preset;



      /// Create only: preset parameters, e.g. `lon`, `lat` (rounded to 0.01° unless `exactLocation=true`)
  @JsonKey(
    
    name: r'presetParams',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? presetParams;



      /// Accept the ingest key in the path (`/v1/ingest/{sourceId}/k/{key}/{format}`)
  @JsonKey(
    defaultValue: false,
    name: r'allowPathKey',
    required: false,
    includeIfNull: false,
  )


  final bool? allowPathKey;



      /// Sender allowlist; null = private ranges only (loopback, RFC 1918, link-local, ULA) when a path key, query key or GET is used. Others → 403 INGEST_SOURCE_DENIED
  @JsonKey(
    
    name: r'ingestAllowCidrs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? ingestAllowCidrs;



      /// Keep the last 5 raw requests in memory (redacted) for `/samples`; auto-off 1 h after it is turned on (a PUT that keeps it on does not extend the window; turning it off drops the samples)
  @JsonKey(
    defaultValue: false,
    name: r'captureSamples',
    required: false,
    includeIfNull: false,
  )


  final bool? captureSamples;



      /// Licence of pushed data, snapshotted on each PUSH batch
  @JsonKey(
    
    name: r'licenceTerms',
    required: false,
    includeIfNull: false,
  )


  final LicenceTerms? licenceTerms;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'lastIngestAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastIngestAt;



  @JsonKey(
    
    name: r'ingestKeyPrefix',
    required: false,
    includeIfNull: false,
  )


  final String? ingestKeyPrefix;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CustomSource &&
      other.id == id &&
      other.name == name &&
      other.description == description &&
      other.kinds == kinds &&
      other.attribution == attribution &&
      other.recordTtl == recordTtl &&
      other.tileProperties == tileProperties &&
      other.searchFields == searchFields &&
      other.embedFields == embedFields &&
      other.rankField == rankField &&
      other.rateLimitPerMinute == rateLimitPerMinute &&
      other.observedAtMode == observedAtMode &&
      other.ttlAnchor == ttlAnchor &&
      other.volatileFields == volatileFields &&
      other.significantFields == significantFields &&
      other.titleField == titleField &&
      other.fieldSpecs == fieldSpecs &&
      other.private == private &&
      other.licence == licence &&
      other.recordsPerMinute == recordsPerMinute &&
      other.formats == formats &&
      other.verifiers == verifiers &&
      other.profile == profile &&
      other.mapping == mapping &&
      other.preset == preset &&
      other.presetParams == presetParams &&
      other.allowPathKey == allowPathKey &&
      other.ingestAllowCidrs == ingestAllowCidrs &&
      other.captureSamples == captureSamples &&
      other.licenceTerms == licenceTerms &&
      other.createdAt == createdAt &&
      other.lastIngestAt == lastIngestAt &&
      other.ingestKeyPrefix == ingestKeyPrefix;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        description.hashCode +
        kinds.hashCode +
        attribution.hashCode +
        (recordTtl == null ? 0 : recordTtl.hashCode) +
        tileProperties.hashCode +
        searchFields.hashCode +
        embedFields.hashCode +
        (rankField == null ? 0 : rankField.hashCode) +
        rateLimitPerMinute.hashCode +
        observedAtMode.hashCode +
        ttlAnchor.hashCode +
        volatileFields.hashCode +
        significantFields.hashCode +
        (titleField == null ? 0 : titleField.hashCode) +
        fieldSpecs.hashCode +
        private.hashCode +
        (licence == null ? 0 : licence.hashCode) +
        recordsPerMinute.hashCode +
        formats.hashCode +
        verifiers.hashCode +
        (profile == null ? 0 : profile.hashCode) +
        (mapping == null ? 0 : mapping.hashCode) +
        (preset == null ? 0 : preset.hashCode) +
        (presetParams == null ? 0 : presetParams.hashCode) +
        allowPathKey.hashCode +
        (ingestAllowCidrs == null ? 0 : ingestAllowCidrs.hashCode) +
        captureSamples.hashCode +
        (licenceTerms == null ? 0 : licenceTerms.hashCode) +
        createdAt.hashCode +
        (lastIngestAt == null ? 0 : lastIngestAt.hashCode) +
        ingestKeyPrefix.hashCode;

  factory CustomSource.fromJson(Map<String, dynamic> json) => _$CustomSourceFromJson(json);

  Map<String, dynamic> toJson() => _$CustomSourceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Time base; changeable only while the source has no heads (409 OBSERVED_AT_MODE_LOCKED). Under UPSTREAM a record without observedAt is rejected MISSING_OBSERVED_AT.
enum CustomSourceObservedAtModeEnum {
@JsonValue(r'UPSTREAM')
UPSTREAM(r'UPSTREAM'),
@JsonValue(r'RECEIVED')
RECEIVED(r'RECEIVED');

const CustomSourceObservedAtModeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum CustomSourceTtlAnchorEnum {
@JsonValue(r'RECEIVED')
RECEIVED(r'RECEIVED'),
@JsonValue(r'OBSERVED')
OBSERVED(r'OBSERVED');

const CustomSourceTtlAnchorEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Preset defaults (039): personal-location = private, STANDARD, kind tracker, UPSTREAM + OBSERVED anchor, 24h TTL, 7d retention, SAMPLED policy, no embedding, no enrichment
enum CustomSourceProfileEnum {
@JsonValue(r'personal-location')
personalLocation(r'personal-location');

const CustomSourceProfileEnum(this.value);

final String value;

@override
String toString() => value;
}


