//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/event_stream_filter_chronicle.dart';
import 'package:foresignal_api/src/model/event_stream_filter_workspace.dart';
import 'package:foresignal_api/src/model/event_stream_filter_external.dart';
import 'package:foresignal_api/src/model/event_stream_changes_filter.dart';
import 'package:foresignal_api/src/model/event_stream_filter_alerts.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_stream_filter.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventStreamFilter {
  /// Returns a new [EventStreamFilter] instance.
  EventStreamFilter({

     this.topics,

     this.changes,

     this.alerts,

     this.notifications,

     this.workspace,

     this.chronicle,

     this.external_,
  });

      /// Subscribed topics (registered by the EventTopic SPI: `changes`, `alerts`, `notifications`, `system`, later more)
  @JsonKey(
    
    name: r'topics',
    required: false,
    includeIfNull: false,
  )


  final List<String>? topics;



  @JsonKey(
    
    name: r'changes',
    required: false,
    includeIfNull: false,
  )


  final EventStreamChangesFilter? changes;



  @JsonKey(
    
    name: r'alerts',
    required: false,
    includeIfNull: false,
  )


  final EventStreamFilterAlerts? alerts;



      /// Filter block for the `notifications` topic (no filters defined yet)
  @JsonKey(
    
    name: r'notifications',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? notifications;



  @JsonKey(
    
    name: r'workspace',
    required: false,
    includeIfNull: false,
  )


  final EventStreamFilterWorkspace? workspace;



  @JsonKey(
    
    name: r'chronicle',
    required: false,
    includeIfNull: false,
  )


  final EventStreamFilterChronicle? chronicle;



  @JsonKey(
    
    name: r'external',
    required: false,
    includeIfNull: false,
  )


  final EventStreamFilterExternal? external_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventStreamFilter &&
      other.topics == topics &&
      other.changes == changes &&
      other.alerts == alerts &&
      other.notifications == notifications &&
      other.workspace == workspace &&
      other.chronicle == chronicle &&
      other.external_ == external_;

    @override
    int get hashCode =>
        topics.hashCode +
        changes.hashCode +
        alerts.hashCode +
        notifications.hashCode +
        workspace.hashCode +
        chronicle.hashCode +
        external_.hashCode;

  factory EventStreamFilter.fromJson(Map<String, dynamic> json) => _$EventStreamFilterFromJson(json);

  Map<String, dynamic> toJson() => _$EventStreamFilterToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

