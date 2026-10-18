//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'window_state_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WindowStatePayload {
  /// Returns a new [WindowStatePayload] instance.
  WindowStatePayload({

    required  this.ruleId,

     this.name,

    required  this.type,

    required  this.groupKey,

    required  this.state,

     this.value,

     this.threshold,

     this.reason,

     this.detector,

     this.automated,
  });

  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final WindowStatePayloadTypeEnum type;



  @JsonKey(
    
    name: r'groupKey',
    required: true,
    includeIfNull: false,
  )


  final String groupKey;



  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final WindowStatePayloadStateEnum state;



  @JsonKey(
    
    name: r'value',
    required: false,
    includeIfNull: false,
  )


  final num? value;



  @JsonKey(
    
    name: r'threshold',
    required: false,
    includeIfNull: false,
  )


  final num? threshold;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;



  @JsonKey(
    
    name: r'detector',
    required: false,
    includeIfNull: false,
  )


  final DetectorRef? detector;



  @JsonKey(
    
    name: r'automated',
    required: false,
    includeIfNull: false,
  )


  final bool? automated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WindowStatePayload &&
      other.ruleId == ruleId &&
      other.name == name &&
      other.type == type &&
      other.groupKey == groupKey &&
      other.state == state &&
      other.value == value &&
      other.threshold == threshold &&
      other.reason == reason &&
      other.detector == detector &&
      other.automated == automated;

    @override
    int get hashCode =>
        ruleId.hashCode +
        name.hashCode +
        type.hashCode +
        groupKey.hashCode +
        state.hashCode +
        (value == null ? 0 : value.hashCode) +
        (threshold == null ? 0 : threshold.hashCode) +
        (reason == null ? 0 : reason.hashCode) +
        detector.hashCode +
        automated.hashCode;

  factory WindowStatePayload.fromJson(Map<String, dynamic> json) => _$WindowStatePayloadFromJson(json);

  Map<String, dynamic> toJson() => _$WindowStatePayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum WindowStatePayloadTypeEnum {
@JsonValue(r'count')
count(r'count'),
@JsonValue(r'absence')
absence(r'absence'),
@JsonValue(r'anomaly')
anomaly(r'anomaly');

const WindowStatePayloadTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum WindowStatePayloadStateEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'clear')
clear(r'clear'),
@JsonValue(r'unknown')
unknown(r'unknown'),
@JsonValue(r'learning')
learning(r'learning');

const WindowStatePayloadStateEnum(this.value);

final String value;

@override
String toString() => value;
}


