//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'channel_airtime.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChannelAirtime {
  /// Returns a new [ChannelAirtime] instance.
  ChannelAirtime({

    required  this.windowSeconds,

    required  this.usedSeconds,

    required  this.limitSeconds,

    required  this.dutyCyclePercent,

    required  this.queued,

    required  this.foldedLastHour,

    required  this.droppedLastHour,
  });

  @JsonKey(
    
    name: r'windowSeconds',
    required: true,
    includeIfNull: false,
  )


  final int windowSeconds;



  @JsonKey(
    
    name: r'usedSeconds',
    required: true,
    includeIfNull: false,
  )


  final num usedSeconds;



  @JsonKey(
    
    name: r'limitSeconds',
    required: true,
    includeIfNull: false,
  )


  final num limitSeconds;



  @JsonKey(
    
    name: r'dutyCyclePercent',
    required: true,
    includeIfNull: false,
  )


  final num dutyCyclePercent;



  @JsonKey(
    
    name: r'queued',
    required: true,
    includeIfNull: false,
  )


  final int queued;



  @JsonKey(
    
    name: r'foldedLastHour',
    required: true,
    includeIfNull: false,
  )


  final int foldedLastHour;



  @JsonKey(
    
    name: r'droppedLastHour',
    required: true,
    includeIfNull: false,
  )


  final int droppedLastHour;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChannelAirtime &&
      other.windowSeconds == windowSeconds &&
      other.usedSeconds == usedSeconds &&
      other.limitSeconds == limitSeconds &&
      other.dutyCyclePercent == dutyCyclePercent &&
      other.queued == queued &&
      other.foldedLastHour == foldedLastHour &&
      other.droppedLastHour == droppedLastHour;

    @override
    int get hashCode =>
        windowSeconds.hashCode +
        usedSeconds.hashCode +
        limitSeconds.hashCode +
        dutyCyclePercent.hashCode +
        queued.hashCode +
        foldedLastHour.hashCode +
        droppedLastHour.hashCode;

  factory ChannelAirtime.fromJson(Map<String, dynamic> json) => _$ChannelAirtimeFromJson(json);

  Map<String, dynamic> toJson() => _$ChannelAirtimeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

