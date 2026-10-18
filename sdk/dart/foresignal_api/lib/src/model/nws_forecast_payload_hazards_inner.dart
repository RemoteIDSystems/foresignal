//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'nws_forecast_payload_hazards_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NwsForecastPayloadHazardsInner {
  /// Returns a new [NwsForecastPayloadHazardsInner] instance.
  NwsForecastPayloadHazardsInner({

    required  this.type,

     this.validTime,
  });

      /// phenomenon.significance, for example FZ.W
  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final String type;



      /// ISO-8601 interval
  @JsonKey(
    
    name: r'validTime',
    required: false,
    includeIfNull: false,
  )


  final String? validTime;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NwsForecastPayloadHazardsInner &&
      other.type == type &&
      other.validTime == validTime;

    @override
    int get hashCode =>
        type.hashCode +
        validTime.hashCode;

  factory NwsForecastPayloadHazardsInner.fromJson(Map<String, dynamic> json) => _$NwsForecastPayloadHazardsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$NwsForecastPayloadHazardsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

