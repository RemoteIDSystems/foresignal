//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_warnings_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoWarningsInner {
  /// Returns a new [SystemInfoWarningsInner] instance.
  SystemInfoWarningsInner({

    required  this.code,

     this.severity,

    required  this.message,
  });

      /// OPEN_READS_EXPOSED, OPEN_READS_ALL_INTERFACES, HISTORY_RATE_HIGH, PARTITION_HEADROOM_LOW, RETENTION_CONSUMER_LAG, RASTER_DISK_QUOTA (042: the raster store is at 90 % of its quota or more) (open set)
  @JsonKey(
    
    name: r'code',
    required: true,
    includeIfNull: false,
  )


  final String code;



  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoWarningsInnerSeverityEnum? severity;



  @JsonKey(
    
    name: r'message',
    required: true,
    includeIfNull: false,
  )


  final String message;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoWarningsInner &&
      other.code == code &&
      other.severity == severity &&
      other.message == message;

    @override
    int get hashCode =>
        code.hashCode +
        severity.hashCode +
        message.hashCode;

  factory SystemInfoWarningsInner.fromJson(Map<String, dynamic> json) => _$SystemInfoWarningsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoWarningsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SystemInfoWarningsInnerSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const SystemInfoWarningsInnerSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


