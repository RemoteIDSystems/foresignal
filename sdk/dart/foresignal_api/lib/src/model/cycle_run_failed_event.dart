//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cycle_run_failed_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CycleRunFailedEvent {
  /// Returns a new [CycleRunFailedEvent] instance.
  CycleRunFailedEvent({

    required  this.runId,

     this.cycleId,

     this.situationId,

    required  this.cadence,

    required  this.reason,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'runId',
    required: true,
    includeIfNull: false,
  )


  final String runId;



      /// ULID identifier
  @JsonKey(
    
    name: r'cycleId',
    required: false,
    includeIfNull: false,
  )


  final String? cycleId;



      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: false,
    includeIfNull: false,
  )


  final String? situationId;



  @JsonKey(
    
    name: r'cadence',
    required: true,
    includeIfNull: false,
  )


  final CycleRunFailedEventCadenceEnum cadence;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CycleRunFailedEvent &&
      other.runId == runId &&
      other.cycleId == cycleId &&
      other.situationId == situationId &&
      other.cadence == cadence &&
      other.reason == reason &&
      other.at == at;

    @override
    int get hashCode =>
        runId.hashCode +
        (cycleId == null ? 0 : cycleId.hashCode) +
        (situationId == null ? 0 : situationId.hashCode) +
        cadence.hashCode +
        reason.hashCode +
        at.hashCode;

  factory CycleRunFailedEvent.fromJson(Map<String, dynamic> json) => _$CycleRunFailedEventFromJson(json);

  Map<String, dynamic> toJson() => _$CycleRunFailedEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CycleRunFailedEventCadenceEnum {
@JsonValue(r'daily')
daily(r'daily'),
@JsonValue(r'weekly')
weekly(r'weekly'),
@JsonValue(r'monthly')
monthly(r'monthly'),
@JsonValue(r'on-demand')
onDemand(r'on-demand');

const CycleRunFailedEventCadenceEnum(this.value);

final String value;

@override
String toString() => value;
}


