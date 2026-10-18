//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exchange_package_deliveries_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExchangePackageDeliveriesInner {
  /// Returns a new [ExchangePackageDeliveriesInner] instance.
  ExchangePackageDeliveriesInner({

    required  this.instanceId,

     this.peerId,

    required  this.status,

     this.at,

     this.error,
  });

  @JsonKey(
    
    name: r'instanceId',
    required: true,
    includeIfNull: false,
  )


  final String instanceId;



  @JsonKey(
    
    name: r'peerId',
    required: false,
    includeIfNull: false,
  )


  final String? peerId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ExchangePackageDeliveriesInnerStatusEnum status;



  @JsonKey(
    
    name: r'at',
    required: false,
    includeIfNull: false,
  )


  final DateTime? at;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExchangePackageDeliveriesInner &&
      other.instanceId == instanceId &&
      other.peerId == peerId &&
      other.status == status &&
      other.at == at &&
      other.error == error;

    @override
    int get hashCode =>
        instanceId.hashCode +
        (peerId == null ? 0 : peerId.hashCode) +
        status.hashCode +
        (at == null ? 0 : at.hashCode) +
        (error == null ? 0 : error.hashCode);

  factory ExchangePackageDeliveriesInner.fromJson(Map<String, dynamic> json) => _$ExchangePackageDeliveriesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ExchangePackageDeliveriesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExchangePackageDeliveriesInnerStatusEnum {
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'delivered')
delivered(r'delivered'),
@JsonValue(r'acknowledged')
acknowledged(r'acknowledged'),
@JsonValue(r'failed')
failed(r'failed');

const ExchangePackageDeliveriesInnerStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


