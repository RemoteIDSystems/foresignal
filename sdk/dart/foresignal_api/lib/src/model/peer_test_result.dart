//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/federation_info.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'peer_test_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PeerTestResult {
  /// Returns a new [PeerTestResult] instance.
  PeerTestResult({

    required  this.ok,

    required  this.reachable,

     this.latencyMs,

     this.info,

     this.signatureValid,

     this.kid,

     this.pinnedKid,

     this.keyMatches,

     this.error,
  });

  @JsonKey(
    
    name: r'ok',
    required: true,
    includeIfNull: false,
  )


  final bool ok;



  @JsonKey(
    
    name: r'reachable',
    required: true,
    includeIfNull: false,
  )


  final bool reachable;



  @JsonKey(
    
    name: r'latencyMs',
    required: false,
    includeIfNull: false,
  )


  final int? latencyMs;



  @JsonKey(
    
    name: r'info',
    required: false,
    includeIfNull: false,
  )


  final FederationInfo? info;



  @JsonKey(
    
    name: r'signatureValid',
    required: false,
    includeIfNull: false,
  )


  final bool? signatureValid;



  @JsonKey(
    
    name: r'kid',
    required: false,
    includeIfNull: false,
  )


  final String? kid;



  @JsonKey(
    
    name: r'pinnedKid',
    required: false,
    includeIfNull: false,
  )


  final String? pinnedKid;



  @JsonKey(
    
    name: r'keyMatches',
    required: false,
    includeIfNull: false,
  )


  final bool? keyMatches;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PeerTestResult &&
      other.ok == ok &&
      other.reachable == reachable &&
      other.latencyMs == latencyMs &&
      other.info == info &&
      other.signatureValid == signatureValid &&
      other.kid == kid &&
      other.pinnedKid == pinnedKid &&
      other.keyMatches == keyMatches &&
      other.error == error;

    @override
    int get hashCode =>
        ok.hashCode +
        reachable.hashCode +
        (latencyMs == null ? 0 : latencyMs.hashCode) +
        (info == null ? 0 : info.hashCode) +
        (signatureValid == null ? 0 : signatureValid.hashCode) +
        (kid == null ? 0 : kid.hashCode) +
        (pinnedKid == null ? 0 : pinnedKid.hashCode) +
        (keyMatches == null ? 0 : keyMatches.hashCode) +
        (error == null ? 0 : error.hashCode);

  factory PeerTestResult.fromJson(Map<String, dynamic> json) => _$PeerTestResultFromJson(json);

  Map<String, dynamic> toJson() => _$PeerTestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

