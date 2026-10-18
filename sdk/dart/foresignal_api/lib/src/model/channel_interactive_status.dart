//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/channel_interactive_status_chats_inner.dart';
import 'package:foresignal_api/src/model/health_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'channel_interactive_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChannelInteractiveStatus {
  /// Returns a new [ChannelInteractiveStatus] instance.
  ChannelInteractiveStatus({

    required  this.interactive,

     this.chatopsEnabled,

    required  this.connected,

    required  this.status,

     this.statusReason,

     this.lastPollAt,

     this.lastError,

     this.discoveryModeUntil,

    required  this.chats,
  });

  @JsonKey(
    
    name: r'interactive',
    required: true,
    includeIfNull: false,
  )


  final bool interactive;



      /// Global `chatops.enabled` kill switch
  @JsonKey(
    
    name: r'chatopsEnabled',
    required: false,
    includeIfNull: false,
  )


  final bool? chatopsEnabled;



  @JsonKey(
    
    name: r'connected',
    required: true,
    includeIfNull: false,
  )


  final bool connected;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final HealthStatus status;



  @JsonKey(
    
    name: r'statusReason',
    required: false,
    includeIfNull: false,
  )


  final String? statusReason;



  @JsonKey(
    
    name: r'lastPollAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastPollAt;



  @JsonKey(
    
    name: r'lastError',
    required: false,
    includeIfNull: false,
  )


  final String? lastError;



  @JsonKey(
    
    name: r'discoveryModeUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? discoveryModeUntil;



  @JsonKey(
    
    name: r'chats',
    required: true,
    includeIfNull: false,
  )


  final List<ChannelInteractiveStatusChatsInner> chats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChannelInteractiveStatus &&
      other.interactive == interactive &&
      other.chatopsEnabled == chatopsEnabled &&
      other.connected == connected &&
      other.status == status &&
      other.statusReason == statusReason &&
      other.lastPollAt == lastPollAt &&
      other.lastError == lastError &&
      other.discoveryModeUntil == discoveryModeUntil &&
      other.chats == chats;

    @override
    int get hashCode =>
        interactive.hashCode +
        chatopsEnabled.hashCode +
        connected.hashCode +
        status.hashCode +
        (statusReason == null ? 0 : statusReason.hashCode) +
        (lastPollAt == null ? 0 : lastPollAt.hashCode) +
        (lastError == null ? 0 : lastError.hashCode) +
        (discoveryModeUntil == null ? 0 : discoveryModeUntil.hashCode) +
        chats.hashCode;

  factory ChannelInteractiveStatus.fromJson(Map<String, dynamic> json) => _$ChannelInteractiveStatusFromJson(json);

  Map<String, dynamic> toJson() => _$ChannelInteractiveStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

