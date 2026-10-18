//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exchange_receipt.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExchangeReceipt {
  /// Returns a new [ExchangeReceipt] instance.
  ExchangeReceipt({

    required  this.packageId,

    required  this.type,

    required  this.receiverInstanceId,

     this.principalHandle,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'packageId',
    required: true,
    includeIfNull: false,
  )


  final String packageId;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ExchangeReceiptTypeEnum type;



  @JsonKey(
    
    name: r'receiverInstanceId',
    required: true,
    includeIfNull: false,
  )


  final String receiverInstanceId;



  @JsonKey(
    
    name: r'principalHandle',
    required: false,
    includeIfNull: false,
  )


  final String? principalHandle;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExchangeReceipt &&
      other.packageId == packageId &&
      other.type == type &&
      other.receiverInstanceId == receiverInstanceId &&
      other.principalHandle == principalHandle &&
      other.at == at;

    @override
    int get hashCode =>
        packageId.hashCode +
        type.hashCode +
        receiverInstanceId.hashCode +
        (principalHandle == null ? 0 : principalHandle.hashCode) +
        at.hashCode;

  factory ExchangeReceipt.fromJson(Map<String, dynamic> json) => _$ExchangeReceiptFromJson(json);

  Map<String, dynamic> toJson() => _$ExchangeReceiptToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExchangeReceiptTypeEnum {
@JsonValue(r'delivered')
delivered(r'delivered'),
@JsonValue(r'acknowledged')
acknowledged(r'acknowledged');

const ExchangeReceiptTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


