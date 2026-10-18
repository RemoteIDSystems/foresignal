//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'impact_projection_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ImpactProjectionEvent {
  /// Returns a new [ImpactProjectionEvent] instance.
  ImpactProjectionEvent({

    required  this.findingId,

    required  this.hazardType,

     this.sourceRef,

    required  this.impactFamily,

    required  this.admin1,

    required  this.pImpact,

     this.questionId,

     this.supersededBy,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'findingId',
    required: true,
    includeIfNull: false,
  )


  final String findingId;



  @JsonKey(
    
    name: r'hazardType',
    required: true,
    includeIfNull: false,
  )


  final ImpactProjectionEventHazardTypeEnum hazardType;



  @JsonKey(
    
    name: r'sourceRef',
    required: false,
    includeIfNull: false,
  )


  final String? sourceRef;



  @JsonKey(
    
    name: r'impactFamily',
    required: true,
    includeIfNull: false,
  )


  final String impactFamily;



  @JsonKey(
    
    name: r'admin1',
    required: true,
    includeIfNull: false,
  )


  final String admin1;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'pImpact',
    required: true,
    includeIfNull: false,
  )


  final num pImpact;



      /// ULID identifier
  @JsonKey(
    
    name: r'questionId',
    required: false,
    includeIfNull: false,
  )


  final String? questionId;



      /// ULID identifier
  @JsonKey(
    
    name: r'supersededBy',
    required: false,
    includeIfNull: false,
  )


  final String? supersededBy;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ImpactProjectionEvent &&
      other.findingId == findingId &&
      other.hazardType == hazardType &&
      other.sourceRef == sourceRef &&
      other.impactFamily == impactFamily &&
      other.admin1 == admin1 &&
      other.pImpact == pImpact &&
      other.questionId == questionId &&
      other.supersededBy == supersededBy &&
      other.at == at;

    @override
    int get hashCode =>
        findingId.hashCode +
        hazardType.hashCode +
        sourceRef.hashCode +
        impactFamily.hashCode +
        admin1.hashCode +
        pImpact.hashCode +
        (questionId == null ? 0 : questionId.hashCode) +
        (supersededBy == null ? 0 : supersededBy.hashCode) +
        at.hashCode;

  factory ImpactProjectionEvent.fromJson(Map<String, dynamic> json) => _$ImpactProjectionEventFromJson(json);

  Map<String, dynamic> toJson() => _$ImpactProjectionEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ImpactProjectionEventHazardTypeEnum {
@JsonValue(r'tropical-wind-34')
tropicalWind34(r'tropical-wind-34'),
@JsonValue(r'tropical-wind-50')
tropicalWind50(r'tropical-wind-50'),
@JsonValue(r'tropical-wind-64')
tropicalWind64(r'tropical-wind-64'),
@JsonValue(r'tropical-track')
tropicalTrack(r'tropical-track'),
@JsonValue(r'tornado')
tornado(r'tornado'),
@JsonValue(r'wind')
wind(r'wind'),
@JsonValue(r'hail')
hail(r'hail'),
@JsonValue(r'severe-categorical')
severeCategorical(r'severe-categorical'),
@JsonValue(r'excessive-rain')
excessiveRain(r'excessive-rain'),
@JsonValue(r'river-flood')
riverFlood(r'river-flood'),
@JsonValue(r'storm-cell')
stormCell(r'storm-cell');

const ImpactProjectionEventHazardTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


