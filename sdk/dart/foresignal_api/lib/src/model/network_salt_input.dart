//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'network_salt_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NetworkSaltInput {
  /// Returns a new [NetworkSaltInput] instance.
  NetworkSaltInput({

    required  this.networkId,

    required  this.saltRef,
  });

  @JsonKey(
    
    name: r'networkId',
    required: true,
    includeIfNull: false,
  )


  final String networkId;



      /// The shared salt value (or secret reference); never returned
  @JsonKey(
    
    name: r'saltRef',
    required: true,
    includeIfNull: false,
  )


  final String saltRef;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NetworkSaltInput &&
      other.networkId == networkId &&
      other.saltRef == saltRef;

    @override
    int get hashCode =>
        networkId.hashCode +
        saltRef.hashCode;

  factory NetworkSaltInput.fromJson(Map<String, dynamic> json) => _$NetworkSaltInputFromJson(json);

  Map<String, dynamic> toJson() => _$NetworkSaltInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

