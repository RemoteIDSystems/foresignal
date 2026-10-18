//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_join_reason_components.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationJoinReasonComponents {
  /// Returns a new [SituationJoinReasonComponents] instance.
  SituationJoinReasonComponents({

     this.region,

     this.distanceKm,

     this.entity,

     this.topic,

     this.textSimilarity,

     this.theme,

     this.recencyHours,
  });

      /// Region contribution (0.30 shared admin1, 0.20 shared country)
  @JsonKey(
    
    name: r'region',
    required: false,
    includeIfNull: false,
  )


  final num? region;



      /// Distance to the situation geometry in km (contributes 0.25 × max(0, 1 − d / maxDistanceKm))
  @JsonKey(
    
    name: r'distanceKm',
    required: false,
    includeIfNull: false,
  )


  final num? distanceKm;



      /// Shared 098 entity contribution (0.25)
  @JsonKey(
    
    name: r'entity',
    required: false,
    includeIfNull: false,
  )


  final num? entity;



      /// 117 attention-topic keyword match contribution (0.15)
  @JsonKey(
    
    name: r'topic',
    required: false,
    includeIfNull: false,
  )


  final num? topic;



      /// Embedding similarity contribution, 0.20 × max(0, (cos − 0.70) / 0.30)
  @JsonKey(
    
    name: r'textSimilarity',
    required: false,
    includeIfNull: false,
  )


  final num? textSimilarity;



      /// Theme incompatibility penalty (−0.20 or 0)
  @JsonKey(
    
    name: r'theme',
    required: false,
    includeIfNull: false,
  )


  final num? theme;



      /// Hours since the situation's last activity (recency multiplier 0.5 + 0.5 × exp(−h / 72))
  @JsonKey(
    
    name: r'recencyHours',
    required: false,
    includeIfNull: false,
  )


  final num? recencyHours;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationJoinReasonComponents &&
      other.region == region &&
      other.distanceKm == distanceKm &&
      other.entity == entity &&
      other.topic == topic &&
      other.textSimilarity == textSimilarity &&
      other.theme == theme &&
      other.recencyHours == recencyHours;

    @override
    int get hashCode =>
        (region == null ? 0 : region.hashCode) +
        (distanceKm == null ? 0 : distanceKm.hashCode) +
        (entity == null ? 0 : entity.hashCode) +
        (topic == null ? 0 : topic.hashCode) +
        (textSimilarity == null ? 0 : textSimilarity.hashCode) +
        (theme == null ? 0 : theme.hashCode) +
        (recencyHours == null ? 0 : recencyHours.hashCode);

  factory SituationJoinReasonComponents.fromJson(Map<String, dynamic> json) => _$SituationJoinReasonComponentsFromJson(json);

  Map<String, dynamic> toJson() => _$SituationJoinReasonComponentsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

