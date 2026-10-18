//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ingest_policy.dart';
import 'package:foresignal_api/src/model/budget.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'source_settings_update.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SourceSettingsUpdate {
  /// Returns a new [SourceSettingsUpdate] instance.
  SourceSettingsUpdate({

     this.enabled,

     this.interval,

     this.params,

     this.budget,

     this.retention,

     this.embed,

     this.ingestPolicy,

     this.statsEnabled,

     this.statsIncludePrivate,

     this.private,

     this.regionTagging,

     this.downsampleInterval,

     this.egress,

     this.reliability,
  });

  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'interval',
    required: false,
    includeIfNull: false,
  )


  final String? interval;



  @JsonKey(
    
    name: r'params',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? params;



  @JsonKey(
    
    name: r'budget',
    required: false,
    includeIfNull: false,
  )


  final Budget? budget;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'retention',
    required: false,
    includeIfNull: false,
  )


  final String? retention;



      /// Embed this source's records for semantic search (027). `true` is refused (400 VALIDATION_FAILED, field `embed`) when the source declares no embedFields or is private; turning it off deletes the source's record documents.
  @JsonKey(
    
    name: r'embed',
    required: false,
    includeIfNull: false,
  )


  final bool? embed;



      /// Explicit null clears the override; omitted leaves it unchanged. Invalid → 400 INGEST_POLICY_INVALID
  @JsonKey(
    
    name: r'ingestPolicy',
    required: false,
    includeIfNull: false,
  )


  final IngestPolicy? ingestPolicy;



  @JsonKey(
    
    name: r'statsEnabled',
    required: false,
    includeIfNull: false,
  )


  final bool? statsEnabled;



  @JsonKey(
    
    name: r'statsIncludePrivate',
    required: false,
    includeIfNull: false,
  )


  final bool? statsIncludePrivate;



  @JsonKey(
    
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;



  @JsonKey(
    
    name: r'regionTagging',
    required: false,
    includeIfNull: false,
  )


  final SourceSettingsUpdateRegionTaggingEnum? regionTagging;



      /// TRACK sources only (STANDARD → 400 VALIDATION_FAILED, field `downsampleInterval`); explicit null clears it. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'downsampleInterval',
    required: false,
    includeIfNull: false,
  )


  final String? downsampleInterval;



      /// Egress override; accepted only when the descriptor is `egressOverridable` (400 EGRESS_FIXED otherwise); null reverts to the descriptor
  @JsonKey(
    
    name: r'egress',
    required: false,
    includeIfNull: false,
  )


  final SourceSettingsUpdateEgressEnum? egress;



      /// Admiralty reliability override; null reverts to the descriptor default
  @JsonKey(
    
    name: r'reliability',
    required: false,
    includeIfNull: false,
  )


  final SourceSettingsUpdateReliabilityEnum? reliability;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SourceSettingsUpdate &&
      other.enabled == enabled &&
      other.interval == interval &&
      other.params == params &&
      other.budget == budget &&
      other.retention == retention &&
      other.embed == embed &&
      other.ingestPolicy == ingestPolicy &&
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
        (ingestPolicy == null ? 0 : ingestPolicy.hashCode) +
        statsEnabled.hashCode +
        statsIncludePrivate.hashCode +
        private.hashCode +
        regionTagging.hashCode +
        (downsampleInterval == null ? 0 : downsampleInterval.hashCode) +
        (egress == null ? 0 : egress.hashCode) +
        (reliability == null ? 0 : reliability.hashCode);

  factory SourceSettingsUpdate.fromJson(Map<String, dynamic> json) => _$SourceSettingsUpdateFromJson(json);

  Map<String, dynamic> toJson() => _$SourceSettingsUpdateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SourceSettingsUpdateRegionTaggingEnum {
@JsonValue(r'eager')
eager(r'eager'),
@JsonValue(r'lazy')
lazy(r'lazy'),
@JsonValue(r'off')
off(r'off');

const SourceSettingsUpdateRegionTaggingEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Egress override; accepted only when the descriptor is `egressOverridable` (400 EGRESS_FIXED otherwise); null reverts to the descriptor
enum SourceSettingsUpdateEgressEnum {
@JsonValue(r'OPEN')
OPEN(r'OPEN'),
@JsonValue(r'LOCAL_ONLY')
LOCAL_ONLY(r'LOCAL_ONLY');

const SourceSettingsUpdateEgressEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Admiralty reliability override; null reverts to the descriptor default
enum SourceSettingsUpdateReliabilityEnum {
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

const SourceSettingsUpdateReliabilityEnum(this.value);

final String value;

@override
String toString() => value;
}


