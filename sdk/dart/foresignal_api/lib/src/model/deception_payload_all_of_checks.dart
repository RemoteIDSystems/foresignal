//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'deception_payload_all_of_checks.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DeceptionPayloadAllOfChecks {
  /// Returns a new [DeceptionPayloadAllOfChecks] instance.
  DeceptionPayloadAllOfChecks({

    required  this.check,

    required  this.count,

     this.lastAt,

     this.detail,
  });

  @JsonKey(
    
    name: r'check',
    required: true,
    includeIfNull: false,
  )


  final DeceptionPayloadAllOfChecksCheckEnum check;



  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;



  @JsonKey(
    
    name: r'lastAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastAt;



  @JsonKey(
    
    name: r'detail',
    required: false,
    includeIfNull: false,
  )


  final String? detail;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DeceptionPayloadAllOfChecks &&
      other.check == check &&
      other.count == count &&
      other.lastAt == lastAt &&
      other.detail == detail;

    @override
    int get hashCode =>
        check.hashCode +
        count.hashCode +
        lastAt.hashCode +
        (detail == null ? 0 : detail.hashCode);

  factory DeceptionPayloadAllOfChecks.fromJson(Map<String, dynamic> json) => _$DeceptionPayloadAllOfChecksFromJson(json);

  Map<String, dynamic> toJson() => _$DeceptionPayloadAllOfChecksToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DeceptionPayloadAllOfChecksCheckEnum {
@JsonValue(r'duplicate-mmsi')
duplicateMmsi(r'duplicate-mmsi'),
@JsonValue(r'vessel-implied-speed')
vesselImpliedSpeed(r'vessel-implied-speed'),
@JsonValue(r'aircraft-implied-speed')
aircraftImpliedSpeed(r'aircraft-implied-speed');

const DeceptionPayloadAllOfChecksCheckEnum(this.value);

final String value;

@override
String toString() => value;
}


