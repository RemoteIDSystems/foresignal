//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sample_request_points_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sample_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SampleRequest {
  /// Returns a new [SampleRequest] instance.
  SampleRequest({

    required  this.points,

    required  this.products,

     this.time = 'latest',

     this.units,
  });

      /// Points to sample
  @JsonKey(
    
    name: r'points',
    required: true,
    includeIfNull: false,
  )


  final List<SampleRequestPointsInner> points;



  @JsonKey(
    
    name: r'products',
    required: true,
    includeIfNull: false,
  )


  final List<String> products;



      /// `latest`, `now`, ISO-8601, or a relative offset (`-PT1H`, `+PT6H`)
  @JsonKey(
    defaultValue: 'latest',
    name: r'time',
    required: false,
    includeIfNull: false,
  )


  final String? time;



  @JsonKey(
    
    name: r'units',
    required: false,
    includeIfNull: false,
  )


  final SampleRequestUnitsEnum? units;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SampleRequest &&
      other.points == points &&
      other.products == products &&
      other.time == time &&
      other.units == units;

    @override
    int get hashCode =>
        points.hashCode +
        products.hashCode +
        time.hashCode +
        units.hashCode;

  factory SampleRequest.fromJson(Map<String, dynamic> json) => _$SampleRequestFromJson(json);

  Map<String, dynamic> toJson() => _$SampleRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SampleRequestUnitsEnum {
@JsonValue(r'metric')
metric(r'metric'),
@JsonValue(r'imperial')
imperial(r'imperial');

const SampleRequestUnitsEnum(this.value);

final String value;

@override
String toString() => value;
}


