//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'briefing_run_delivery_results_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BriefingRunDeliveryResultsInner {
  /// Returns a new [BriefingRunDeliveryResultsInner] instance.
  BriefingRunDeliveryResultsInner({

    required  this.kind,

     this.channelId,

    required  this.format,

    required  this.status,

     this.notificationId,

     this.url,

     this.error,
  });

  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final BriefingRunDeliveryResultsInnerKindEnum kind;



  @JsonKey(
    
    name: r'channelId',
    required: false,
    includeIfNull: false,
  )


  final String? channelId;



  @JsonKey(
    
    name: r'format',
    required: true,
    includeIfNull: false,
  )


  final BriefingRunDeliveryResultsInnerFormatEnum format;



      /// `queued` handed to the channel (its own delivery is in the notification's deliveries); `available` a pulled target (`website`, `podcast`) the run can be read at; `skipped` not attempted (`error` says why: a missing, disabled or foreign channel, no audio); `failed` the send failed (`error`). One target's failure never stops the others
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final BriefingRunDeliveryResultsInnerStatusEnum status;



      /// The notification published to the channel
  @JsonKey(
    
    name: r'notificationId',
    required: false,
    includeIfNull: false,
  )


  final int? notificationId;



      /// Where a pulled target is available
  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BriefingRunDeliveryResultsInner &&
      other.kind == kind &&
      other.channelId == channelId &&
      other.format == format &&
      other.status == status &&
      other.notificationId == notificationId &&
      other.url == url &&
      other.error == error;

    @override
    int get hashCode =>
        kind.hashCode +
        (channelId == null ? 0 : channelId.hashCode) +
        format.hashCode +
        status.hashCode +
        (notificationId == null ? 0 : notificationId.hashCode) +
        (url == null ? 0 : url.hashCode) +
        (error == null ? 0 : error.hashCode);

  factory BriefingRunDeliveryResultsInner.fromJson(Map<String, dynamic> json) => _$BriefingRunDeliveryResultsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$BriefingRunDeliveryResultsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BriefingRunDeliveryResultsInnerKindEnum {
@JsonValue(r'channel')
channel(r'channel'),
@JsonValue(r'website')
website(r'website'),
@JsonValue(r'podcast')
podcast(r'podcast');

const BriefingRunDeliveryResultsInnerKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BriefingRunDeliveryResultsInnerFormatEnum {
@JsonValue(r'email')
email(r'email'),
@JsonValue(r'push')
push(r'push'),
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'html')
html(r'html'),
@JsonValue(r'audio')
audio(r'audio');

const BriefingRunDeliveryResultsInnerFormatEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `queued` handed to the channel (its own delivery is in the notification's deliveries); `available` a pulled target (`website`, `podcast`) the run can be read at; `skipped` not attempted (`error` says why: a missing, disabled or foreign channel, no audio); `failed` the send failed (`error`). One target's failure never stops the others
enum BriefingRunDeliveryResultsInnerStatusEnum {
@JsonValue(r'queued')
queued(r'queued'),
@JsonValue(r'available')
available(r'available'),
@JsonValue(r'skipped')
skipped(r'skipped'),
@JsonValue(r'failed')
failed(r'failed');

const BriefingRunDeliveryResultsInnerStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


