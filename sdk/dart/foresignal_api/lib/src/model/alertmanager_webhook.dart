//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/alertmanager_webhook_alerts_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'alertmanager_webhook.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AlertmanagerWebhook {
  /// Returns a new [AlertmanagerWebhook] instance.
  AlertmanagerWebhook({

    required  this.version,

     this.groupKey,

     this.truncatedAlerts,

     this.status,

     this.receiver,

     this.groupLabels,

     this.commonLabels,

     this.commonAnnotations,

     this.externalURL,

    required  this.alerts,
  });

  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final AlertmanagerWebhookVersionEnum version;



  @JsonKey(
    
    name: r'groupKey',
    required: false,
    includeIfNull: false,
  )


  final String? groupKey;



  @JsonKey(
    
    name: r'truncatedAlerts',
    required: false,
    includeIfNull: false,
  )


  final int? truncatedAlerts;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final AlertmanagerWebhookStatusEnum? status;



  @JsonKey(
    
    name: r'receiver',
    required: false,
    includeIfNull: false,
  )


  final String? receiver;



  @JsonKey(
    
    name: r'groupLabels',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? groupLabels;



  @JsonKey(
    
    name: r'commonLabels',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? commonLabels;



  @JsonKey(
    
    name: r'commonAnnotations',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? commonAnnotations;



  @JsonKey(
    
    name: r'externalURL',
    required: false,
    includeIfNull: false,
  )


  final String? externalURL;



  @JsonKey(
    
    name: r'alerts',
    required: true,
    includeIfNull: false,
  )


  final List<AlertmanagerWebhookAlertsInner> alerts;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AlertmanagerWebhook &&
      other.version == version &&
      other.groupKey == groupKey &&
      other.truncatedAlerts == truncatedAlerts &&
      other.status == status &&
      other.receiver == receiver &&
      other.groupLabels == groupLabels &&
      other.commonLabels == commonLabels &&
      other.commonAnnotations == commonAnnotations &&
      other.externalURL == externalURL &&
      other.alerts == alerts;

    @override
    int get hashCode =>
        version.hashCode +
        groupKey.hashCode +
        truncatedAlerts.hashCode +
        status.hashCode +
        receiver.hashCode +
        groupLabels.hashCode +
        commonLabels.hashCode +
        commonAnnotations.hashCode +
        externalURL.hashCode +
        alerts.hashCode;

  factory AlertmanagerWebhook.fromJson(Map<String, dynamic> json) => _$AlertmanagerWebhookFromJson(json);

  Map<String, dynamic> toJson() => _$AlertmanagerWebhookToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AlertmanagerWebhookVersionEnum {
@JsonValue(r'4')
n4(r'4');

const AlertmanagerWebhookVersionEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AlertmanagerWebhookStatusEnum {
@JsonValue(r'firing')
firing(r'firing'),
@JsonValue(r'resolved')
resolved(r'resolved');

const AlertmanagerWebhookStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


