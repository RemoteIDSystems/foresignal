//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_area_payload_threshold.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastAreaPayloadThreshold {
  /// Returns a new [ForecastAreaPayloadThreshold] instance.
  ForecastAreaPayloadThreshold({

     this.op,

     this.value,

     this.units,
  });

  @JsonKey(
    
    name: r'op',
    required: false,
    includeIfNull: false,
  )


  final ForecastAreaPayloadThresholdOpEnum? op;



  @JsonKey(
    
    name: r'value',
    required: false,
    includeIfNull: false,
  )


  final num? value;



  @JsonKey(
    
    name: r'units',
    required: false,
    includeIfNull: false,
  )


  final String? units;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastAreaPayloadThreshold &&
      other.op == op &&
      other.value == value &&
      other.units == units;

    @override
    int get hashCode =>
        op.hashCode +
        value.hashCode +
        units.hashCode;

  factory ForecastAreaPayloadThreshold.fromJson(Map<String, dynamic> json) => _$ForecastAreaPayloadThresholdFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastAreaPayloadThresholdToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ForecastAreaPayloadThresholdOpEnum {
@JsonValue(r'>=')
greaterThanEqual(r'>='),
@JsonValue(r'<=')
lessThanEqual(r'<=');

const ForecastAreaPayloadThresholdOpEnum(this.value);

final String value;

@override
String toString() => value;
}


