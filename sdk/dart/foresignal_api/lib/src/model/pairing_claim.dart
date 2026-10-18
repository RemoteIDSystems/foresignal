//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/device_info.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'pairing_claim.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PairingClaim {
  /// Returns a new [PairingClaim] instance.
  PairingClaim({

    required  this.code,

    required  this.device,
  });

      /// XXXX-XXXX (the dash is optional; case-insensitive)
  @JsonKey(
    
    name: r'code',
    required: true,
    includeIfNull: false,
  )


  final String code;



  @JsonKey(
    
    name: r'device',
    required: true,
    includeIfNull: false,
  )


  final DeviceInfo device;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PairingClaim &&
      other.code == code &&
      other.device == device;

    @override
    int get hashCode =>
        code.hashCode +
        device.hashCode;

  factory PairingClaim.fromJson(Map<String, dynamic> json) => _$PairingClaimFromJson(json);

  Map<String, dynamic> toJson() => _$PairingClaimToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

