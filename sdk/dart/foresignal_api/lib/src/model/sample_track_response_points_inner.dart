//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sample_status.dart';
import 'package:foresignal_api/src/model/sample_value.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sample_track_response_points_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SampleTrackResponsePointsInner {
  /// Returns a new [SampleTrackResponsePointsInner] instance.
  SampleTrackResponsePointsInner({

    required  this.lon,

    required  this.lat,

    required  this.time,

    required  this.status,

    required  this.values,
  });

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



  @JsonKey(
    
    name: r'time',
    required: true,
    includeIfNull: false,
  )


  final DateTime time;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final SampleStatus status;



  @JsonKey(
    
    name: r'values',
    required: true,
    includeIfNull: false,
  )


  final List<SampleValue> values;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SampleTrackResponsePointsInner &&
      other.lon == lon &&
      other.lat == lat &&
      other.time == time &&
      other.status == status &&
      other.values == values;

    @override
    int get hashCode =>
        lon.hashCode +
        lat.hashCode +
        time.hashCode +
        status.hashCode +
        values.hashCode;

  factory SampleTrackResponsePointsInner.fromJson(Map<String, dynamic> json) => _$SampleTrackResponsePointsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SampleTrackResponsePointsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

