//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/storm_track_payload_lightning_jump.dart';
import 'package:foresignal_api/src/model/storm_track_payload_nowcast_inner.dart';
import 'package:foresignal_api/src/model/storm_track_payload_trend.dart';
import 'package:foresignal_api/src/model/storm_track_payload_cell.dart';
import 'package:foresignal_api/src/model/storm_track_payload_motion.dart';
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:foresignal_api/src/model/storm_track_payload_history_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'storm_track_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StormTrackPayload {
  /// Returns a new [StormTrackPayload] instance.
  StormTrackPayload({

    required  this.cell,

     this.history,

    required  this.motion,

     this.trend,

     this.lightningJump,

     this.nowcast,

    required  this.status,

     this.mergedInto,

     this.parentTrackId,

     this.detector,
  });

  @JsonKey(
    
    name: r'cell',
    required: true,
    includeIfNull: false,
  )


  final StormTrackPayloadCell cell;



  @JsonKey(
    
    name: r'history',
    required: false,
    includeIfNull: false,
  )


  final List<StormTrackPayloadHistoryInner>? history;



  @JsonKey(
    
    name: r'motion',
    required: true,
    includeIfNull: false,
  )


  final StormTrackPayloadMotion motion;



  @JsonKey(
    
    name: r'trend',
    required: false,
    includeIfNull: false,
  )


  final StormTrackPayloadTrend? trend;



  @JsonKey(
    
    name: r'lightningJump',
    required: false,
    includeIfNull: false,
  )


  final StormTrackPayloadLightningJump? lightningJump;



  @JsonKey(
    
    name: r'nowcast',
    required: false,
    includeIfNull: false,
  )


  final List<StormTrackPayloadNowcastInner>? nowcast;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final StormTrackPayloadStatusEnum status;



  @JsonKey(
    
    name: r'mergedInto',
    required: false,
    includeIfNull: false,
  )


  final String? mergedInto;



  @JsonKey(
    
    name: r'parentTrackId',
    required: false,
    includeIfNull: false,
  )


  final String? parentTrackId;



  @JsonKey(
    
    name: r'detector',
    required: false,
    includeIfNull: false,
  )


  final DetectorRef? detector;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StormTrackPayload &&
      other.cell == cell &&
      other.history == history &&
      other.motion == motion &&
      other.trend == trend &&
      other.lightningJump == lightningJump &&
      other.nowcast == nowcast &&
      other.status == status &&
      other.mergedInto == mergedInto &&
      other.parentTrackId == parentTrackId &&
      other.detector == detector;

    @override
    int get hashCode =>
        cell.hashCode +
        history.hashCode +
        motion.hashCode +
        trend.hashCode +
        (lightningJump == null ? 0 : lightningJump.hashCode) +
        nowcast.hashCode +
        status.hashCode +
        (mergedInto == null ? 0 : mergedInto.hashCode) +
        (parentTrackId == null ? 0 : parentTrackId.hashCode) +
        detector.hashCode;

  factory StormTrackPayload.fromJson(Map<String, dynamic> json) => _$StormTrackPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$StormTrackPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StormTrackPayloadStatusEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'ended')
ended(r'ended');

const StormTrackPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


