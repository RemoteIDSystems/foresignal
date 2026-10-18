//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'presence_event_clients_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PresenceEventClientsInner {
  /// Returns a new [PresenceEventClientsInner] instance.
  PresenceEventClientsInner({

    required  this.clientId,

    required  this.clientKind,

     this.principal,

    required  this.connectedAt,
  });

  @JsonKey(
    
    name: r'clientId',
    required: true,
    includeIfNull: false,
  )


  final String clientId;



  @JsonKey(
    
    name: r'clientKind',
    required: true,
    includeIfNull: false,
  )


  final PresenceEventClientsInnerClientKindEnum clientKind;



  @JsonKey(
    
    name: r'principal',
    required: false,
    includeIfNull: false,
  )


  final String? principal;



  @JsonKey(
    
    name: r'connectedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime connectedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PresenceEventClientsInner &&
      other.clientId == clientId &&
      other.clientKind == clientKind &&
      other.principal == principal &&
      other.connectedAt == connectedAt;

    @override
    int get hashCode =>
        clientId.hashCode +
        clientKind.hashCode +
        (principal == null ? 0 : principal.hashCode) +
        connectedAt.hashCode;

  factory PresenceEventClientsInner.fromJson(Map<String, dynamic> json) => _$PresenceEventClientsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$PresenceEventClientsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PresenceEventClientsInnerClientKindEnum {
@JsonValue(r'globe')
globe(r'globe'),
@JsonValue(r'map')
map(r'map'),
@JsonValue(r'analyst')
analyst(r'analyst'),
@JsonValue(r'satellite')
satellite(r'satellite'),
@JsonValue(r'rf')
rf(r'rf'),
@JsonValue(r'mobile')
mobile(r'mobile'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'other')
other(r'other');

const PresenceEventClientsInnerClientKindEnum(this.value);

final String value;

@override
String toString() => value;
}


