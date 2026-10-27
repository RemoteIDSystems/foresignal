//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'resource_thresholds_collector_backlog.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ResourceThresholdsCollectorBacklog {
  /// Returns a new [ResourceThresholdsCollectorBacklog] instance.
  ResourceThresholdsCollectorBacklog({

     this.triggerLagSeq,

     this.waitingPulls,

     this.sustainedSeconds,
  });

      /// Trigger lag bound in sequence numbers; null is off
          // minimum: 1
  @JsonKey(
    
    name: r'triggerLagSeq',
    required: false,
    includeIfNull: false,
  )


  final int? triggerLagSeq;



      /// Waiting-fetch bound; null is off
          // minimum: 1
  @JsonKey(
    
    name: r'waitingPulls',
    required: false,
    includeIfNull: false,
  )


  final int? waitingPulls;



      /// How long a bound must be exceeded (default 300)
          // minimum: 30
          // maximum: 86400
  @JsonKey(
    
    name: r'sustainedSeconds',
    required: false,
    includeIfNull: false,
  )


  final int? sustainedSeconds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ResourceThresholdsCollectorBacklog &&
      other.triggerLagSeq == triggerLagSeq &&
      other.waitingPulls == waitingPulls &&
      other.sustainedSeconds == sustainedSeconds;

    @override
    int get hashCode =>
        (triggerLagSeq == null ? 0 : triggerLagSeq.hashCode) +
        (waitingPulls == null ? 0 : waitingPulls.hashCode) +
        sustainedSeconds.hashCode;

  factory ResourceThresholdsCollectorBacklog.fromJson(Map<String, dynamic> json) => _$ResourceThresholdsCollectorBacklogFromJson(json);

  Map<String, dynamic> toJson() => _$ResourceThresholdsCollectorBacklogToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

