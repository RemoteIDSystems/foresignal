//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/archive_settings_secrets.dart';
import 'package:foresignal_api/src/model/archive_settings_s3.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'archive_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ArchiveSettings {
  /// Returns a new [ArchiveSettings] instance.
  ArchiveSettings({

     this.enabled = false,

     this.sources,

     this.root,

    required  this.target,

     this.s3,

     this.secrets,

     this.secretsSet,

     this.keepLocal = true,

     this.cutoff,

     this.schedule,
  });

      /// Default false
  @JsonKey(
    defaultValue: false,
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



      /// Allowlist; empty = all non-private STANDARD and TRACK sources
  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



      /// Default ${FORESIGNAL_DATA_DIR}/archive; an absolute path, or a path relative to the data directory. For the `local` target, point it at a mounted external disk: archive segments are written under `segments/` there.
  @JsonKey(
    
    name: r'root',
    required: false,
    includeIfNull: false,
  )


  final String? root;



      /// Where archive segments go (R2-016): `local` is the external-disk tier (files under `root`, meant to be a mounted external disk), `s3` an S3-compatible bucket, `glacier` the same API with an archival storage class (objects are thawed before a restore). The prompt 111 GeoParquet copies upload only for `s3`.
  @JsonKey(
    
    name: r'target',
    required: true,
    includeIfNull: false,
  )


  final ArchiveSettingsTargetEnum target;



  @JsonKey(
    
    name: r's3',
    required: false,
    includeIfNull: false,
  )


  final ArchiveSettingsS3? s3;



  @JsonKey(
    
    name: r'secrets',
    required: false,
    includeIfNull: false,
  )


  final ArchiveSettingsSecrets? secrets;



      /// Names of secrets that have a stored value
  @JsonKey(
    
    name: r'secretsSet',
    required: false,
    includeIfNull: false,
  )


  final List<String>? secretsSet;



      /// Keep the local copy after a verified upload
  @JsonKey(
    defaultValue: true,
    name: r'keepLocal',
    required: false,
    includeIfNull: false,
  )


  final bool? keepLocal;



      /// Archive runs (R2-016) move STANDARD history of the archived sources older than this age (a duration of at least 1d, such as `365d`) to the cold tier, then purge it. Null: a run takes only what retention removes anyway. TRACK history is archived as its partitions reach `retention.trackWindow`.
  @JsonKey(
    
    name: r'cutoff',
    required: false,
    includeIfNull: false,
  )


  final String? cutoff;



      /// Six-field cron expression (second minute hour day-of-month month day-of-week, UTC) of scheduled archive runs (R2-016), such as `0 0 3 * * SUN`. Null: archive runs happen only through runArchive or the retention size cap.
  @JsonKey(
    
    name: r'schedule',
    required: false,
    includeIfNull: false,
  )


  final String? schedule;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ArchiveSettings &&
      other.enabled == enabled &&
      other.sources == sources &&
      other.root == root &&
      other.target == target &&
      other.s3 == s3 &&
      other.secrets == secrets &&
      other.secretsSet == secretsSet &&
      other.keepLocal == keepLocal &&
      other.cutoff == cutoff &&
      other.schedule == schedule;

    @override
    int get hashCode =>
        enabled.hashCode +
        sources.hashCode +
        root.hashCode +
        target.hashCode +
        (s3 == null ? 0 : s3.hashCode) +
        secrets.hashCode +
        secretsSet.hashCode +
        keepLocal.hashCode +
        (cutoff == null ? 0 : cutoff.hashCode) +
        (schedule == null ? 0 : schedule.hashCode);

  factory ArchiveSettings.fromJson(Map<String, dynamic> json) => _$ArchiveSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$ArchiveSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Where archive segments go (R2-016): `local` is the external-disk tier (files under `root`, meant to be a mounted external disk), `s3` an S3-compatible bucket, `glacier` the same API with an archival storage class (objects are thawed before a restore). The prompt 111 GeoParquet copies upload only for `s3`.
enum ArchiveSettingsTargetEnum {
@JsonValue(r'local')
local(r'local'),
@JsonValue(r's3')
s3(r's3'),
@JsonValue(r'glacier')
glacier(r'glacier');

const ArchiveSettingsTargetEnum(this.value);

final String value;

@override
String toString() => value;
}


