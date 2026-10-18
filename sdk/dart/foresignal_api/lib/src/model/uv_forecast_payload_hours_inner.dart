//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'uv_forecast_payload_hours_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UvForecastPayloadHoursInner {
  /// Returns a new [UvForecastPayloadHoursInner] instance.
  UvForecastPayloadHoursInner({

    required  this.time,

    required  this.uv,
  });

  @JsonKey(
    
    name: r'time',
    required: true,
    includeIfNull: false,
  )


  final DateTime time;



  @JsonKey(
    
    name: r'uv',
    required: true,
    includeIfNull: false,
  )


  final num uv;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UvForecastPayloadHoursInner &&
      other.time == time &&
      other.uv == uv;

    @override
    int get hashCode =>
        time.hashCode +
        uv.hashCode;

  factory UvForecastPayloadHoursInner.fromJson(Map<String, dynamic> json) => _$UvForecastPayloadHoursInnerFromJson(json);

  Map<String, dynamic> toJson() => _$UvForecastPayloadHoursInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

