//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sink_type.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SinkType {
  /// Returns a new [SinkType] instance.
  SinkType({

    required  this.id,

    required  this.name,

     this.description,

    required  this.kind,

    required  this.modes,

    required  this.configSchema,

    required  this.secretFields,

     this.defaultMaxPerSecond,
  });

      /// federation-push is added by 113 (edge relay)
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final SinkTypeIdEnum id;



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



      /// CHANGE_FEED: ordered at-least-once delivery from a durable cursor; STATE_MIRROR: external copy of current state
  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final SinkTypeKindEnum kind;



      /// PUSH: Foresignal connects out; SERVE: Foresignal listens on a LAN address
  @JsonKey(
    
    name: r'modes',
    required: true,
    includeIfNull: false,
  )


  final List<SinkTypeModesEnum> modes;



      /// JSON Schema 2020-12 of the type's `config`
  @JsonKey(
    
    name: r'configSchema',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> configSchema;



      /// Write-only secret settings
  @JsonKey(
    
    name: r'secretFields',
    required: true,
    includeIfNull: false,
  )


  final List<String> secretFields;



      /// 50 for PUSH, null (unlimited) for SERVE
  @JsonKey(
    
    name: r'defaultMaxPerSecond',
    required: false,
    includeIfNull: false,
  )


  final int? defaultMaxPerSecond;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SinkType &&
      other.id == id &&
      other.name == name &&
      other.description == description &&
      other.kind == kind &&
      other.modes == modes &&
      other.configSchema == configSchema &&
      other.secretFields == secretFields &&
      other.defaultMaxPerSecond == defaultMaxPerSecond;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        description.hashCode +
        kind.hashCode +
        modes.hashCode +
        configSchema.hashCode +
        secretFields.hashCode +
        (defaultMaxPerSecond == null ? 0 : defaultMaxPerSecond.hashCode);

  factory SinkType.fromJson(Map<String, dynamic> json) => _$SinkTypeFromJson(json);

  Map<String, dynamic> toJson() => _$SinkTypeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// federation-push is added by 113 (edge relay)
enum SinkTypeIdEnum {
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

const SinkTypeIdEnum(this.value);

final String value;

@override
String toString() => value;
}


/// CHANGE_FEED: ordered at-least-once delivery from a durable cursor; STATE_MIRROR: external copy of current state
enum SinkTypeKindEnum {
@JsonValue(r'CHANGE_FEED')
CHANGE_FEED(r'CHANGE_FEED'),
@JsonValue(r'STATE_MIRROR')
STATE_MIRROR(r'STATE_MIRROR');

const SinkTypeKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SinkTypeModesEnum {
@JsonValue(r'PUSH')
PUSH(r'PUSH'),
@JsonValue(r'SERVE')
SERVE(r'SERVE');

const SinkTypeModesEnum(this.value);

final String value;

@override
String toString() => value;
}


