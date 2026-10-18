//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'storm_track_payload_nowcast_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StormTrackPayloadNowcastInner {
  /// Returns a new [StormTrackPayloadNowcastInner] instance.
  StormTrackPayloadNowcastInner({

    required  this.leadMinutes,

    required  this.method,
  });

  @JsonKey(
    
    name: r'leadMinutes',
    required: true,
    includeIfNull: false,
  )


  final StormTrackPayloadNowcastInnerLeadMinutesEnum leadMinutes;



  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final StormTrackPayloadNowcastInnerMethodEnum method;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StormTrackPayloadNowcastInner &&
      other.leadMinutes == leadMinutes &&
      other.method == method;

    @override
    int get hashCode =>
        leadMinutes.hashCode +
        method.hashCode;

  factory StormTrackPayloadNowcastInner.fromJson(Map<String, dynamic> json) => _$StormTrackPayloadNowcastInnerFromJson(json);

  Map<String, dynamic> toJson() => _$StormTrackPayloadNowcastInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StormTrackPayloadNowcastInnerLeadMinutesEnum {
@JsonValue(15)
number15('15'),
@JsonValue(30)
number30('30'),
@JsonValue(60)
number60('60');

const StormTrackPayloadNowcastInnerLeadMinutesEnum(this.value);

final String value;

@override
String toString() => value;
}


enum StormTrackPayloadNowcastInnerMethodEnum {
@JsonValue(r'linear-advection')
linearAdvection(r'linear-advection');

const StormTrackPayloadNowcastInnerMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


