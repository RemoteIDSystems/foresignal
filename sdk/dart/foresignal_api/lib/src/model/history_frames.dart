//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/frame_item.dart';
import 'package:foresignal_api/src/model/history_frame.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'history_frames.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HistoryFrames {
  /// Returns a new [HistoryFrames] instance.
  HistoryFrames({

    required  this.asOf,

    required  this.step,

    required  this.watermarkSeq,

     this.horizon,

     this.approximate,

    required  this.keyframe,

    required  this.frames,
  });

  @JsonKey(
    
    name: r'asOf',
    required: true,
    includeIfNull: false,
  )


  final DateTime asOf;



  @JsonKey(
    
    name: r'step',
    required: true,
    includeIfNull: false,
  )


  final String step;



  @JsonKey(
    
    name: r'watermarkSeq',
    required: true,
    includeIfNull: false,
  )


  final int watermarkSeq;



  @JsonKey(
    
    name: r'horizon',
    required: false,
    includeIfNull: false,
  )


  final DateTime? horizon;



  @JsonKey(
    
    name: r'approximate',
    required: false,
    includeIfNull: false,
  )


  final bool? approximate;



  @JsonKey(
    
    name: r'keyframe',
    required: true,
    includeIfNull: false,
  )


  final List<FrameItem> keyframe;



  @JsonKey(
    
    name: r'frames',
    required: true,
    includeIfNull: false,
  )


  final List<HistoryFrame> frames;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HistoryFrames &&
      other.asOf == asOf &&
      other.step == step &&
      other.watermarkSeq == watermarkSeq &&
      other.horizon == horizon &&
      other.approximate == approximate &&
      other.keyframe == keyframe &&
      other.frames == frames;

    @override
    int get hashCode =>
        asOf.hashCode +
        step.hashCode +
        watermarkSeq.hashCode +
        (horizon == null ? 0 : horizon.hashCode) +
        approximate.hashCode +
        keyframe.hashCode +
        frames.hashCode;

  factory HistoryFrames.fromJson(Map<String, dynamic> json) => _$HistoryFramesFromJson(json);

  Map<String, dynamic> toJson() => _$HistoryFramesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

