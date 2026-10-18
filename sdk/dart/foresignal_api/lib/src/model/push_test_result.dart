//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_test_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushTestResult {
  /// Returns a new [PushTestResult] instance.
  PushTestResult({

    required  this.success,

     this.credentialMinted,

     this.sent,

     this.status,

     this.reason,

     this.error,
  });

  @JsonKey(
    
    name: r'success',
    required: true,
    includeIfNull: false,
  )


  final bool success;



      /// APNs JWT or FCM OAuth token obtained
  @JsonKey(
    
    name: r'credentialMinted',
    required: false,
    includeIfNull: false,
  )


  final bool? credentialMinted;



      /// null when no device was targeted
  @JsonKey(
    
    name: r'sent',
    required: false,
    includeIfNull: false,
  )


  final bool? sent;



      /// Push service HTTP status
  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final int? status;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushTestResult &&
      other.success == success &&
      other.credentialMinted == credentialMinted &&
      other.sent == sent &&
      other.status == status &&
      other.reason == reason &&
      other.error == error;

    @override
    int get hashCode =>
        success.hashCode +
        credentialMinted.hashCode +
        (sent == null ? 0 : sent.hashCode) +
        (status == null ? 0 : status.hashCode) +
        (reason == null ? 0 : reason.hashCode) +
        (error == null ? 0 : error.hashCode);

  factory PushTestResult.fromJson(Map<String, dynamic> json) => _$PushTestResultFromJson(json);

  Map<String, dynamic> toJson() => _$PushTestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

