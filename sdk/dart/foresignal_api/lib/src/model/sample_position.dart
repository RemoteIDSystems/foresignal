//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sample_position.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SamplePosition {
  /// Returns a new [SamplePosition] instance.
  SamplePosition({

    required  this.lon,

    required  this.lat,

    required  this.observedAt,

    required  this.ageSeconds,
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
    
    name: r'observedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime observedAt;



  @JsonKey(
    
    name: r'ageSeconds',
    required: true,
    includeIfNull: false,
  )


  final int ageSeconds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SamplePosition &&
      other.lon == lon &&
      other.lat == lat &&
      other.observedAt == observedAt &&
      other.ageSeconds == ageSeconds;

    @override
    int get hashCode =>
        lon.hashCode +
        lat.hashCode +
        observedAt.hashCode +
        ageSeconds.hashCode;

  factory SamplePosition.fromJson(Map<String, dynamic> json) => _$SamplePositionFromJson(json);

  Map<String, dynamic> toJson() => _$SamplePositionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

