//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/s3_target.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'backup_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BackupSettings {
  /// Returns a new [BackupSettings] instance.
  BackupSettings({

     this.enabled = false,

     this.schedule,

     this.target = 'local',

     this.root,

     this.s3,

     this.keepBackups = 7,

     this.includeConfig = true,

     this.secretsSet,

     this.lastRunAt,
  });

      /// Master switch: off, `runBackup` is 409 BACKUP_NOT_CONFIGURED and the schedule does not fire
  @JsonKey(
    defaultValue: false,
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



      /// Cron expression for scheduled backups: six fields (second minute hour day month weekday), evaluated in UTC; null for backups only on request
  @JsonKey(
    
    name: r'schedule',
    required: false,
    includeIfNull: false,
  )


  final String? schedule;



      /// `local`: under `root`, or the data directory when `root` is empty; `external`: a mounted disk at the absolute `root`; `s3`: the `s3` bucket
  @JsonKey(
    defaultValue: 'local',
    name: r'target',
    required: true,
    includeIfNull: false,
  )


  final String target;



      /// Local/external backup directory; required and absolute for `external`; a relative `local` root is under the data directory
  @JsonKey(
    
    name: r'root',
    required: false,
    includeIfNull: false,
  )


  final String? root;



  @JsonKey(
    
    name: r's3',
    required: false,
    includeIfNull: false,
  )


  final S3Target? s3;



      /// How many complete backups of each type to retain; older ones are pruned with their files, and failed ones older than the oldest kept are dropped. Null keeps every backup. 1 to 10000
  @JsonKey(
    defaultValue: 7,
    name: r'keepBackups',
    required: false,
    includeIfNull: false,
  )


  final int? keepBackups;



      /// A scheduled backup, and a `runBackup` that names no type, also writes the config bundle (type `full`); off, they are `database` only
  @JsonKey(
    defaultValue: true,
    name: r'includeConfig',
    required: false,
    includeIfNull: false,
  )


  final bool? includeConfig;



      /// Names of secrets that have a stored value
  @JsonKey(
    
    name: r'secretsSet',
    required: false,
    includeIfNull: false,
  )


  final List<String>? secretsSet;



  @JsonKey(
    
    name: r'lastRunAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastRunAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BackupSettings &&
      other.enabled == enabled &&
      other.schedule == schedule &&
      other.target == target &&
      other.root == root &&
      other.s3 == s3 &&
      other.keepBackups == keepBackups &&
      other.includeConfig == includeConfig &&
      other.secretsSet == secretsSet &&
      other.lastRunAt == lastRunAt;

    @override
    int get hashCode =>
        enabled.hashCode +
        (schedule == null ? 0 : schedule.hashCode) +
        target.hashCode +
        (root == null ? 0 : root.hashCode) +
        (s3 == null ? 0 : s3.hashCode) +
        (keepBackups == null ? 0 : keepBackups.hashCode) +
        includeConfig.hashCode +
        secretsSet.hashCode +
        (lastRunAt == null ? 0 : lastRunAt.hashCode);

  factory BackupSettings.fromJson(Map<String, dynamic> json) => _$BackupSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$BackupSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

