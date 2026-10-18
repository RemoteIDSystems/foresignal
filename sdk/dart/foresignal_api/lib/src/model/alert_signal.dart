//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'alert_signal.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AlertSignal {
  /// Returns a new [AlertSignal] instance.
  AlertSignal({

    required  this.alertId,

    required  this.type,

    required  this.at,
  });

  @JsonKey(
    
    name: r'alertId',
    required: true,
    includeIfNull: false,
  )


  final int alertId;



      /// ACK_FAST acknowledged within 15 min; SNOOZED alert or rule snoozed; FLAP resolved via inverse_edge within 10 min; UNOPENED notification unread after 24 h
  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final AlertSignalTypeEnum type;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AlertSignal &&
      other.alertId == alertId &&
      other.type == type &&
      other.at == at;

    @override
    int get hashCode =>
        alertId.hashCode +
        type.hashCode +
        at.hashCode;

  factory AlertSignal.fromJson(Map<String, dynamic> json) => _$AlertSignalFromJson(json);

  Map<String, dynamic> toJson() => _$AlertSignalToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// ACK_FAST acknowledged within 15 min; SNOOZED alert or rule snoozed; FLAP resolved via inverse_edge within 10 min; UNOPENED notification unread after 24 h
enum AlertSignalTypeEnum {
@JsonValue(r'ACK_FAST')
ACK_FAST(r'ACK_FAST'),
@JsonValue(r'SNOOZED')
SNOOZED(r'SNOOZED'),
@JsonValue(r'FLAP')
FLAP(r'FLAP'),
@JsonValue(r'UNOPENED')
UNOPENED(r'UNOPENED');

const AlertSignalTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


