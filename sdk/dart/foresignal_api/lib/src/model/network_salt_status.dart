//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'network_salt_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NetworkSaltStatus {
  /// Returns a new [NetworkSaltStatus] instance.
  NetworkSaltStatus({

    required  this.networkId,

    required  this.saltId,

    required  this.createdAt,

    required  this.rotatesAt,
  });

  @JsonKey(
    
    name: r'networkId',
    required: true,
    includeIfNull: false,
  )


  final String networkId;



  @JsonKey(
    
    name: r'saltId',
    required: true,
    includeIfNull: false,
  )


  final String saltId;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'rotatesAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime rotatesAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NetworkSaltStatus &&
      other.networkId == networkId &&
      other.saltId == saltId &&
      other.createdAt == createdAt &&
      other.rotatesAt == rotatesAt;

    @override
    int get hashCode =>
        networkId.hashCode +
        saltId.hashCode +
        createdAt.hashCode +
        rotatesAt.hashCode;

  factory NetworkSaltStatus.fromJson(Map<String, dynamic> json) => _$NetworkSaltStatusFromJson(json);

  Map<String, dynamic> toJson() => _$NetworkSaltStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

