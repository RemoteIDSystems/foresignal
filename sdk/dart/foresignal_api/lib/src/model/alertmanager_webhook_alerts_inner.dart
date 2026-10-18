//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'alertmanager_webhook_alerts_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AlertmanagerWebhookAlertsInner {
  /// Returns a new [AlertmanagerWebhookAlertsInner] instance.
  AlertmanagerWebhookAlertsInner({

    required  this.status,

    required  this.labels,

     this.annotations,

    required  this.startsAt,

     this.endsAt,

     this.generatorURL,

    required  this.fingerprint,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final AlertmanagerWebhookAlertsInnerStatusEnum status;



      /// `latitude`/`longitude` (or `lat`/`lon`) become a point geometry
  @JsonKey(
    
    name: r'labels',
    required: true,
    includeIfNull: false,
  )


  final Map<String, String> labels;



  @JsonKey(
    
    name: r'annotations',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? annotations;



  @JsonKey(
    
    name: r'startsAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime startsAt;



  @JsonKey(
    
    name: r'endsAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? endsAt;



  @JsonKey(
    
    name: r'generatorURL',
    required: false,
    includeIfNull: false,
  )


  final String? generatorURL;



  @JsonKey(
    
    name: r'fingerprint',
    required: true,
    includeIfNull: false,
  )


  final String fingerprint;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AlertmanagerWebhookAlertsInner &&
      other.status == status &&
      other.labels == labels &&
      other.annotations == annotations &&
      other.startsAt == startsAt &&
      other.endsAt == endsAt &&
      other.generatorURL == generatorURL &&
      other.fingerprint == fingerprint;

    @override
    int get hashCode =>
        status.hashCode +
        labels.hashCode +
        annotations.hashCode +
        startsAt.hashCode +
        endsAt.hashCode +
        generatorURL.hashCode +
        fingerprint.hashCode;

  factory AlertmanagerWebhookAlertsInner.fromJson(Map<String, dynamic> json) => _$AlertmanagerWebhookAlertsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$AlertmanagerWebhookAlertsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AlertmanagerWebhookAlertsInnerStatusEnum {
@JsonValue(r'firing')
firing(r'firing'),
@JsonValue(r'resolved')
resolved(r'resolved');

const AlertmanagerWebhookAlertsInnerStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


