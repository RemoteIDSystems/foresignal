//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sample_track_request_points_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sample_track_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SampleTrackRequest {
  /// Returns a new [SampleTrackRequest] instance.
  SampleTrackRequest({

     this.record,

     this.from,

     this.to,

     this.points,

    required  this.products,

     this.maxGap,

     this.run,

     this.units,
  });

      /// `{sourceId}/{externalId}`
  @JsonKey(
    
    name: r'record',
    required: false,
    includeIfNull: false,
  )


  final String? record;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;



  @JsonKey(
    
    name: r'points',
    required: false,
    includeIfNull: false,
  )


  final List<SampleTrackRequestPointsInner>? points;



  @JsonKey(
    
    name: r'products',
    required: true,
    includeIfNull: false,
  )


  final List<String> products;



      /// Nearest-frame tolerance; default per-product maxGap. ISO-8601 duration or shorthand (30m, 6h, 7d).
  @JsonKey(
    
    name: r'maxGap',
    required: false,
    includeIfNull: false,
  )


  final String? maxGap;



      /// Forecast run for future times: latest | previous | <runIso>
  @JsonKey(
    
    name: r'run',
    required: false,
    includeIfNull: false,
  )


  final String? run;



  @JsonKey(
    
    name: r'units',
    required: false,
    includeIfNull: false,
  )


  final SampleTrackRequestUnitsEnum? units;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SampleTrackRequest &&
      other.record == record &&
      other.from == from &&
      other.to == to &&
      other.points == points &&
      other.products == products &&
      other.maxGap == maxGap &&
      other.run == run &&
      other.units == units;

    @override
    int get hashCode =>
        record.hashCode +
        from.hashCode +
        to.hashCode +
        points.hashCode +
        products.hashCode +
        maxGap.hashCode +
        run.hashCode +
        units.hashCode;

  factory SampleTrackRequest.fromJson(Map<String, dynamic> json) => _$SampleTrackRequestFromJson(json);

  Map<String, dynamic> toJson() => _$SampleTrackRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SampleTrackRequestUnitsEnum {
@JsonValue(r'metric')
metric(r'metric'),
@JsonValue(r'imperial')
imperial(r'imperial');

const SampleTrackRequestUnitsEnum(this.value);

final String value;

@override
String toString() => value;
}


