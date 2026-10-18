//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_policy_latest.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestPolicyLatest {
  /// Returns a new [IngestPolicyLatest] instance.
  IngestPolicyLatest({

    required  this.mode,

     this.minInterval,

     this.keyframe,

     this.edgeMinInterval,
  });

  @JsonKey(
    
    name: r'mode',
    required: true,
    includeIfNull: false,
  )


  final IngestPolicyLatestModeEnum mode;



      /// Minimum interval (2 s ≤ minInterval ≤ keyframe). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'minInterval',
    required: false,
    includeIfNull: false,
  )


  final String? minInterval;



      /// Keyframe interval on the TTL-anchor axis: a duration or `none` (only without a TTL). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'keyframe',
    required: false,
    includeIfNull: false,
  )


  final String? keyframe;



      /// Minimum Δt between TRIGGER_EDGE versions. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'edgeMinInterval',
    required: false,
    includeIfNull: false,
  )


  final String? edgeMinInterval;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestPolicyLatest &&
      other.mode == mode &&
      other.minInterval == minInterval &&
      other.keyframe == keyframe &&
      other.edgeMinInterval == edgeMinInterval;

    @override
    int get hashCode =>
        mode.hashCode +
        (minInterval == null ? 0 : minInterval.hashCode) +
        keyframe.hashCode +
        (edgeMinInterval == null ? 0 : edgeMinInterval.hashCode);

  factory IngestPolicyLatest.fromJson(Map<String, dynamic> json) => _$IngestPolicyLatestFromJson(json);

  Map<String, dynamic> toJson() => _$IngestPolicyLatestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IngestPolicyLatestModeEnum {
@JsonValue(r'LATEST')
LATEST(r'LATEST');

const IngestPolicyLatestModeEnum(this.value);

final String value;

@override
String toString() => value;
}


