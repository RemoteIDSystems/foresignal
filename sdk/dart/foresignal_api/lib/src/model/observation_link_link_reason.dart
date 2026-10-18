//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'observation_link_link_reason.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ObservationLinkLinkReason {
  /// Returns a new [ObservationLinkLinkReason] instance.
  ObservationLinkLinkReason({

    required  this.rule,

     this.dtSeconds,

     this.distanceKm,

     this.attributeDeltas,

     this.upstreamIdMatch,
  });

  @JsonKey(
    
    name: r'rule',
    required: true,
    includeIfNull: false,
  )


  final String rule;



  @JsonKey(
    
    name: r'dtSeconds',
    required: false,
    includeIfNull: false,
  )


  final num? dtSeconds;



  @JsonKey(
    
    name: r'distanceKm',
    required: false,
    includeIfNull: false,
  )


  final num? distanceKm;



  @JsonKey(
    
    name: r'attributeDeltas',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? attributeDeltas;



  @JsonKey(
    
    name: r'upstreamIdMatch',
    required: false,
    includeIfNull: false,
  )


  final bool? upstreamIdMatch;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ObservationLinkLinkReason &&
      other.rule == rule &&
      other.dtSeconds == dtSeconds &&
      other.distanceKm == distanceKm &&
      other.attributeDeltas == attributeDeltas &&
      other.upstreamIdMatch == upstreamIdMatch;

    @override
    int get hashCode =>
        rule.hashCode +
        (dtSeconds == null ? 0 : dtSeconds.hashCode) +
        (distanceKm == null ? 0 : distanceKm.hashCode) +
        attributeDeltas.hashCode +
        upstreamIdMatch.hashCode;

  factory ObservationLinkLinkReason.fromJson(Map<String, dynamic> json) => _$ObservationLinkLinkReasonFromJson(json);

  Map<String, dynamic> toJson() => _$ObservationLinkLinkReasonToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

