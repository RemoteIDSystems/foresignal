//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/trigger_rule_input_window.dart';
import 'package:foresignal_api/src/model/trigger_rule_input_conditions_inner.dart';
import 'package:foresignal_api/src/model/near.dart';
import 'package:foresignal_api/src/model/geofence_ref.dart';
import 'package:foresignal_api/src/model/trigger_rule_input_auto_resolve.dart';
import 'package:foresignal_api/src/model/active_window.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'trigger_rule_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TriggerRuleInput {
  /// Returns a new [TriggerRuleInput] instance.
  TriggerRuleInput({

    required  this.name,

     this.enabled = true,

     this.source_,

     this.kind,

     this.conditions,

     this.geofence,

     this.mode,

     this.onlyNew = false,

     this.cooldown,

     this.messageTemplate = '{{name}}: {{source}}/{{externalId}}',

     this.severity,

     this.enrich = false,

     this.enrichRadiusKm = 100,

     this.enrichWindow = '24h',

     this.tags,

     this.sources,

     this.kinds,

     this.bufferMeters,

     this.hysteresisMeters,

     this.near,

     this.reappearAsEnter,

     this.cooldownScope,

     this.autoResolve,

     this.maxAlertsPerHour,

     this.autoDisableOnStorm = false,

     this.activeFrom,

     this.activeUntil,

     this.activeWindow,

     this.forecastHorizon,

     this.escalationPolicyId,

     this.geofenceRef,

     this.weatherContext,

     this.type,

     this.window,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



      /// Deprecated alias normalised into `sources` (both given → 400)
  @Deprecated('source_ has been deprecated')
  @JsonKey(
    
    name: r'source',
    required: false,
    includeIfNull: false,
  )


  final String? source_;



      /// Deprecated alias normalised into `kinds` (both given → 400)
  @Deprecated('kind has been deprecated')
  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



      /// Payload conditions and `approach` conditions, ANDed
  @JsonKey(
    
    name: r'conditions',
    required: false,
    includeIfNull: false,
  )


  final List<TriggerRuleInputConditionsInner>? conditions;



      /// Polygon/MultiPolygon, or LineString/MultiLineString with bufferMeters (buffered server-side as ST_Buffer(line::geography, bufferMeters)); ≤ 10,000 vertices; fixed, split at the antimeridian and stored as MultiPolygon. Mutually exclusive with near. When `geofenceRoute` (a line) is sent together with bufferMeters, the route is the input and `geofence` is ignored, so a rule read back can be sent back unchanged.
  @JsonKey(
    
    name: r'geofence',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geofence;



      /// match: every matching non-HEARTBEAT version. enter: matches and the previous version did not (or was a tombstone, or is new). exit: the previous version matched and this one does not or is a tombstone. Edge modes need a geofence, near or a condition.
  @JsonKey(
    
    name: r'mode',
    required: false,
    includeIfNull: false,
  )


  final TriggerRuleInputModeEnum? mode;



  @JsonKey(
    defaultValue: false,
    name: r'onlyNew',
    required: false,
    includeIfNull: false,
  )


  final bool? onlyNew;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'cooldown',
    required: false,
    includeIfNull: false,
  )


  final String? cooldown;



      /// Placeholders {{name}}, {{source}}, {{externalId}}, {{kind}}, {{title}}, {{severity}}, {{observedAt}}, {{eventTime}}, {{time}}, {{validFrom}}, {{payload.<path>}}; region placeholders (040), empty when unknown: {{near.name}}, {{near.km}}, {{near.mi}}, {{near.bearing}} (16-point compass), {{near.text}} (\"12 km NNE of Austin, TX\"; miles with {{near.text|imperial}} or a channel's config.units=imperial), {{country}} (ISO2), {{country.name}}, {{admin1}}, {{admin1.name}}, {{county}} (the county name, else its FIPS5) and {{sea}}
  @JsonKey(
    defaultValue: '{{name}}: {{source}}/{{externalId}}',
    name: r'messageTemplate',
    required: false,
    includeIfNull: false,
  )


  final String? messageTemplate;



  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final TriggerRuleInputSeverityEnum? severity;



      /// Add an LLM context summary to each alert (030). Accepted without a chat provider: the flag is stored and enrichment is skipped until one is assigned and llm.enrichmentEnabled is on
  @JsonKey(
    defaultValue: false,
    name: r'enrich',
    required: false,
    includeIfNull: false,
  )


  final bool? enrich;



      /// Enrichment radius (030)
          // minimum: 1
          // maximum: 1000
  @JsonKey(
    defaultValue: 100,
    name: r'enrichRadiusKm',
    required: false,
    includeIfNull: false,
  )


  final num? enrichRadiusKm;



      /// Context window around the alert for enrichment (030): ISO-8601 duration or shorthand, 10m to 7d
  @JsonKey(
    defaultValue: '24h',
    name: r'enrichWindow',
    required: false,
    includeIfNull: false,
  )


  final String? enrichWindow;



      /// Copied to alerts
  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



      /// Source ids (≤ 50)
  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



      /// Kinds (≤ 50)
  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



      /// Required when geofence is a LineString/MultiLineString: corridor half-width (1–50,000 m)
          // minimum: 1
          // maximum: 50000
  @JsonKey(
    
    name: r'bufferMeters',
    required: false,
    includeIfNull: false,
  )


  final num? bufferMeters;



      /// Edge band for enter/exit geofence and near rules (0–5,000 m, default 0): an enter edge needs the position inside the fence shrunk by the band, an exit edge outside the fence grown by it, and edges per (rule, entity) alternate, so jitter along a boundary fires nothing. 400 GEOFENCE_TOO_SMALL_FOR_HYSTERESIS when the shrunk fence is empty.
          // minimum: 0
          // maximum: 5000
  @JsonKey(
    
    name: r'hysteresisMeters',
    required: false,
    includeIfNull: false,
  )


  final num? hysteresisMeters;



      /// Places, point or moving anchor (036); materialised into geofence
  @JsonKey(
    
    name: r'near',
    required: false,
    includeIfNull: false,
  )


  final Near? near;



      /// Fire enter on REAPPEAR (default true with a geofence, else false)
  @JsonKey(
    
    name: r'reappearAsEnter',
    required: false,
    includeIfNull: false,
  )


  final bool? reappearAsEnter;



  @JsonKey(
    
    name: r'cooldownScope',
    required: false,
    includeIfNull: false,
  )


  final TriggerRuleInputCooldownScopeEnum? cooldownScope;



  @JsonKey(
    
    name: r'autoResolve',
    required: false,
    includeIfNull: false,
  )


  final TriggerRuleInputAutoResolve? autoResolve;



      /// Storm cap per rule (null → triggers.defaultMaxAlertsPerHour, default 120; 0 = unlimited)
          // minimum: 0
  @JsonKey(
    
    name: r'maxAlertsPerHour',
    required: false,
    includeIfNull: false,
  )


  final int? maxAlertsPerHour;



  @JsonKey(
    defaultValue: false,
    name: r'autoDisableOnStorm',
    required: false,
    includeIfNull: false,
  )


  final bool? autoDisableOnStorm;



  @JsonKey(
    
    name: r'activeFrom',
    required: false,
    includeIfNull: false,
  )


  final DateTime? activeFrom;



      /// Rule is disabled automatically after this time
  @JsonKey(
    
    name: r'activeUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? activeUntil;



  @JsonKey(
    
    name: r'activeWindow',
    required: false,
    includeIfNull: false,
  )


  final ActiveWindow? activeWindow;



      /// Fire on versions whose validFrom ≤ evaluation time + horizon (400 FEATURE_UNAVAILABLE without validFrom). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'forecastHorizon',
    required: false,
    includeIfNull: false,
  )


  final String? forecastHorizon;



      /// Escalation policy (033), validated on save
  @JsonKey(
    
    name: r'escalationPolicyId',
    required: false,
    includeIfNull: false,
  )


  final String? escalationPolicyId;



      /// Region preset instead of a polygon (040); 409 REFERENCE_DATA_MISSING when reference data is absent
  @JsonKey(
    
    name: r'geofenceRef',
    required: false,
    includeIfNull: false,
  )


  final GeofenceRef? geofenceRef;



      /// Compute `alert.context.weather` for this rule's geolocated alerts (global setting `weather.alertContext`)
  @JsonKey(
    
    name: r'weatherContext',
    required: false,
    includeIfNull: false,
  )


  final TriggerRuleInputWeatherContextEnum? weatherContext;



      /// Rule type; existing rules are `record`. `onlyNew`, `mode` and `reappearAsEnter` are rejected for windowed types (400 WINDOW_RULE_INVALID).
  @JsonKey(
    
    name: r'type',
    required: false,
    includeIfNull: false,
  )


  final TriggerRuleInputTypeEnum? type;



  @JsonKey(
    
    name: r'window',
    required: false,
    includeIfNull: false,
  )


  final TriggerRuleInputWindow? window;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TriggerRuleInput &&
      other.name == name &&
      other.enabled == enabled &&
      other.source_ == source_ &&
      other.kind == kind &&
      other.conditions == conditions &&
      other.geofence == geofence &&
      other.mode == mode &&
      other.onlyNew == onlyNew &&
      other.cooldown == cooldown &&
      other.messageTemplate == messageTemplate &&
      other.severity == severity &&
      other.enrich == enrich &&
      other.enrichRadiusKm == enrichRadiusKm &&
      other.enrichWindow == enrichWindow &&
      other.tags == tags &&
      other.sources == sources &&
      other.kinds == kinds &&
      other.bufferMeters == bufferMeters &&
      other.hysteresisMeters == hysteresisMeters &&
      other.near == near &&
      other.reappearAsEnter == reappearAsEnter &&
      other.cooldownScope == cooldownScope &&
      other.autoResolve == autoResolve &&
      other.maxAlertsPerHour == maxAlertsPerHour &&
      other.autoDisableOnStorm == autoDisableOnStorm &&
      other.activeFrom == activeFrom &&
      other.activeUntil == activeUntil &&
      other.activeWindow == activeWindow &&
      other.forecastHorizon == forecastHorizon &&
      other.escalationPolicyId == escalationPolicyId &&
      other.geofenceRef == geofenceRef &&
      other.weatherContext == weatherContext &&
      other.type == type &&
      other.window == window;

    @override
    int get hashCode =>
        name.hashCode +
        enabled.hashCode +
        (source_ == null ? 0 : source_.hashCode) +
        (kind == null ? 0 : kind.hashCode) +
        conditions.hashCode +
        (geofence == null ? 0 : geofence.hashCode) +
        mode.hashCode +
        onlyNew.hashCode +
        cooldown.hashCode +
        messageTemplate.hashCode +
        severity.hashCode +
        enrich.hashCode +
        enrichRadiusKm.hashCode +
        enrichWindow.hashCode +
        tags.hashCode +
        sources.hashCode +
        kinds.hashCode +
        (bufferMeters == null ? 0 : bufferMeters.hashCode) +
        hysteresisMeters.hashCode +
        (near == null ? 0 : near.hashCode) +
        reappearAsEnter.hashCode +
        cooldownScope.hashCode +
        (autoResolve == null ? 0 : autoResolve.hashCode) +
        (maxAlertsPerHour == null ? 0 : maxAlertsPerHour.hashCode) +
        autoDisableOnStorm.hashCode +
        (activeFrom == null ? 0 : activeFrom.hashCode) +
        (activeUntil == null ? 0 : activeUntil.hashCode) +
        (activeWindow == null ? 0 : activeWindow.hashCode) +
        (forecastHorizon == null ? 0 : forecastHorizon.hashCode) +
        (escalationPolicyId == null ? 0 : escalationPolicyId.hashCode) +
        (geofenceRef == null ? 0 : geofenceRef.hashCode) +
        weatherContext.hashCode +
        type.hashCode +
        (window == null ? 0 : window.hashCode);

  factory TriggerRuleInput.fromJson(Map<String, dynamic> json) => _$TriggerRuleInputFromJson(json);

  Map<String, dynamic> toJson() => _$TriggerRuleInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// match: every matching non-HEARTBEAT version. enter: matches and the previous version did not (or was a tombstone, or is new). exit: the previous version matched and this one does not or is a tombstone. Edge modes need a geofence, near or a condition.
enum TriggerRuleInputModeEnum {
@JsonValue(r'match')
match(r'match'),
@JsonValue(r'enter')
enter(r'enter'),
@JsonValue(r'exit')
exit(r'exit');

const TriggerRuleInputModeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum TriggerRuleInputSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const TriggerRuleInputSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


enum TriggerRuleInputCooldownScopeEnum {
@JsonValue(r'record')
record(r'record'),
@JsonValue(r'rule')
rule(r'rule');

const TriggerRuleInputCooldownScopeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Compute `alert.context.weather` for this rule's geolocated alerts (global setting `weather.alertContext`)
enum TriggerRuleInputWeatherContextEnum {
@JsonValue(r'auto')
auto(r'auto'),
@JsonValue(r'off')
off(r'off');

const TriggerRuleInputWeatherContextEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Rule type; existing rules are `record`. `onlyNew`, `mode` and `reappearAsEnter` are rejected for windowed types (400 WINDOW_RULE_INVALID).
enum TriggerRuleInputTypeEnum {
@JsonValue(r'record')
record(r'record'),
@JsonValue(r'count')
count(r'count'),
@JsonValue(r'absence')
absence(r'absence'),
@JsonValue(r'anomaly')
anomaly(r'anomaly');

const TriggerRuleInputTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


