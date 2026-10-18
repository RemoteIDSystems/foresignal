//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_exports.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoExports {
  /// Returns a new [SystemInfoExports] instance.
  SystemInfoExports({

     this.queued,

     this.running,

     this.bytes,

     this.quotaBytes,

     this.maxConcurrent,
  });

  @JsonKey(
    
    name: r'queued',
    required: false,
    includeIfNull: false,
  )


  final int? queued;



  @JsonKey(
    
    name: r'running',
    required: false,
    includeIfNull: false,
  )


  final int? running;



  @JsonKey(
    
    name: r'bytes',
    required: false,
    includeIfNull: false,
  )


  final int? bytes;



      /// exports.diskQuotaGb in bytes
  @JsonKey(
    
    name: r'quotaBytes',
    required: false,
    includeIfNull: false,
  )


  final int? quotaBytes;



      /// exports.maxConcurrent
  @JsonKey(
    
    name: r'maxConcurrent',
    required: false,
    includeIfNull: false,
  )


  final int? maxConcurrent;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoExports &&
      other.queued == queued &&
      other.running == running &&
      other.bytes == bytes &&
      other.quotaBytes == quotaBytes &&
      other.maxConcurrent == maxConcurrent;

    @override
    int get hashCode =>
        queued.hashCode +
        running.hashCode +
        bytes.hashCode +
        quotaBytes.hashCode +
        maxConcurrent.hashCode;

  factory SystemInfoExports.fromJson(Map<String, dynamic> json) => _$SystemInfoExportsFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoExportsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

