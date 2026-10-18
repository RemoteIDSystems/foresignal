//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/model_disagreement.dart';
import 'package:foresignal_api/src/model/sample_position.dart';
import 'package:foresignal_api/src/model/sample_value.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sample_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SampleResponse {
  /// Returns a new [SampleResponse] instance.
  SampleResponse({

    required  this.lon,

    required  this.lat,

     this.placeId,

     this.timezone,

     this.time,

    required  this.serverTime,

    required  this.values,

     this.disagreement,

     this.position,
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
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



  @JsonKey(
    
    name: r'timezone',
    required: false,
    includeIfNull: false,
  )


  final String? timezone;



      /// Requested time expression
  @JsonKey(
    
    name: r'time',
    required: false,
    includeIfNull: false,
  )


  final String? time;



  @JsonKey(
    
    name: r'serverTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime serverTime;



  @JsonKey(
    
    name: r'values',
    required: true,
    includeIfNull: false,
  )


  final List<SampleValue> values;



      /// 105: when several model products are sampled
  @JsonKey(
    
    name: r'disagreement',
    required: false,
    includeIfNull: false,
  )


  final ModelDisagreement? disagreement;



      /// 106: present with `record=`
  @JsonKey(
    
    name: r'position',
    required: false,
    includeIfNull: false,
  )


  final SamplePosition? position;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SampleResponse &&
      other.lon == lon &&
      other.lat == lat &&
      other.placeId == placeId &&
      other.timezone == timezone &&
      other.time == time &&
      other.serverTime == serverTime &&
      other.values == values &&
      other.disagreement == disagreement &&
      other.position == position;

    @override
    int get hashCode =>
        lon.hashCode +
        lat.hashCode +
        (placeId == null ? 0 : placeId.hashCode) +
        timezone.hashCode +
        time.hashCode +
        serverTime.hashCode +
        values.hashCode +
        (disagreement == null ? 0 : disagreement.hashCode) +
        (position == null ? 0 : position.hashCode);

  factory SampleResponse.fromJson(Map<String, dynamic> json) => _$SampleResponseFromJson(json);

  Map<String, dynamic> toJson() => _$SampleResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

