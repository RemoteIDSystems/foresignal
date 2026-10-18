//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/event_selector.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'watch_pair.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WatchPair {
  /// Returns a new [WatchPair] instance.
  WatchPair({

    required  this.eventFamily,

     this.regionCodes,

     this.selector,

     this.minEffect = 2.0,

     this.releaseLagDays,
  });

      /// Signal family from the 119 registry (`intel/signal-families.json`, operator-extensible), e.g. `ais-behaviour`, `aviation-military`, `aviation-ops`, `conflict`, `outage`, `internet-measurement`, `gnss-interference`, `nav-warning`, `attention`, `seismic`, `fire`, `weather-severe`, `cyber`, `markets`, `sanctions`, `health`, `space`
  @JsonKey(
    
    name: r'eventFamily',
    required: true,
    includeIfNull: false,
  )


  final String eventFamily;



      /// Event regions (empty = any)
  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



      /// Optional narrower event selector
  @JsonKey(
    
    name: r'selector',
    required: false,
    includeIfNull: false,
  )


  final EventSelector? selector;



      /// Minimum |CAR| in standard deviations
  @JsonKey(
    defaultValue: 2.0,
    name: r'minEffect',
    required: false,
    includeIfNull: false,
  )


  final num? minEffect;



      /// Publication lag of the series; triggers a re-check and `LAGGED_PUBLICATION`
  @JsonKey(
    
    name: r'releaseLagDays',
    required: false,
    includeIfNull: false,
  )


  final int? releaseLagDays;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WatchPair &&
      other.eventFamily == eventFamily &&
      other.regionCodes == regionCodes &&
      other.selector == selector &&
      other.minEffect == minEffect &&
      other.releaseLagDays == releaseLagDays;

    @override
    int get hashCode =>
        eventFamily.hashCode +
        regionCodes.hashCode +
        (selector == null ? 0 : selector.hashCode) +
        minEffect.hashCode +
        (releaseLagDays == null ? 0 : releaseLagDays.hashCode);

  factory WatchPair.fromJson(Map<String, dynamic> json) => _$WatchPairFromJson(json);

  Map<String, dynamic> toJson() => _$WatchPairToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

