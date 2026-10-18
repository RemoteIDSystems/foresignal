//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_policy_sampled.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestPolicySampled {
  /// Returns a new [IngestPolicySampled] instance.
  IngestPolicySampled({

    required  this.mode,

    required  this.minInterval,

     this.keyframe,

     this.minDeviationMeters,

     this.thresholds = false,

     this.edgeMinInterval,
  });

  @JsonKey(
    
    name: r'mode',
    required: true,
    includeIfNull: false,
  )


  final IngestPolicySampledModeEnum mode;



      /// Minimum Δt between SAMPLE versions (2 s ≤ minInterval ≤ keyframe). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'minInterval',
    required: true,
    includeIfNull: false,
  )


  final String minInterval;



      /// Keyframe interval on the TTL-anchor axis: a duration or `none` (only without a TTL). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'keyframe',
    required: false,
    includeIfNull: false,
  )


  final String? keyframe;



      /// Dead-reckoning deviation that triggers a SAMPLE
          // minimum: 0
  @JsonKey(
    
    name: r'minDeviationMeters',
    required: false,
    includeIfNull: false,
  )


  final num? minDeviationMeters;



      /// Also sample when a descriptor threshold is exceeded
  @JsonKey(
    defaultValue: false,
    name: r'thresholds',
    required: false,
    includeIfNull: false,
  )


  final bool? thresholds;



      /// Minimum Δt between TRIGGER_EDGE versions (≤ minInterval; default minInterval/2). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'edgeMinInterval',
    required: false,
    includeIfNull: false,
  )


  final String? edgeMinInterval;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestPolicySampled &&
      other.mode == mode &&
      other.minInterval == minInterval &&
      other.keyframe == keyframe &&
      other.minDeviationMeters == minDeviationMeters &&
      other.thresholds == thresholds &&
      other.edgeMinInterval == edgeMinInterval;

    @override
    int get hashCode =>
        mode.hashCode +
        minInterval.hashCode +
        keyframe.hashCode +
        minDeviationMeters.hashCode +
        thresholds.hashCode +
        (edgeMinInterval == null ? 0 : edgeMinInterval.hashCode);

  factory IngestPolicySampled.fromJson(Map<String, dynamic> json) => _$IngestPolicySampledFromJson(json);

  Map<String, dynamic> toJson() => _$IngestPolicySampledToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IngestPolicySampledModeEnum {
@JsonValue(r'SAMPLED')
SAMPLED(r'SAMPLED');

const IngestPolicySampledModeEnum(this.value);

final String value;

@override
String toString() => value;
}


