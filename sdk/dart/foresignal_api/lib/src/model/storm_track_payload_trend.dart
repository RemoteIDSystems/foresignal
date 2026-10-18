//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'storm_track_payload_trend.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StormTrackPayloadTrend {
  /// Returns a new [StormTrackPayloadTrend] instance.
  StormTrackPayloadTrend({

     this.maxDbz,
  });

  @JsonKey(
    
    name: r'maxDbz',
    required: false,
    includeIfNull: false,
  )


  final StormTrackPayloadTrendMaxDbzEnum? maxDbz;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StormTrackPayloadTrend &&
      other.maxDbz == maxDbz;

    @override
    int get hashCode =>
        maxDbz.hashCode;

  factory StormTrackPayloadTrend.fromJson(Map<String, dynamic> json) => _$StormTrackPayloadTrendFromJson(json);

  Map<String, dynamic> toJson() => _$StormTrackPayloadTrendToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StormTrackPayloadTrendMaxDbzEnum {
@JsonValue(r'rising')
rising(r'rising'),
@JsonValue(r'steady')
steady(r'steady'),
@JsonValue(r'falling')
falling(r'falling');

const StormTrackPayloadTrendMaxDbzEnum(this.value);

final String value;

@override
String toString() => value;
}


