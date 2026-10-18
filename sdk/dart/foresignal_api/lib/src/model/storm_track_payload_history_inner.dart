//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'storm_track_payload_history_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StormTrackPayloadHistoryInner {
  /// Returns a new [StormTrackPayloadHistoryInner] instance.
  StormTrackPayloadHistoryInner({

    required  this.time,

    required  this.lon,

    required  this.lat,
  });

  @JsonKey(
    
    name: r'time',
    required: true,
    includeIfNull: false,
  )


  final DateTime time;



  @JsonKey(
    
    name: r'lon',
    required: true,
    includeIfNull: false,
  )


  final num lon;



  @JsonKey(
    
    name: r'lat',
    required: true,
    includeIfNull: false,
  )


  final num lat;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StormTrackPayloadHistoryInner &&
      other.time == time &&
      other.lon == lon &&
      other.lat == lat;

    @override
    int get hashCode =>
        time.hashCode +
        lon.hashCode +
        lat.hashCode;

  factory StormTrackPayloadHistoryInner.fromJson(Map<String, dynamic> json) => _$StormTrackPayloadHistoryInnerFromJson(json);

  Map<String, dynamic> toJson() => _$StormTrackPayloadHistoryInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

