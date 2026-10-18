//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ingest_policy_latest.dart';
import 'package:foresignal_api/src/model/ingest_policy_sampled.dart';
import 'package:foresignal_api/src/model/ingest_policy_all.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_policy.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestPolicy {
  /// Returns a new [IngestPolicy] instance.
  IngestPolicy({

    required  this.mode,

     this.keyframe,

    required  this.minInterval,

     this.minDeviationMeters,

     this.thresholds = false,

     this.edgeMinInterval,
  });

  @JsonKey(
    
    name: r'mode',
    required: true,
    includeIfNull: false,
  )


  final IngestPolicyModeEnum mode;



      /// Keyframe interval on the TTL-anchor axis: a duration or `none` (only without a TTL). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'keyframe',
    required: false,
    includeIfNull: false,
  )


  final String? keyframe;



      /// Minimum interval (2 s ≤ minInterval ≤ keyframe). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'minInterval',
    required: true,
    includeIfNull: true,
  )


  final String? minInterval;



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



      /// Minimum Δt between TRIGGER_EDGE versions. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'edgeMinInterval',
    required: false,
    includeIfNull: false,
  )


  final String? edgeMinInterval;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestPolicy &&
      other.mode == mode &&
      other.keyframe == keyframe &&
      other.minInterval == minInterval &&
      other.minDeviationMeters == minDeviationMeters &&
      other.thresholds == thresholds &&
      other.edgeMinInterval == edgeMinInterval;

    @override
    int get hashCode =>
        mode.hashCode +
        keyframe.hashCode +
        (minInterval == null ? 0 : minInterval.hashCode) +
        minDeviationMeters.hashCode +
        thresholds.hashCode +
        (edgeMinInterval == null ? 0 : edgeMinInterval.hashCode);

  factory IngestPolicy.fromJson(Map<String, dynamic> json) => _$IngestPolicyFromJson(json);

  Map<String, dynamic> toJson() => _$IngestPolicyToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IngestPolicyModeEnum {
@JsonValue(r'LATEST')
LATEST(r'LATEST');

const IngestPolicyModeEnum(this.value);

final String value;

@override
String toString() => value;
}


