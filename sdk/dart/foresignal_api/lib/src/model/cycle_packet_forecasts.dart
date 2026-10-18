//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/cycle_packet_item.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cycle_packet_forecasts.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CyclePacketForecasts {
  /// Returns a new [CyclePacketForecasts] instance.
  CyclePacketForecasts({

     this.opened,

     this.due,

     this.resolved,

     this.scorecardChanges,
  });

  @JsonKey(
    
    name: r'opened',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? opened;



  @JsonKey(
    
    name: r'due',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? due;



  @JsonKey(
    
    name: r'resolved',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? resolved;



  @JsonKey(
    
    name: r'scorecardChanges',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? scorecardChanges;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CyclePacketForecasts &&
      other.opened == opened &&
      other.due == due &&
      other.resolved == resolved &&
      other.scorecardChanges == scorecardChanges;

    @override
    int get hashCode =>
        opened.hashCode +
        due.hashCode +
        resolved.hashCode +
        scorecardChanges.hashCode;

  factory CyclePacketForecasts.fromJson(Map<String, dynamic> json) => _$CyclePacketForecastsFromJson(json);

  Map<String, dynamic> toJson() => _$CyclePacketForecastsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

