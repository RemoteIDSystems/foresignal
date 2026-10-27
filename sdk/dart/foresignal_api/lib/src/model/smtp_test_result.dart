//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'smtp_test_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SmtpTestResult {
  /// Returns a new [SmtpTestResult] instance.
  SmtpTestResult({

    required  this.ok,

     this.message,

     this.sentAt,
  });

  @JsonKey(
    
    name: r'ok',
    required: true,
    includeIfNull: false,
  )


  final bool ok;



      /// Server reply or failure reason
  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



  @JsonKey(
    
    name: r'sentAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? sentAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SmtpTestResult &&
      other.ok == ok &&
      other.message == message &&
      other.sentAt == sentAt;

    @override
    int get hashCode =>
        ok.hashCode +
        (message == null ? 0 : message.hashCode) +
        (sentAt == null ? 0 : sentAt.hashCode);

  factory SmtpTestResult.fromJson(Map<String, dynamic> json) => _$SmtpTestResultFromJson(json);

  Map<String, dynamic> toJson() => _$SmtpTestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

