//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/taf_payload_forecast_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'taf_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TafPayload {
  /// Returns a new [TafPayload] instance.
  TafPayload({

    required  this.stationId,

    required  this.issueTime,

    required  this.validFrom,

    required  this.validTo,

     this.forecast,

    required  this.rawText,
  });

  @JsonKey(
    
    name: r'stationId',
    required: true,
    includeIfNull: false,
  )


  final String stationId;



  @JsonKey(
    
    name: r'issueTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime issueTime;



  @JsonKey(
    
    name: r'validFrom',
    required: true,
    includeIfNull: false,
  )


  final DateTime validFrom;



  @JsonKey(
    
    name: r'validTo',
    required: true,
    includeIfNull: false,
  )


  final DateTime validTo;



  @JsonKey(
    
    name: r'forecast',
    required: false,
    includeIfNull: false,
  )


  final List<TafPayloadForecastInner>? forecast;



  @JsonKey(
    
    name: r'rawText',
    required: true,
    includeIfNull: false,
  )


  final String rawText;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TafPayload &&
      other.stationId == stationId &&
      other.issueTime == issueTime &&
      other.validFrom == validFrom &&
      other.validTo == validTo &&
      other.forecast == forecast &&
      other.rawText == rawText;

    @override
    int get hashCode =>
        stationId.hashCode +
        issueTime.hashCode +
        validFrom.hashCode +
        validTo.hashCode +
        forecast.hashCode +
        rawText.hashCode;

  factory TafPayload.fromJson(Map<String, dynamic> json) => _$TafPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$TafPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

