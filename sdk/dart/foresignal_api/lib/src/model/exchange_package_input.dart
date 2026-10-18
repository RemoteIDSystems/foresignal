//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/exchange_package_input_extract.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exchange_package_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExchangePackageInput {
  /// Returns a new [ExchangePackageInput] instance.
  ExchangePackageInput({

    required  this.objectRef,

    required  this.recipients,

     this.message,

     this.deliver,

     this.extract,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'objectRef',
    required: true,
    includeIfNull: false,
  )


  final String objectRef;



  @JsonKey(
    
    name: r'recipients',
    required: true,
    includeIfNull: true,
  )


  final Object? recipients;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



      /// Push to 113 peers or only build a downloadable file
  @JsonKey(
    
    name: r'deliver',
    required: false,
    includeIfNull: false,
  )


  final ExchangePackageInputDeliverEnum? deliver;



  @JsonKey(
    
    name: r'extract',
    required: false,
    includeIfNull: false,
  )


  final ExchangePackageInputExtract? extract;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExchangePackageInput &&
      other.objectRef == objectRef &&
      other.recipients == recipients &&
      other.message == message &&
      other.deliver == deliver &&
      other.extract == extract;

    @override
    int get hashCode =>
        objectRef.hashCode +
        (recipients == null ? 0 : recipients.hashCode) +
        (message == null ? 0 : message.hashCode) +
        deliver.hashCode +
        (extract == null ? 0 : extract.hashCode);

  factory ExchangePackageInput.fromJson(Map<String, dynamic> json) => _$ExchangePackageInputFromJson(json);

  Map<String, dynamic> toJson() => _$ExchangePackageInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Push to 113 peers or only build a downloadable file
enum ExchangePackageInputDeliverEnum {
@JsonValue(r'peer')
peer(r'peer'),
@JsonValue(r'file')
file(r'file');

const ExchangePackageInputDeliverEnum(this.value);

final String value;

@override
String toString() => value;
}


