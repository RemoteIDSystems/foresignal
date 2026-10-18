//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sink_filter.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sink_create.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SinkCreate {
  /// Returns a new [SinkCreate] instance.
  SinkCreate({

    required  this.id,

    required  this.type,

    required  this.name,

     this.enabled = false,

    required  this.config,

     this.secrets,

     this.filter,
  });

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


  final SinkCreateTypeEnum type;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// Defaults to false
  @JsonKey(
    defaultValue: false,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



      /// Per-type settings validated against SinkType.configSchema. Common keys: `mode` (PUSH|SERVE where the type supports both), `bind` (default 127.0.0.1; loopback, RFC 1918, link-local, ULA, or 100.64.0.0/10 with `allowTailnet`), `port` (required for SERVE, no default), `allowAllInterfaces`, `maxClients` (default 8), `maxPerSecond`; broker and target URLs pass the outbound URL policy (035).
  @JsonKey(
    
    name: r'config',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> config;



      /// Write-only secrets per SinkType.secretFields (encrypted at rest, never returned)
  @JsonKey(
    
    name: r'secrets',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? secrets;



  @JsonKey(
    
    name: r'filter',
    required: false,
    includeIfNull: false,
  )


  final SinkFilter? filter;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SinkCreate &&
      other.id == id &&
      other.type == type &&
      other.name == name &&
      other.enabled == enabled &&
      other.config == config &&
      other.secrets == secrets &&
      other.filter == filter;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        name.hashCode +
        enabled.hashCode +
        config.hashCode +
        secrets.hashCode +
        filter.hashCode;

  factory SinkCreate.fromJson(Map<String, dynamic> json) => _$SinkCreateFromJson(json);

  Map<String, dynamic> toJson() => _$SinkCreateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SinkCreateTypeEnum {
@JsonValue(r'mqtt-state')
mqttState(r'mqtt-state'),
@JsonValue(r'cot')
cot(r'cot'),
@JsonValue(r'readsb-json')
readsbJson(r'readsb-json'),
@JsonValue(r'sbs')
sbs(r'sbs'),
@JsonValue(r'nmea-aivdm')
nmeaAivdm(r'nmea-aivdm'),
@JsonValue(r'aprs-is-serve')
aprsIsServe(r'aprs-is-serve'),
@JsonValue(r'cloudevents')
cloudevents(r'cloudevents'),
@JsonValue(r'federation-push')
federationPush(r'federation-push');

const SinkCreateTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


