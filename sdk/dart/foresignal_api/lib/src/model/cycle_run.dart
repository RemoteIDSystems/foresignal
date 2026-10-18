//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/cycle_run_roles_inner.dart';
import 'package:foresignal_api/src/model/cycle_packet.dart';
import 'package:foresignal_api/src/model/maintenance_run.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cycle_run.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CycleRun {
  /// Returns a new [CycleRun] instance.
  CycleRun({

    required  this.id,

     this.cycleId,

     this.situationId,

    required  this.cadence,

    required  this.trigger,

    required  this.status,

    required  this.periodStart,

    required  this.periodEnd,

     this.previousRunId,

     this.startedAt,

     this.finishedAt,

     this.packet,

     this.roles,

     this.tokensUsed,

     this.assessmentId,

     this.maintenance,

     this.manifestId,

     this.warnings,

     this.error,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



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


  final CycleRunCadenceEnum cadence;



  @JsonKey(
    
    name: r'trigger',
    required: true,
    includeIfNull: false,
  )


  final CycleRunTriggerEnum trigger;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final CycleRunStatusEnum status;



  @JsonKey(
    
    name: r'periodStart',
    required: true,
    includeIfNull: false,
  )


  final DateTime periodStart;



  @JsonKey(
    
    name: r'periodEnd',
    required: true,
    includeIfNull: false,
  )


  final DateTime periodEnd;



      /// ULID identifier
  @JsonKey(
    
    name: r'previousRunId',
    required: false,
    includeIfNull: false,
  )


  final String? previousRunId;



  @JsonKey(
    
    name: r'startedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? startedAt;



  @JsonKey(
    
    name: r'finishedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? finishedAt;



  @JsonKey(
    
    name: r'packet',
    required: false,
    includeIfNull: false,
  )


  final CyclePacket? packet;



  @JsonKey(
    
    name: r'roles',
    required: false,
    includeIfNull: false,
  )


  final List<CycleRunRolesInner>? roles;



  @JsonKey(
    
    name: r'tokensUsed',
    required: false,
    includeIfNull: false,
  )


  final int? tokensUsed;



      /// ULID identifier
  @JsonKey(
    
    name: r'assessmentId',
    required: false,
    includeIfNull: false,
  )


  final String? assessmentId;



  @JsonKey(
    
    name: r'maintenance',
    required: false,
    includeIfNull: false,
  )


  final MaintenanceRun? maintenance;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;



  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<String>? warnings;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CycleRun &&
      other.id == id &&
      other.cycleId == cycleId &&
      other.situationId == situationId &&
      other.cadence == cadence &&
      other.trigger == trigger &&
      other.status == status &&
      other.periodStart == periodStart &&
      other.periodEnd == periodEnd &&
      other.previousRunId == previousRunId &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.packet == packet &&
      other.roles == roles &&
      other.tokensUsed == tokensUsed &&
      other.assessmentId == assessmentId &&
      other.maintenance == maintenance &&
      other.manifestId == manifestId &&
      other.warnings == warnings &&
      other.error == error;

    @override
    int get hashCode =>
        id.hashCode +
        (cycleId == null ? 0 : cycleId.hashCode) +
        (situationId == null ? 0 : situationId.hashCode) +
        cadence.hashCode +
        trigger.hashCode +
        status.hashCode +
        periodStart.hashCode +
        periodEnd.hashCode +
        (previousRunId == null ? 0 : previousRunId.hashCode) +
        (startedAt == null ? 0 : startedAt.hashCode) +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        (packet == null ? 0 : packet.hashCode) +
        roles.hashCode +
        tokensUsed.hashCode +
        (assessmentId == null ? 0 : assessmentId.hashCode) +
        (maintenance == null ? 0 : maintenance.hashCode) +
        (manifestId == null ? 0 : manifestId.hashCode) +
        warnings.hashCode +
        (error == null ? 0 : error.hashCode);

  factory CycleRun.fromJson(Map<String, dynamic> json) => _$CycleRunFromJson(json);

  Map<String, dynamic> toJson() => _$CycleRunToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CycleRunCadenceEnum {
@JsonValue(r'daily')
daily(r'daily'),
@JsonValue(r'weekly')
weekly(r'weekly'),
@JsonValue(r'monthly')
monthly(r'monthly'),
@JsonValue(r'on-demand')
onDemand(r'on-demand');

const CycleRunCadenceEnum(this.value);

final String value;

@override
String toString() => value;
}


enum CycleRunTriggerEnum {
@JsonValue(r'schedule')
schedule(r'schedule'),
@JsonValue(r'missed')
missed(r'missed'),
@JsonValue(r'manual')
manual(r'manual'),
@JsonValue(r'situation')
situation(r'situation');

const CycleRunTriggerEnum(this.value);

final String value;

@override
String toString() => value;
}


enum CycleRunStatusEnum {
@JsonValue(r'queued')
queued(r'queued'),
@JsonValue(r'running')
running(r'running'),
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'partial')
partial(r'partial'),
@JsonValue(r'digest-only')
digestOnly(r'digest-only'),
@JsonValue(r'failed')
failed(r'failed');

const CycleRunStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


