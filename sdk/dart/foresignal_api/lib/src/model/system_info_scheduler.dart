//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_scheduler.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoScheduler {
  /// Returns a new [SystemInfoScheduler] instance.
  SystemInfoScheduler({

     this.running,

     this.scheduledSources,

     this.activeFetches,

     this.activeStreams,

     this.ingestQueueDepth,
  });

  @JsonKey(
    
    name: r'running',
    required: false,
    includeIfNull: false,
  )


  final bool? running;



  @JsonKey(
    
    name: r'scheduledSources',
    required: false,
    includeIfNull: false,
  )


  final int? scheduledSources;



  @JsonKey(
    
    name: r'activeFetches',
    required: false,
    includeIfNull: false,
  )


  final int? activeFetches;



  @JsonKey(
    
    name: r'activeStreams',
    required: false,
    includeIfNull: false,
  )


  final int? activeStreams;



  @JsonKey(
    
    name: r'ingestQueueDepth',
    required: false,
    includeIfNull: false,
  )


  final int? ingestQueueDepth;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoScheduler &&
      other.running == running &&
      other.scheduledSources == scheduledSources &&
      other.activeFetches == activeFetches &&
      other.activeStreams == activeStreams &&
      other.ingestQueueDepth == ingestQueueDepth;

    @override
    int get hashCode =>
        running.hashCode +
        scheduledSources.hashCode +
        activeFetches.hashCode +
        activeStreams.hashCode +
        ingestQueueDepth.hashCode;

  factory SystemInfoScheduler.fromJson(Map<String, dynamic> json) => _$SystemInfoSchedulerFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoSchedulerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

