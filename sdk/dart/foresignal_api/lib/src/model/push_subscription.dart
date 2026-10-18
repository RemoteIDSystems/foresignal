//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/push_filters.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_subscription.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushSubscription {
  /// Returns a new [PushSubscription] instance.
  PushSubscription({

    required  this.id,

     this.name,

    required  this.endpointHost,

     this.filters,

    required  this.createdAt,

     this.lastSuccessAt,

     this.status,

     this.transportLabel,

     this.payloadFormat,

     this.lang,

     this.failureCount,

     this.lastFailureAt,

     this.lastStatus,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'endpointHost',
    required: true,
    includeIfNull: false,
  )


  final String endpointHost;



  @JsonKey(
    
    name: r'filters',
    required: false,
    includeIfNull: false,
  )


  final PushFilters? filters;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'lastSuccessAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastSuccessAt;



      /// pending until the verification code is echoed; active receives notifications; disabled after three consecutive failed send-time address rechecks (URL_NOT_ALLOWED)
  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final PushSubscriptionStatusEnum? status;



  @JsonKey(
    
    name: r'transportLabel',
    required: false,
    includeIfNull: false,
  )


  final PushSubscriptionTransportLabelEnum? transportLabel;



  @JsonKey(
    
    name: r'payloadFormat',
    required: false,
    includeIfNull: false,
  )


  final PushSubscriptionPayloadFormatEnum? payloadFormat;



  @JsonKey(
    
    name: r'lang',
    required: false,
    includeIfNull: false,
  )


  final String? lang;



  @JsonKey(
    
    name: r'failureCount',
    required: false,
    includeIfNull: false,
  )


  final int? failureCount;



  @JsonKey(
    
    name: r'lastFailureAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastFailureAt;



      /// Last push-service HTTP status
  @JsonKey(
    
    name: r'lastStatus',
    required: false,
    includeIfNull: false,
  )


  final int? lastStatus;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushSubscription &&
      other.id == id &&
      other.name == name &&
      other.endpointHost == endpointHost &&
      other.filters == filters &&
      other.createdAt == createdAt &&
      other.lastSuccessAt == lastSuccessAt &&
      other.status == status &&
      other.transportLabel == transportLabel &&
      other.payloadFormat == payloadFormat &&
      other.lang == lang &&
      other.failureCount == failureCount &&
      other.lastFailureAt == lastFailureAt &&
      other.lastStatus == lastStatus;

    @override
    int get hashCode =>
        id.hashCode +
        (name == null ? 0 : name.hashCode) +
        endpointHost.hashCode +
        filters.hashCode +
        createdAt.hashCode +
        (lastSuccessAt == null ? 0 : lastSuccessAt.hashCode) +
        status.hashCode +
        transportLabel.hashCode +
        payloadFormat.hashCode +
        (lang == null ? 0 : lang.hashCode) +
        failureCount.hashCode +
        (lastFailureAt == null ? 0 : lastFailureAt.hashCode) +
        (lastStatus == null ? 0 : lastStatus.hashCode);

  factory PushSubscription.fromJson(Map<String, dynamic> json) => _$PushSubscriptionFromJson(json);

  Map<String, dynamic> toJson() => _$PushSubscriptionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// pending until the verification code is echoed; active receives notifications; disabled after three consecutive failed send-time address rechecks (URL_NOT_ALLOWED)
enum PushSubscriptionStatusEnum {
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'disabled')
disabled(r'disabled');

const PushSubscriptionStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PushSubscriptionTransportLabelEnum {
@JsonValue(r'browser')
browser(r'browser'),
@JsonValue(r'unifiedpush')
unifiedpush(r'unifiedpush'),
@JsonValue(r'other')
other(r'other');

const PushSubscriptionTransportLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PushSubscriptionPayloadFormatEnum {
@JsonValue(r'foresignal')
foresignal(r'foresignal'),
@JsonValue(r'declarative')
declarative(r'declarative');

const PushSubscriptionPayloadFormatEnum(this.value);

final String value;

@override
String toString() => value;
}


