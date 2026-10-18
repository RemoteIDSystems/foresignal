//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/intel_event_member.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'recipe_backtest_result_events_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecipeBacktestResultEventsInner {
  /// Returns a new [RecipeBacktestResultEventsInner] instance.
  RecipeBacktestResultEventsInner({

    required  this.type,

    required  this.startTime,

     this.endTime,

    required  this.members,

     this.confidence,

     this.evidenceQuality,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final RecipeBacktestResultEventsInnerTypeEnum type;



  @JsonKey(
    
    name: r'startTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime startTime;



  @JsonKey(
    
    name: r'endTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? endTime;



  @JsonKey(
    
    name: r'members',
    required: true,
    includeIfNull: false,
  )


  final List<IntelEventMember> members;



  @JsonKey(
    
    name: r'confidence',
    required: false,
    includeIfNull: false,
  )


  final num? confidence;



  @JsonKey(
    
    name: r'evidenceQuality',
    required: false,
    includeIfNull: false,
  )


  final RecipeBacktestResultEventsInnerEvidenceQualityEnum? evidenceQuality;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecipeBacktestResultEventsInner &&
      other.type == type &&
      other.startTime == startTime &&
      other.endTime == endTime &&
      other.members == members &&
      other.confidence == confidence &&
      other.evidenceQuality == evidenceQuality;

    @override
    int get hashCode =>
        type.hashCode +
        startTime.hashCode +
        (endTime == null ? 0 : endTime.hashCode) +
        members.hashCode +
        confidence.hashCode +
        evidenceQuality.hashCode;

  factory RecipeBacktestResultEventsInner.fromJson(Map<String, dynamic> json) => _$RecipeBacktestResultEventsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$RecipeBacktestResultEventsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RecipeBacktestResultEventsInnerTypeEnum {
@JsonValue(r'maritime-disruption')
maritimeDisruption(r'maritime-disruption'),
@JsonValue(r'aviation-anomaly')
aviationAnomaly(r'aviation-anomaly'),
@JsonValue(r'infrastructure-disruption')
infrastructureDisruption(r'infrastructure-disruption'),
@JsonValue(r'weather-impact')
weatherImpact(r'weather-impact'),
@JsonValue(r'seismic')
seismic(r'seismic'),
@JsonValue(r'custom')
custom(r'custom');

const RecipeBacktestResultEventsInnerTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum RecipeBacktestResultEventsInnerEvidenceQualityEnum {
@JsonValue(r'strong')
strong(r'strong'),
@JsonValue(r'moderate')
moderate(r'moderate'),
@JsonValue(r'weak')
weak(r'weak'),
@JsonValue(r'insufficient')
insufficient(r'insufficient');

const RecipeBacktestResultEventsInnerEvidenceQualityEnum(this.value);

final String value;

@override
String toString() => value;
}


