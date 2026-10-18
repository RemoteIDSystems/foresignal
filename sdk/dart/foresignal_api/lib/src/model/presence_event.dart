//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/presence_event_clients_inner.dart';
import 'package:foresignal_api/src/model/presence_event_change.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'presence_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PresenceEvent {
  /// Returns a new [PresenceEvent] instance.
  PresenceEvent({

    required  this.workspaceId,

    required  this.clients,

     this.change,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'workspaceId',
    required: true,
    includeIfNull: false,
  )


  final String workspaceId;



  @JsonKey(
    
    name: r'clients',
    required: true,
    includeIfNull: false,
  )


  final List<PresenceEventClientsInner> clients;



  @JsonKey(
    
    name: r'change',
    required: false,
    includeIfNull: false,
  )


  final PresenceEventChange? change;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PresenceEvent &&
      other.workspaceId == workspaceId &&
      other.clients == clients &&
      other.change == change &&
      other.at == at;

    @override
    int get hashCode =>
        workspaceId.hashCode +
        clients.hashCode +
        (change == null ? 0 : change.hashCode) +
        at.hashCode;

  factory PresenceEvent.fromJson(Map<String, dynamic> json) => _$PresenceEventFromJson(json);

  Map<String, dynamic> toJson() => _$PresenceEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

