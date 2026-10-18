//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'client_presence_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ClientPresenceInput {
  /// Returns a new [ClientPresenceInput] instance.
  ClientPresenceInput({

    required  this.kind,

    required  this.state,

     this.ttlSeconds = 120,
  });

  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final ClientPresenceInputKindEnum kind;



  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final ClientPresenceInputStateEnum state;



          // minimum: 1
          // maximum: 300
  @JsonKey(
    defaultValue: 120,
    name: r'ttlSeconds',
    required: false,
    includeIfNull: false,
  )


  final int? ttlSeconds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ClientPresenceInput &&
      other.kind == kind &&
      other.state == state &&
      other.ttlSeconds == ttlSeconds;

    @override
    int get hashCode =>
        kind.hashCode +
        state.hashCode +
        ttlSeconds.hashCode;

  factory ClientPresenceInput.fromJson(Map<String, dynamic> json) => _$ClientPresenceInputFromJson(json);

  Map<String, dynamic> toJson() => _$ClientPresenceInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ClientPresenceInputKindEnum {
@JsonValue(r'desktop')
desktop(r'desktop'),
@JsonValue(r'mobile')
mobile(r'mobile'),
@JsonValue(r'web')
web(r'web');

const ClientPresenceInputKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ClientPresenceInputStateEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'idle')
idle(r'idle'),
@JsonValue(r'background')
background(r'background');

const ClientPresenceInputStateEnum(this.value);

final String value;

@override
String toString() => value;
}


