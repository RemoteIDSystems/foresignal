//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/archive_settings_secrets.dart';
import 'package:foresignal_api/src/model/s3_target.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'backup_settings_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BackupSettingsInput {
  /// Returns a new [BackupSettingsInput] instance.
  BackupSettingsInput({

    required  this.enabled,

     this.schedule,

    required  this.target,

     this.root,

     this.s3,

     this.keepBackups,

     this.includeConfig = true,

     this.secrets,
  });

  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'schedule',
    required: false,
    includeIfNull: false,
  )


  final String? schedule;



  @JsonKey(
    
    name: r'target',
    required: true,
    includeIfNull: false,
  )


  final String target;



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



          // minimum: 1
          // maximum: 10000
  @JsonKey(
    
    name: r'keepBackups',
    required: false,
    includeIfNull: false,
  )


  final int? keepBackups;



  @JsonKey(
    defaultValue: true,
    name: r'includeConfig',
    required: false,
    includeIfNull: false,
  )


  final bool? includeConfig;



  @JsonKey(
    
    name: r'secrets',
    required: false,
    includeIfNull: false,
  )


  final ArchiveSettingsSecrets? secrets;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BackupSettingsInput &&
      other.enabled == enabled &&
      other.schedule == schedule &&
      other.target == target &&
      other.root == root &&
      other.s3 == s3 &&
      other.keepBackups == keepBackups &&
      other.includeConfig == includeConfig &&
      other.secrets == secrets;

    @override
    int get hashCode =>
        enabled.hashCode +
        (schedule == null ? 0 : schedule.hashCode) +
        target.hashCode +
        (root == null ? 0 : root.hashCode) +
        (s3 == null ? 0 : s3.hashCode) +
        (keepBackups == null ? 0 : keepBackups.hashCode) +
        includeConfig.hashCode +
        secrets.hashCode;

  factory BackupSettingsInput.fromJson(Map<String, dynamic> json) => _$BackupSettingsInputFromJson(json);

  Map<String, dynamic> toJson() => _$BackupSettingsInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

