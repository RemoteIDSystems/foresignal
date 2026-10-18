//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'storm_report_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StormReportPayload {
  /// Returns a new [StormReportPayload] instance.
  StormReportPayload({

    required  this.type,

     this.magnitude,

     this.magnitudeUnits,

    required  this.location,

     this.county,

     this.state,

     this.wfo,

     this.comments,

    required  this.title,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final StormReportPayloadTypeEnum type;



      /// Tornado rating string (EF0 to EF5), wind mph or hail inches as numbers; omitted when UNK
  @JsonKey(
    
    name: r'magnitude',
    required: false,
    includeIfNull: false,
  )


  final Object? magnitude;



      /// EF (tornado), mph (wind) or in (hail)
  @JsonKey(
    
    name: r'magnitudeUnits',
    required: false,
    includeIfNull: false,
  )


  final StormReportPayloadMagnitudeUnitsEnum? magnitudeUnits;



  @JsonKey(
    
    name: r'location',
    required: true,
    includeIfNull: false,
  )


  final String location;



  @JsonKey(
    
    name: r'county',
    required: false,
    includeIfNull: false,
  )


  final String? county;



  @JsonKey(
    
    name: r'state',
    required: false,
    includeIfNull: false,
  )


  final String? state;



  @JsonKey(
    
    name: r'wfo',
    required: false,
    includeIfNull: false,
  )


  final String? wfo;



      /// Untrusted upstream text, ≤ 500 chars
  @JsonKey(
    
    name: r'comments',
    required: false,
    includeIfNull: false,
  )


  final String? comments;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StormReportPayload &&
      other.type == type &&
      other.magnitude == magnitude &&
      other.magnitudeUnits == magnitudeUnits &&
      other.location == location &&
      other.county == county &&
      other.state == state &&
      other.wfo == wfo &&
      other.comments == comments &&
      other.title == title;

    @override
    int get hashCode =>
        type.hashCode +
        (magnitude == null ? 0 : magnitude.hashCode) +
        magnitudeUnits.hashCode +
        location.hashCode +
        county.hashCode +
        state.hashCode +
        wfo.hashCode +
        comments.hashCode +
        title.hashCode;

  factory StormReportPayload.fromJson(Map<String, dynamic> json) => _$StormReportPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$StormReportPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StormReportPayloadTypeEnum {
@JsonValue(r'tornado')
tornado(r'tornado'),
@JsonValue(r'wind')
wind(r'wind'),
@JsonValue(r'hail')
hail(r'hail');

const StormReportPayloadTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// EF (tornado), mph (wind) or in (hail)
enum StormReportPayloadMagnitudeUnitsEnum {
@JsonValue(r'EF')
EF(r'EF'),
@JsonValue(r'mph')
mph(r'mph'),
@JsonValue(r'in')
in_(r'in');

const StormReportPayloadMagnitudeUnitsEnum(this.value);

final String value;

@override
String toString() => value;
}


