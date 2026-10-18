//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sink_filter.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sink.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Sink {
  /// Returns a new [Sink] instance.
  Sink({

    required  this.id,

    required  this.type,

    required  this.kind,

    required  this.mode,

    required  this.name,

    required  this.enabled,

    required  this.config,

     this.secretsSet,

    required  this.filter,

     this.state,

    required  this.createdAt,

    required  this.updatedAt,
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


  final SinkTypeEnum type;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final SinkKindEnum kind;



  @JsonKey(
    
    name: r'mode',
    required: true,
    includeIfNull: false,
  )


  final SinkModeEnum mode;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'config',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> config;



      /// Names of secret fields that have a stored value (values are never returned)
  @JsonKey(
    
    name: r'secretsSet',
    required: false,
    includeIfNull: false,
  )


  final List<String>? secretsSet;



  @JsonKey(
    
    name: r'filter',
    required: true,
    includeIfNull: false,
  )


  final SinkFilter filter;



  @JsonKey(
    
    name: r'state',
    required: false,
    includeIfNull: false,
  )


  final SinkStateEnum? state;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is Sink &&
      other.id == id &&
      other.type == type &&
      other.kind == kind &&
      other.mode == mode &&
      other.name == name &&
      other.enabled == enabled &&
      other.config == config &&
      other.secretsSet == secretsSet &&
      other.filter == filter &&
      other.state == state &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        kind.hashCode +
        mode.hashCode +
        name.hashCode +
        enabled.hashCode +
        config.hashCode +
        secretsSet.hashCode +
        filter.hashCode +
        (state == null ? 0 : state.hashCode) +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory Sink.fromJson(Map<String, dynamic> json) => _$SinkFromJson(json);

  Map<String, dynamic> toJson() => _$SinkToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SinkTypeEnum {
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

const SinkTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SinkKindEnum {
@JsonValue(r'CHANGE_FEED')
CHANGE_FEED(r'CHANGE_FEED'),
@JsonValue(r'STATE_MIRROR')
STATE_MIRROR(r'STATE_MIRROR');

const SinkKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SinkModeEnum {
@JsonValue(r'PUSH')
PUSH(r'PUSH'),
@JsonValue(r'SERVE')
SERVE(r'SERVE');

const SinkModeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SinkStateEnum {
@JsonValue(r'RUNNING')
RUNNING(r'RUNNING'),
@JsonValue(r'RESYNCING')
RESYNCING(r'RESYNCING'),
@JsonValue(r'FAILING')
FAILING(r'FAILING'),
@JsonValue(r'STOPPED')
STOPPED(r'STOPPED');

const SinkStateEnum(this.value);

final String value;

@override
String toString() => value;
}


