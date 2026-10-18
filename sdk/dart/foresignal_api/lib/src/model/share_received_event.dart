//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'share_received_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ShareReceivedEvent {
  /// Returns a new [ShareReceivedEvent] instance.
  ShareReceivedEvent({

    required  this.ref,

     this.envelope,

    required  this.role,

    required  this.sharedBy,

     this.message,

    required  this.at,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'envelope',
    required: false,
    includeIfNull: false,
  )


  final ObjectEnvelope? envelope;



  @JsonKey(
    
    name: r'role',
    required: true,
    includeIfNull: false,
  )


  final ShareReceivedEventRoleEnum role;



  @JsonKey(
    
    name: r'sharedBy',
    required: true,
    includeIfNull: false,
  )


  final PrincipalSummary sharedBy;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ShareReceivedEvent &&
      other.ref == ref &&
      other.envelope == envelope &&
      other.role == role &&
      other.sharedBy == sharedBy &&
      other.message == message &&
      other.at == at;

    @override
    int get hashCode =>
        ref.hashCode +
        envelope.hashCode +
        role.hashCode +
        sharedBy.hashCode +
        (message == null ? 0 : message.hashCode) +
        at.hashCode;

  factory ShareReceivedEvent.fromJson(Map<String, dynamic> json) => _$ShareReceivedEventFromJson(json);

  Map<String, dynamic> toJson() => _$ShareReceivedEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ShareReceivedEventRoleEnum {
@JsonValue(r'viewer')
viewer(r'viewer'),
@JsonValue(r'commenter')
commenter(r'commenter'),
@JsonValue(r'editor')
editor(r'editor'),
@JsonValue(r'owner')
owner(r'owner');

const ShareReceivedEventRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


