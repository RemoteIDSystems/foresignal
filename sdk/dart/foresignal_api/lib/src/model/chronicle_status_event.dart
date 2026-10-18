//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_status_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleStatusEvent {
  /// Returns a new [ChronicleStatusEvent] instance.
  ChronicleStatusEvent({

    required  this.chronicleId,

    required  this.status,

     this.previousStatus,

     this.capState,

     this.bytesUsed,

     this.reason,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'chronicleId',
    required: true,
    includeIfNull: false,
  )


  final String chronicleId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ChronicleStatusEventStatusEnum status;



  @JsonKey(
    
    name: r'previousStatus',
    required: false,
    includeIfNull: false,
  )


  final ChronicleStatusEventPreviousStatusEnum? previousStatus;



  @JsonKey(
    
    name: r'capState',
    required: false,
    includeIfNull: false,
  )


  final ChronicleStatusEventCapStateEnum? capState;



  @JsonKey(
    
    name: r'bytesUsed',
    required: false,
    includeIfNull: false,
  )


  final int? bytesUsed;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleStatusEvent &&
      other.chronicleId == chronicleId &&
      other.status == status &&
      other.previousStatus == previousStatus &&
      other.capState == capState &&
      other.bytesUsed == bytesUsed &&
      other.reason == reason &&
      other.at == at;

    @override
    int get hashCode =>
        chronicleId.hashCode +
        status.hashCode +
        (previousStatus == null ? 0 : previousStatus.hashCode) +
        capState.hashCode +
        bytesUsed.hashCode +
        (reason == null ? 0 : reason.hashCode) +
        at.hashCode;

  factory ChronicleStatusEvent.fromJson(Map<String, dynamic> json) => _$ChronicleStatusEventFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleStatusEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleStatusEventStatusEnum {
@JsonValue(r'proposed')
proposed(r'proposed'),
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'paused')
paused(r'paused'),
@JsonValue(r'stopped')
stopped(r'stopped'),
@JsonValue(r'archived')
archived(r'archived');

const ChronicleStatusEventStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChronicleStatusEventPreviousStatusEnum {
@JsonValue(r'proposed')
proposed(r'proposed'),
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'paused')
paused(r'paused'),
@JsonValue(r'stopped')
stopped(r'stopped'),
@JsonValue(r'archived')
archived(r'archived');

const ChronicleStatusEventPreviousStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChronicleStatusEventCapStateEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'exceeded')
exceeded(r'exceeded');

const ChronicleStatusEventCapStateEnum(this.value);

final String value;

@override
String toString() => value;
}


