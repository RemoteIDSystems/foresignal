//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'share_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ShareRequest {
  /// Returns a new [ShareRequest] instance.
  ShareRequest({

    required  this.grantees,

     this.role,

     this.message,

     this.expiresAt,
  });

  @JsonKey(
    
    name: r'grantees',
    required: true,
    includeIfNull: false,
  )


  final List<String> grantees;



  @JsonKey(
    
    name: r'role',
    required: false,
    includeIfNull: false,
  )


  final ShareRequestRoleEnum? role;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ShareRequest &&
      other.grantees == grantees &&
      other.role == role &&
      other.message == message &&
      other.expiresAt == expiresAt;

    @override
    int get hashCode =>
        grantees.hashCode +
        role.hashCode +
        (message == null ? 0 : message.hashCode) +
        (expiresAt == null ? 0 : expiresAt.hashCode);

  factory ShareRequest.fromJson(Map<String, dynamic> json) => _$ShareRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ShareRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ShareRequestRoleEnum {
@JsonValue(r'viewer')
viewer(r'viewer'),
@JsonValue(r'commenter')
commenter(r'commenter'),
@JsonValue(r'editor')
editor(r'editor');

const ShareRequestRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


