//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/maintenance_run_steps_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'maintenance_run.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MaintenanceRun {
  /// Returns a new [MaintenanceRun] instance.
  MaintenanceRun({

    required  this.id,

    required  this.trigger,

    required  this.status,

    required  this.startedAt,

     this.finishedAt,

    required  this.steps,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// monthly-cycle (the monthly cycle run), manual (runAnalysisMaintenance) or schedule (the 1st at 03:30 UTC when no monthly cycle is enabled)
  @JsonKey(
    
    name: r'trigger',
    required: true,
    includeIfNull: false,
  )


  final MaintenanceRunTriggerEnum trigger;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final MaintenanceRunStatusEnum status;



  @JsonKey(
    
    name: r'startedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime startedAt;



  @JsonKey(
    
    name: r'finishedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? finishedAt;



  @JsonKey(
    
    name: r'steps',
    required: true,
    includeIfNull: false,
  )


  final List<MaintenanceRunStepsInner> steps;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MaintenanceRun &&
      other.id == id &&
      other.trigger == trigger &&
      other.status == status &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.steps == steps;

    @override
    int get hashCode =>
        id.hashCode +
        trigger.hashCode +
        status.hashCode +
        startedAt.hashCode +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        steps.hashCode;

  factory MaintenanceRun.fromJson(Map<String, dynamic> json) => _$MaintenanceRunFromJson(json);

  Map<String, dynamic> toJson() => _$MaintenanceRunToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// monthly-cycle (the monthly cycle run), manual (runAnalysisMaintenance) or schedule (the 1st at 03:30 UTC when no monthly cycle is enabled)
enum MaintenanceRunTriggerEnum {
@JsonValue(r'monthly-cycle')
monthlyCycle(r'monthly-cycle'),
@JsonValue(r'manual')
manual(r'manual'),
@JsonValue(r'schedule')
schedule(r'schedule');

const MaintenanceRunTriggerEnum(this.value);

final String value;

@override
String toString() => value;
}


enum MaintenanceRunStatusEnum {
@JsonValue(r'running')
running(r'running'),
@JsonValue(r'done')
done(r'done'),
@JsonValue(r'partial')
partial(r'partial'),
@JsonValue(r'failed')
failed(r'failed');

const MaintenanceRunStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


