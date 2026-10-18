//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/uv_forecast_payload_hours_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'uv_forecast_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UvForecastPayload {
  /// Returns a new [UvForecastPayload] instance.
  UvForecastPayload({

    required  this.zip,

    required  this.hours,

     this.maxUv,

     this.maxAt,
  });

  @JsonKey(
    
    name: r'zip',
    required: true,
    includeIfNull: false,
  )


  final String zip;



  @JsonKey(
    
    name: r'hours',
    required: true,
    includeIfNull: false,
  )


  final List<UvForecastPayloadHoursInner> hours;



  @JsonKey(
    
    name: r'maxUv',
    required: false,
    includeIfNull: false,
  )


  final num? maxUv;



  @JsonKey(
    
    name: r'maxAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? maxAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UvForecastPayload &&
      other.zip == zip &&
      other.hours == hours &&
      other.maxUv == maxUv &&
      other.maxAt == maxAt;

    @override
    int get hashCode =>
        zip.hashCode +
        hours.hashCode +
        (maxUv == null ? 0 : maxUv.hashCode) +
        (maxAt == null ? 0 : maxAt.hashCode);

  factory UvForecastPayload.fromJson(Map<String, dynamic> json) => _$UvForecastPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$UvForecastPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

