//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'param_spec.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ParamSpec {
  /// Returns a new [ParamSpec] instance.
  ParamSpec({

    required  this.name,

    required  this.type,

     this.description,

     this.default_,

     this.required_,

     this.allowed,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ParamSpecTypeEnum type;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'default',
    required: false,
    includeIfNull: false,
  )


  final Object? default_;



  @JsonKey(
    
    name: r'required',
    required: false,
    includeIfNull: false,
  )


  final bool? required_;



      /// When non-empty, the only accepted values. For type lan-url it lists the accepted URL schemes instead (069): empty means http, https, ws and wss; tcp (tcp://host:port, resolved once to a loopback, RFC 1918 or unique-local address and pinned) and udp (udp://bindHost:port, bindHost 0.0.0.0, :: or a local interface address, with the required string-list companion allowSenders of private IPs or CIDRs) are accepted only when listed; so are mqtt and mqtts (083: an operator's broker, mqtt://host:port or mqtts://host:port, pinned to a private address like tcp). The port of a tcp, udp, mqtt or mqtts URL is always explicit.
  @JsonKey(
    
    name: r'allowed',
    required: false,
    includeIfNull: false,
  )


  final List<String>? allowed;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ParamSpec &&
      other.name == name &&
      other.type == type &&
      other.description == description &&
      other.default_ == default_ &&
      other.required_ == required_ &&
      other.allowed == allowed;

    @override
    int get hashCode =>
        name.hashCode +
        type.hashCode +
        description.hashCode +
        (default_ == null ? 0 : default_.hashCode) +
        required_.hashCode +
        allowed.hashCode;

  factory ParamSpec.fromJson(Map<String, dynamic> json) => _$ParamSpecFromJson(json);

  Map<String, dynamic> toJson() => _$ParamSpecToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ParamSpecTypeEnum {
@JsonValue(r'string')
string(r'string'),
@JsonValue(r'integer')
integer(r'integer'),
@JsonValue(r'number')
number(r'number'),
@JsonValue(r'boolean')
boolean(r'boolean'),
@JsonValue(r'duration')
duration(r'duration'),
@JsonValue(r'bbox')
bbox(r'bbox'),
@JsonValue(r'string-list')
stringList(r'string-list'),
@JsonValue(r'lan-url')
lanUrl(r'lan-url'),
@JsonValue(r'inbox-path')
inboxPath(r'inbox-path'),
@JsonValue(r'json')
json(r'json');

const ParamSpecTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


