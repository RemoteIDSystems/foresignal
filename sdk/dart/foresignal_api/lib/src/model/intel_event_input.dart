//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/intel_event_member_input.dart';
import 'package:foresignal_api/src/model/severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_event_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelEventInput {
  /// Returns a new [IntelEventInput] instance.
  IntelEventInput({

    required  this.type,

    required  this.title,

     this.severity,

     this.origin,

     this.geometry,

     this.startTime,

     this.endTime,

     this.notes,

    required  this.members,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final IntelEventInputTypeEnum type;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final Severity? severity;



  @JsonKey(
    
    name: r'origin',
    required: false,
    includeIfNull: false,
  )


  final IntelEventInputOriginEnum? origin;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



  @JsonKey(
    
    name: r'startTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? startTime;



  @JsonKey(
    
    name: r'endTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? endTime;



  @JsonKey(
    
    name: r'notes',
    required: false,
    includeIfNull: false,
  )


  final String? notes;



  @JsonKey(
    
    name: r'members',
    required: true,
    includeIfNull: false,
  )


  final List<IntelEventMemberInput> members;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelEventInput &&
      other.type == type &&
      other.title == title &&
      other.severity == severity &&
      other.origin == origin &&
      other.geometry == geometry &&
      other.startTime == startTime &&
      other.endTime == endTime &&
      other.notes == notes &&
      other.members == members;

    @override
    int get hashCode =>
        type.hashCode +
        title.hashCode +
        severity.hashCode +
        origin.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        startTime.hashCode +
        (endTime == null ? 0 : endTime.hashCode) +
        notes.hashCode +
        members.hashCode;

  factory IntelEventInput.fromJson(Map<String, dynamic> json) => _$IntelEventInputFromJson(json);

  Map<String, dynamic> toJson() => _$IntelEventInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IntelEventInputTypeEnum {
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

const IntelEventInputTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IntelEventInputOriginEnum {
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'operator')
operator_(r'operator');

const IntelEventInputOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


