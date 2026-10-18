//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'access_request_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AccessRequestInput {
  /// Returns a new [AccessRequestInput] instance.
  AccessRequestInput({

     this.message,

     this.offerShare,
  });

  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



      /// Chronicle id the requester offers to share
  @JsonKey(
    
    name: r'offerShare',
    required: false,
    includeIfNull: false,
  )


  final String? offerShare;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AccessRequestInput &&
      other.message == message &&
      other.offerShare == offerShare;

    @override
    int get hashCode =>
        (message == null ? 0 : message.hashCode) +
        (offerShare == null ? 0 : offerShare.hashCode);

  factory AccessRequestInput.fromJson(Map<String, dynamic> json) => _$AccessRequestInputFromJson(json);

  Map<String, dynamic> toJson() => _$AccessRequestInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

