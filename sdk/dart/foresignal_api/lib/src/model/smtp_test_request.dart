//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'smtp_test_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SmtpTestRequest {
  /// Returns a new [SmtpTestRequest] instance.
  SmtpTestRequest({

    required  this.to,
  });

      /// Recipient of the test message
  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final String to;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SmtpTestRequest &&
      other.to == to;

    @override
    int get hashCode =>
        to.hashCode;

  factory SmtpTestRequest.fromJson(Map<String, dynamic> json) => _$SmtpTestRequestFromJson(json);

  Map<String, dynamic> toJson() => _$SmtpTestRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

