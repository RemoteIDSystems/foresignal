//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'egress_test_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EgressTestResult {
  /// Returns a new [EgressTestResult] instance.
  EgressTestResult({

    required  this.ok,

     this.exitIp,

     this.latencyMs,

     this.message,

     this.checkedAt,
  });

  @JsonKey(
    
    name: r'ok',
    required: true,
    includeIfNull: false,
  )


  final bool ok;



      /// The public IP the route presented on the test call
  @JsonKey(
    
    name: r'exitIp',
    required: false,
    includeIfNull: false,
  )


  final String? exitIp;



  @JsonKey(
    
    name: r'latencyMs',
    required: false,
    includeIfNull: false,
  )


  final num? latencyMs;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



  @JsonKey(
    
    name: r'checkedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? checkedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EgressTestResult &&
      other.ok == ok &&
      other.exitIp == exitIp &&
      other.latencyMs == latencyMs &&
      other.message == message &&
      other.checkedAt == checkedAt;

    @override
    int get hashCode =>
        ok.hashCode +
        (exitIp == null ? 0 : exitIp.hashCode) +
        (latencyMs == null ? 0 : latencyMs.hashCode) +
        (message == null ? 0 : message.hashCode) +
        checkedAt.hashCode;

  factory EgressTestResult.fromJson(Map<String, dynamic> json) => _$EgressTestResultFromJson(json);

  Map<String, dynamic> toJson() => _$EgressTestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

