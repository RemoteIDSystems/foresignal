//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'user_link_issued.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UserLinkIssued {
  /// Returns a new [UserLinkIssued] instance.
  UserLinkIssued({

    required  this.kind,

    required  this.emailed,

     this.token,

    required  this.tokenPrefix,

    required  this.expiresAt,
  });

  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final String kind;



      /// True when the email left; the token is then withheld
  @JsonKey(
    
    name: r'emailed',
    required: true,
    includeIfNull: false,
  )


  final bool emailed;



      /// The one-time token (`fsv_…` activation, `fsw_…` reset), only when not emailed
  @JsonKey(
    
    name: r'token',
    required: false,
    includeIfNull: false,
  )


  final String? token;



      /// The token's first eight characters
  @JsonKey(
    
    name: r'tokenPrefix',
    required: true,
    includeIfNull: false,
  )


  final String tokenPrefix;



  @JsonKey(
    
    name: r'expiresAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime expiresAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UserLinkIssued &&
      other.kind == kind &&
      other.emailed == emailed &&
      other.token == token &&
      other.tokenPrefix == tokenPrefix &&
      other.expiresAt == expiresAt;

    @override
    int get hashCode =>
        kind.hashCode +
        emailed.hashCode +
        (token == null ? 0 : token.hashCode) +
        tokenPrefix.hashCode +
        expiresAt.hashCode;

  factory UserLinkIssued.fromJson(Map<String, dynamic> json) => _$UserLinkIssuedFromJson(json);

  Map<String, dynamic> toJson() => _$UserLinkIssuedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

