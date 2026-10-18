//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'received_package_sender.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReceivedPackageSender {
  /// Returns a new [ReceivedPackageSender] instance.
  ReceivedPackageSender({

    required  this.instanceId,

     this.instanceName,

    required  this.keyId,

     this.peerId,
  });

  @JsonKey(
    
    name: r'instanceId',
    required: true,
    includeIfNull: false,
  )


  final String instanceId;



  @JsonKey(
    
    name: r'instanceName',
    required: false,
    includeIfNull: false,
  )


  final String? instanceName;



  @JsonKey(
    
    name: r'keyId',
    required: true,
    includeIfNull: false,
  )


  final String keyId;



  @JsonKey(
    
    name: r'peerId',
    required: false,
    includeIfNull: false,
  )


  final String? peerId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReceivedPackageSender &&
      other.instanceId == instanceId &&
      other.instanceName == instanceName &&
      other.keyId == keyId &&
      other.peerId == peerId;

    @override
    int get hashCode =>
        instanceId.hashCode +
        (instanceName == null ? 0 : instanceName.hashCode) +
        keyId.hashCode +
        (peerId == null ? 0 : peerId.hashCode);

  factory ReceivedPackageSender.fromJson(Map<String, dynamic> json) => _$ReceivedPackageSenderFromJson(json);

  Map<String, dynamic> toJson() => _$ReceivedPackageSenderToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

