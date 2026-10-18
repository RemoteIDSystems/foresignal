//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_policy_all.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestPolicyAll {
  /// Returns a new [IngestPolicyAll] instance.
  IngestPolicyAll({

    required  this.mode,

     this.keyframe,
  });

  @JsonKey(
    
    name: r'mode',
    required: true,
    includeIfNull: false,
  )


  final IngestPolicyAllModeEnum mode;



      /// Keyframe interval on the TTL-anchor axis: a duration or `none` (only without a TTL). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'keyframe',
    required: false,
    includeIfNull: false,
  )


  final String? keyframe;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestPolicyAll &&
      other.mode == mode &&
      other.keyframe == keyframe;

    @override
    int get hashCode =>
        mode.hashCode +
        keyframe.hashCode;

  factory IngestPolicyAll.fromJson(Map<String, dynamic> json) => _$IngestPolicyAllFromJson(json);

  Map<String, dynamic> toJson() => _$IngestPolicyAllToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IngestPolicyAllModeEnum {
@JsonValue(r'ALL')
ALL(r'ALL');

const IngestPolicyAllModeEnum(this.value);

final String value;

@override
String toString() => value;
}


