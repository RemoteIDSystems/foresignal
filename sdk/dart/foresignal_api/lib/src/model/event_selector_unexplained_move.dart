//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_selector_unexplained_move.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventSelectorUnexplainedMove {
  /// Returns a new [EventSelectorUnexplainedMove] instance.
  EventSelectorUnexplainedMove({

    required  this.family,
  });

      /// Signal family from the 119 registry (`intel/signal-families.json`, operator-extensible), e.g. `ais-behaviour`, `aviation-military`, `aviation-ops`, `conflict`, `outage`, `internet-measurement`, `gnss-interference`, `nav-warning`, `attention`, `seismic`, `fire`, `weather-severe`, `cyber`, `markets`, `sanctions`, `health`, `space`
  @JsonKey(
    
    name: r'family',
    required: true,
    includeIfNull: false,
  )


  final String family;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventSelectorUnexplainedMove &&
      other.family == family;

    @override
    int get hashCode =>
        family.hashCode;

  factory EventSelectorUnexplainedMove.fromJson(Map<String, dynamic> json) => _$EventSelectorUnexplainedMoveFromJson(json);

  Map<String, dynamic> toJson() => _$EventSelectorUnexplainedMoveToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

