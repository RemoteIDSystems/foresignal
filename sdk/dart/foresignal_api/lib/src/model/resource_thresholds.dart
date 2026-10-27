//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/resource_thresholds_source_storm.dart';
import 'package:foresignal_api/src/model/resource_thresholds_collector_backlog.dart';
import 'package:foresignal_api/src/model/resource_threshold.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'resource_thresholds.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ResourceThresholds {
  /// Returns a new [ResourceThresholds] instance.
  ResourceThresholds({

     this.disk,

     this.memory,

     this.alertChannels,

     this.sourceStorm,

     this.collectorBacklog,
  });

  @JsonKey(
    
    name: r'disk',
    required: false,
    includeIfNull: false,
  )


  final ResourceThreshold? disk;



  @JsonKey(
    
    name: r'memory',
    required: false,
    includeIfNull: false,
  )


  final ResourceThreshold? memory;



      /// Channel ids notified on a breach, personal channels included, whatever their event filters. Empty uses every channel subscribed to `system` plus the personal channels of every active user with `audit.read`. An unknown id is a 400 that lists it.
  @JsonKey(
    
    name: r'alertChannels',
    required: false,
    includeIfNull: false,
  )


  final List<String>? alertChannels;



  @JsonKey(
    
    name: r'sourceStorm',
    required: false,
    includeIfNull: false,
  )


  final ResourceThresholdsSourceStorm? sourceStorm;



  @JsonKey(
    
    name: r'collectorBacklog',
    required: false,
    includeIfNull: false,
  )


  final ResourceThresholdsCollectorBacklog? collectorBacklog;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ResourceThresholds &&
      other.disk == disk &&
      other.memory == memory &&
      other.alertChannels == alertChannels &&
      other.sourceStorm == sourceStorm &&
      other.collectorBacklog == collectorBacklog;

    @override
    int get hashCode =>
        disk.hashCode +
        memory.hashCode +
        alertChannels.hashCode +
        (sourceStorm == null ? 0 : sourceStorm.hashCode) +
        (collectorBacklog == null ? 0 : collectorBacklog.hashCode);

  factory ResourceThresholds.fromJson(Map<String, dynamic> json) => _$ResourceThresholdsFromJson(json);

  Map<String, dynamic> toJson() => _$ResourceThresholdsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

