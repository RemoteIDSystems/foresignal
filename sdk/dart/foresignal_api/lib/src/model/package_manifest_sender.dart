//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'package_manifest_sender.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PackageManifestSender {
  /// Returns a new [PackageManifestSender] instance.
  PackageManifestSender({

    required  this.instanceId,

     this.instanceName,

    required  this.keyId,
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



      /// Kid in the sender's 109 JWKS
  @JsonKey(
    
    name: r'keyId',
    required: true,
    includeIfNull: false,
  )


  final String keyId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PackageManifestSender &&
      other.instanceId == instanceId &&
      other.instanceName == instanceName &&
      other.keyId == keyId;

    @override
    int get hashCode =>
        instanceId.hashCode +
        (instanceName == null ? 0 : instanceName.hashCode) +
        keyId.hashCode;

  factory PackageManifestSender.fromJson(Map<String, dynamic> json) => _$PackageManifestSenderFromJson(json);

  Map<String, dynamic> toJson() => _$PackageManifestSenderToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

