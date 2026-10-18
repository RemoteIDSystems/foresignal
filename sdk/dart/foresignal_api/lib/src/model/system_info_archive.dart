//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_archive.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoArchive {
  /// Returns a new [SystemInfoArchive] instance.
  SystemInfoArchive({

     this.enabled,

     this.lastRunAt,

     this.bytes,

     this.failures,

     this.target,

     this.lastRunBytes,

     this.lastRunFailures,

     this.lastFailureAt,
  });

  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



  @JsonKey(
    
    name: r'lastRunAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastRunAt;



  @JsonKey(
    
    name: r'bytes',
    required: false,
    includeIfNull: false,
  )


  final int? bytes;



  @JsonKey(
    
    name: r'failures',
    required: false,
    includeIfNull: false,
  )


  final int? failures;



  @JsonKey(
    
    name: r'target',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoArchiveTargetEnum? target;



  @JsonKey(
    
    name: r'lastRunBytes',
    required: false,
    includeIfNull: false,
  )


  final int? lastRunBytes;



  @JsonKey(
    
    name: r'lastRunFailures',
    required: false,
    includeIfNull: false,
  )


  final int? lastRunFailures;



  @JsonKey(
    
    name: r'lastFailureAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastFailureAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoArchive &&
      other.enabled == enabled &&
      other.lastRunAt == lastRunAt &&
      other.bytes == bytes &&
      other.failures == failures &&
      other.target == target &&
      other.lastRunBytes == lastRunBytes &&
      other.lastRunFailures == lastRunFailures &&
      other.lastFailureAt == lastFailureAt;

    @override
    int get hashCode =>
        enabled.hashCode +
        (lastRunAt == null ? 0 : lastRunAt.hashCode) +
        bytes.hashCode +
        failures.hashCode +
        target.hashCode +
        lastRunBytes.hashCode +
        lastRunFailures.hashCode +
        (lastFailureAt == null ? 0 : lastFailureAt.hashCode);

  factory SystemInfoArchive.fromJson(Map<String, dynamic> json) => _$SystemInfoArchiveFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoArchiveToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SystemInfoArchiveTargetEnum {
@JsonValue(r'local')
local(r'local'),
@JsonValue(r's3')
s3(r's3');

const SystemInfoArchiveTargetEnum(this.value);

final String value;

@override
String toString() => value;
}


