//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intelligence_event_context.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelligenceEventContext {
  /// Returns a new [IntelligenceEventContext] instance.
  IntelligenceEventContext({

     this.weather,
  });

      /// 105 weather annotators (outage-weather): NWS warnings, radar ≥ 50 dBZ, gusts ≥ 25 m/s near the event, as context members
  @JsonKey(
    
    name: r'weather',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? weather;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelligenceEventContext &&
      other.weather == weather;

    @override
    int get hashCode =>
        (weather == null ? 0 : weather.hashCode);

  factory IntelligenceEventContext.fromJson(Map<String, dynamic> json) => _$IntelligenceEventContextFromJson(json);

  Map<String, dynamic> toJson() => _$IntelligenceEventContextToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

