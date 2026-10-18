//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'timeline_tick_frames_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TimelineTickFramesInner {
  /// Returns a new [TimelineTickFramesInner] instance.
  TimelineTickFramesInner({

    required  this.productId,

     this.frameKey,

    required  this.time,

     this.run,

     this.step,

    required  this.tiles,

    required  this.availability,
  });

  @JsonKey(
    
    name: r'productId',
    required: true,
    includeIfNull: false,
  )


  final String productId;



      /// Null for external entries
  @JsonKey(
    
    name: r'frameKey',
    required: false,
    includeIfNull: false,
  )


  final String? frameKey;



      /// Valid time of the chosen frame (within maxGap of the tick)
  @JsonKey(
    
    name: r'time',
    required: true,
    includeIfNull: false,
  )


  final DateTime time;



  @JsonKey(
    
    name: r'run',
    required: false,
    includeIfNull: false,
  )


  final String? run;



  @JsonKey(
    
    name: r'step',
    required: false,
    includeIfNull: false,
  )


  final int? step;



      /// Expanded tile template
  @JsonKey(
    
    name: r'tiles',
    required: true,
    includeIfNull: false,
  )


  final String tiles;



      /// `unverified` for synthesized external ticks
  @JsonKey(
    
    name: r'availability',
    required: true,
    includeIfNull: false,
  )


  final TimelineTickFramesInnerAvailabilityEnum availability;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TimelineTickFramesInner &&
      other.productId == productId &&
      other.frameKey == frameKey &&
      other.time == time &&
      other.run == run &&
      other.step == step &&
      other.tiles == tiles &&
      other.availability == availability;

    @override
    int get hashCode =>
        productId.hashCode +
        (frameKey == null ? 0 : frameKey.hashCode) +
        time.hashCode +
        (run == null ? 0 : run.hashCode) +
        (step == null ? 0 : step.hashCode) +
        tiles.hashCode +
        availability.hashCode;

  factory TimelineTickFramesInner.fromJson(Map<String, dynamic> json) => _$TimelineTickFramesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$TimelineTickFramesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `unverified` for synthesized external ticks
enum TimelineTickFramesInnerAvailabilityEnum {
@JsonValue(r'available')
available(r'available'),
@JsonValue(r'unverified')
unverified(r'unverified');

const TimelineTickFramesInnerAvailabilityEnum(this.value);

final String value;

@override
String toString() => value;
}


