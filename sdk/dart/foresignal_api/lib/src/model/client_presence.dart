//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'client_presence.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ClientPresence {
  /// Returns a new [ClientPresence] instance.
  ClientPresence({

    required  this.deviceKey,

    required  this.kind,

    required  this.state,

    required  this.expiresAt,

     this.updatedAt,
  });

  @JsonKey(
    
    name: r'deviceKey',
    required: true,
    includeIfNull: false,
  )


  final String deviceKey;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final ClientPresenceKindEnum kind;



  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final ClientPresenceStateEnum state;



  @JsonKey(
    
    name: r'expiresAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime expiresAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ClientPresence &&
      other.deviceKey == deviceKey &&
      other.kind == kind &&
      other.state == state &&
      other.expiresAt == expiresAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        deviceKey.hashCode +
        kind.hashCode +
        state.hashCode +
        expiresAt.hashCode +
        updatedAt.hashCode;

  factory ClientPresence.fromJson(Map<String, dynamic> json) => _$ClientPresenceFromJson(json);

  Map<String, dynamic> toJson() => _$ClientPresenceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ClientPresenceKindEnum {
@JsonValue(r'desktop')
desktop(r'desktop'),
@JsonValue(r'mobile')
mobile(r'mobile'),
@JsonValue(r'web')
web(r'web');

const ClientPresenceKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ClientPresenceStateEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'idle')
idle(r'idle'),
@JsonValue(r'background')
background(r'background');

const ClientPresenceStateEnum(this.value);

final String value;

@override
String toString() => value;
}


