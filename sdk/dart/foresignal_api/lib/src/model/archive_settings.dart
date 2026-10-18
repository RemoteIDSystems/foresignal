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



      /// Default ${FORESIGNAL_DATA_DIR}/archive; an absolute path, or a path relative to the data directory
  @JsonKey(
    
    name: r'root',
    required: false,
    includeIfNull: false,
  )


  final String? root;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is ArchiveSettings &&
      other.enabled == enabled &&
      other.sources == sources &&
      other.root == root &&
      other.target == target &&
      other.s3 == s3 &&
      other.secrets == secrets &&
      other.secretsSet == secretsSet &&
      other.keepLocal == keepLocal;

    @override
    int get hashCode =>
        enabled.hashCode +
        sources.hashCode +
        root.hashCode +
        target.hashCode +
        (s3 == null ? 0 : s3.hashCode) +
        secrets.hashCode +
        secretsSet.hashCode +
        keepLocal.hashCode;

  factory ArchiveSettings.fromJson(Map<String, dynamic> json) => _$ArchiveSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$ArchiveSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ArchiveSettingsTargetEnum {
@JsonValue(r'local')
local(r'local'),
@JsonValue(r's3')
s3(r's3');

const ArchiveSettingsTargetEnum(this.value);

final String value;

@override
String toString() => value;
}


