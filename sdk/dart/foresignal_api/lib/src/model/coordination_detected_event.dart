//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'coordination_detected_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CoordinationDetectedEvent {
  /// Returns a new [CoordinationDetectedEvent] instance.
  CoordinationDetectedEvent({

    required  this.event,

    required  this.findingId,

    required  this.narrativeId,

    required  this.significantIndicators,

    required  this.at,
  });

  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final CoordinationDetectedEventEventEnum event;



      /// ULID identifier
  @JsonKey(
    
    name: r'findingId',
    required: true,
    includeIfNull: false,
  )


  final String findingId;



      /// ULID identifier
  @JsonKey(
    
    name: r'narrativeId',
    required: true,
    includeIfNull: false,
  )


  final String narrativeId;



  @JsonKey(
    
    name: r'significantIndicators',
    required: true,
    includeIfNull: false,
  )


  final List<CoordinationDetectedEventSignificantIndicatorsEnum> significantIndicators;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CoordinationDetectedEvent &&
      other.event == event &&
      other.findingId == findingId &&
      other.narrativeId == narrativeId &&
      other.significantIndicators == significantIndicators &&
      other.at == at;

    @override
    int get hashCode =>
        event.hashCode +
        findingId.hashCode +
        narrativeId.hashCode +
        significantIndicators.hashCode +
        at.hashCode;

  factory CoordinationDetectedEvent.fromJson(Map<String, dynamic> json) => _$CoordinationDetectedEventFromJson(json);

  Map<String, dynamic> toJson() => _$CoordinationDetectedEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CoordinationDetectedEventEventEnum {
@JsonValue(r'coordination.detected')
coordinationPeriodDetected(r'coordination.detected');

const CoordinationDetectedEventEventEnum(this.value);

final String value;

@override
String toString() => value;
}


enum CoordinationDetectedEventSignificantIndicatorsEnum {
@JsonValue(r'synchrony')
synchrony(r'synchrony'),
@JsonValue(r'textDuplication')
textDuplication(r'textDuplication'),
@JsonValue(r'accountAgeSkew')
accountAgeSkew(r'accountAgeSkew'),
@JsonValue(r'coRepostBursts')
coRepostBursts(r'coRepostBursts');

const CoordinationDetectedEventSignificantIndicatorsEnum(this.value);

final String value;

@override
String toString() => value;
}


