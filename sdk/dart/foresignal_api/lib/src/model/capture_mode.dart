//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'capture_mode.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CaptureMode {
  /// Returns a new [CaptureMode] instance.
  CaptureMode({

    required  this.mode,

     this.interval,

     this.everyNthFrame,
  });

      /// `full` every matching version; `downsampled` one per entity per interval; `events-only` versions whose persist reason is FIRST, SIGNIFICANT, REAPPEAR, TOMBSTONE or TRIGGER_EDGE; `latest-per-interval` the latest per entity per interval; `rollup-only` counts per region and hour, no records; `frames` every Nth raster frame
  @JsonKey(
    
    name: r'mode',
    required: true,
    includeIfNull: false,
  )


  final CaptureModeModeEnum mode;



      /// For `downsampled` (default PT60S per entity) and `latest-per-interval`
  @JsonKey(
    
    name: r'interval',
    required: false,
    includeIfNull: false,
  )


  final String? interval;



      /// For `frames`
          // minimum: 1
  @JsonKey(
    
    name: r'everyNthFrame',
    required: false,
    includeIfNull: false,
  )


  final int? everyNthFrame;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CaptureMode &&
      other.mode == mode &&
      other.interval == interval &&
      other.everyNthFrame == everyNthFrame;

    @override
    int get hashCode =>
        mode.hashCode +
        interval.hashCode +
        everyNthFrame.hashCode;

  factory CaptureMode.fromJson(Map<String, dynamic> json) => _$CaptureModeFromJson(json);

  Map<String, dynamic> toJson() => _$CaptureModeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `full` every matching version; `downsampled` one per entity per interval; `events-only` versions whose persist reason is FIRST, SIGNIFICANT, REAPPEAR, TOMBSTONE or TRIGGER_EDGE; `latest-per-interval` the latest per entity per interval; `rollup-only` counts per region and hour, no records; `frames` every Nth raster frame
enum CaptureModeModeEnum {
@JsonValue(r'full')
full(r'full'),
@JsonValue(r'downsampled')
downsampled(r'downsampled'),
@JsonValue(r'events-only')
eventsOnly(r'events-only'),
@JsonValue(r'latest-per-interval')
latestPerInterval(r'latest-per-interval'),
@JsonValue(r'rollup-only')
rollupOnly(r'rollup-only'),
@JsonValue(r'frames')
frames(r'frames');

const CaptureModeModeEnum(this.value);

final String value;

@override
String toString() => value;
}


