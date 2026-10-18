//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/sample_track_response_points_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sample_track_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SampleTrackResponse {
  /// Returns a new [SampleTrackResponse] instance.
  SampleTrackResponse({

     this.record,

    required  this.products,

    required  this.points,

     this.attribution,

     this.serverTime,
  });

  @JsonKey(
    
    name: r'record',
    required: false,
    includeIfNull: false,
  )


  final String? record;



  @JsonKey(
    
    name: r'products',
    required: true,
    includeIfNull: false,
  )


  final List<String> products;



  @JsonKey(
    
    name: r'points',
    required: true,
    includeIfNull: false,
  )


  final List<SampleTrackResponsePointsInner> points;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attribution;



  @JsonKey(
    
    name: r'serverTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? serverTime;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SampleTrackResponse &&
      other.record == record &&
      other.products == products &&
      other.points == points &&
      other.attribution == attribution &&
      other.serverTime == serverTime;

    @override
    int get hashCode =>
        (record == null ? 0 : record.hashCode) +
        products.hashCode +
        points.hashCode +
        attribution.hashCode +
        serverTime.hashCode;

  factory SampleTrackResponse.fromJson(Map<String, dynamic> json) => _$SampleTrackResponseFromJson(json);

  Map<String, dynamic> toJson() => _$SampleTrackResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

