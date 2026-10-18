//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_ingest_writer.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoIngestWriter {
  /// Returns a new [SystemInfoIngestWriter] instance.
  SystemInfoIngestWriter({

     this.state,

     this.warnings,

     this.lastSeq,

     this.lastIngestedAt,

     this.queueDepth,
  });

  @JsonKey(
    
    name: r'state',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoIngestWriterStateEnum? state;



  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<String>? warnings;



  @JsonKey(
    
    name: r'lastSeq',
    required: false,
    includeIfNull: false,
  )


  final int? lastSeq;



  @JsonKey(
    
    name: r'lastIngestedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastIngestedAt;



  @JsonKey(
    
    name: r'queueDepth',
    required: false,
    includeIfNull: false,
  )


  final int? queueDepth;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoIngestWriter &&
      other.state == state &&
      other.warnings == warnings &&
      other.lastSeq == lastSeq &&
      other.lastIngestedAt == lastIngestedAt &&
      other.queueDepth == queueDepth;

    @override
    int get hashCode =>
        state.hashCode +
        warnings.hashCode +
        lastSeq.hashCode +
        (lastIngestedAt == null ? 0 : lastIngestedAt.hashCode) +
        queueDepth.hashCode;

  factory SystemInfoIngestWriter.fromJson(Map<String, dynamic> json) => _$SystemInfoIngestWriterFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoIngestWriterToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SystemInfoIngestWriterStateEnum {
@JsonValue(r'RUNNING')
RUNNING(r'RUNNING'),
@JsonValue(r'CLOCK_BEHIND')
CLOCK_BEHIND(r'CLOCK_BEHIND'),
@JsonValue(r'LOCK_LOST')
LOCK_LOST(r'LOCK_LOST'),
@JsonValue(r'STOPPED')
STOPPED(r'STOPPED');

const SystemInfoIngestWriterStateEnum(this.value);

final String value;

@override
String toString() => value;
}


