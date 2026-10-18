//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/retention_run_result_consumer_clamp.dart';
import 'package:foresignal_api/src/model/retention_run_result_per_source_value.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'retention_run_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RetentionRunResult {
  /// Returns a new [RetentionRunResult] instance.
  RetentionRunResult({

     this.startedAt,

     this.durationMs,

    required  this.versionsDeleted,

    required  this.headsDeleted,

     this.alertsDeleted,

     this.usageRowsDeleted,

     this.partitionsDropped,

     this.headsPurged,

     this.fetchLogDeleted,

     this.seqClockDeleted,

     this.notificationsDeleted,

     this.skippedSources,

     this.consumerClamp,

     this.perSource,
  });

  @JsonKey(
    
    name: r'startedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? startedAt;



  @JsonKey(
    
    name: r'durationMs',
    required: false,
    includeIfNull: false,
  )


  final int? durationMs;



  @JsonKey(
    
    name: r'versionsDeleted',
    required: true,
    includeIfNull: false,
  )


  final int versionsDeleted;



      /// Alias of headsPurged
  @JsonKey(
    
    name: r'headsDeleted',
    required: true,
    includeIfNull: false,
  )


  final int headsDeleted;



  @JsonKey(
    
    name: r'alertsDeleted',
    required: false,
    includeIfNull: false,
  )


  final int? alertsDeleted;



  @JsonKey(
    
    name: r'usageRowsDeleted',
    required: false,
    includeIfNull: false,
  )


  final int? usageRowsDeleted;



          // minimum: 0
  @JsonKey(
    
    name: r'partitionsDropped',
    required: false,
    includeIfNull: false,
  )


  final int? partitionsDropped;



          // minimum: 0
  @JsonKey(
    
    name: r'headsPurged',
    required: false,
    includeIfNull: false,
  )


  final int? headsPurged;



          // minimum: 0
  @JsonKey(
    
    name: r'fetchLogDeleted',
    required: false,
    includeIfNull: false,
  )


  final int? fetchLogDeleted;



          // minimum: 0
  @JsonKey(
    
    name: r'seqClockDeleted',
    required: false,
    includeIfNull: false,
  )


  final int? seqClockDeleted;



          // minimum: 0
  @JsonKey(
    
    name: r'notificationsDeleted',
    required: false,
    includeIfNull: false,
  )


  final int? notificationsDeleted;



      /// Sources skipped this run after a failed delete (no HeadPurge)
  @JsonKey(
    
    name: r'skippedSources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? skippedSources;



  @JsonKey(
    
    name: r'consumerClamp',
    required: false,
    includeIfNull: false,
  )


  final RetentionRunResultConsumerClamp? consumerClamp;



      /// Keyed by source id
  @JsonKey(
    
    name: r'perSource',
    required: false,
    includeIfNull: false,
  )


  final Map<String, RetentionRunResultPerSourceValue>? perSource;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RetentionRunResult &&
      other.startedAt == startedAt &&
      other.durationMs == durationMs &&
      other.versionsDeleted == versionsDeleted &&
      other.headsDeleted == headsDeleted &&
      other.alertsDeleted == alertsDeleted &&
      other.usageRowsDeleted == usageRowsDeleted &&
      other.partitionsDropped == partitionsDropped &&
      other.headsPurged == headsPurged &&
      other.fetchLogDeleted == fetchLogDeleted &&
      other.seqClockDeleted == seqClockDeleted &&
      other.notificationsDeleted == notificationsDeleted &&
      other.skippedSources == skippedSources &&
      other.consumerClamp == consumerClamp &&
      other.perSource == perSource;

    @override
    int get hashCode =>
        startedAt.hashCode +
        durationMs.hashCode +
        versionsDeleted.hashCode +
        headsDeleted.hashCode +
        alertsDeleted.hashCode +
        usageRowsDeleted.hashCode +
        partitionsDropped.hashCode +
        headsPurged.hashCode +
        fetchLogDeleted.hashCode +
        seqClockDeleted.hashCode +
        notificationsDeleted.hashCode +
        skippedSources.hashCode +
        consumerClamp.hashCode +
        perSource.hashCode;

  factory RetentionRunResult.fromJson(Map<String, dynamic> json) => _$RetentionRunResultFromJson(json);

  Map<String, dynamic> toJson() => _$RetentionRunResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

