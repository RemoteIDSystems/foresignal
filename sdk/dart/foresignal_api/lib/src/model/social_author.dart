//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_author.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialAuthor {
  /// Returns a new [SocialAuthor] instance.
  SocialAuthor({

    required  this.handle,

     this.displayName,

    required  this.accountId,

     this.accountCreatedAt,

     this.followers,

     this.verified,
  });

  @JsonKey(
    
    name: r'handle',
    required: true,
    includeIfNull: false,
  )


  final String handle;



  @JsonKey(
    
    name: r'displayName',
    required: false,
    includeIfNull: false,
  )


  final String? displayName;



      /// Platform account id
  @JsonKey(
    
    name: r'accountId',
    required: true,
    includeIfNull: false,
  )


  final String accountId;



  @JsonKey(
    
    name: r'accountCreatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? accountCreatedAt;



          // minimum: 0
  @JsonKey(
    
    name: r'followers',
    required: false,
    includeIfNull: false,
  )


  final int? followers;



  @JsonKey(
    
    name: r'verified',
    required: false,
    includeIfNull: false,
  )


  final bool? verified;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialAuthor &&
      other.handle == handle &&
      other.displayName == displayName &&
      other.accountId == accountId &&
      other.accountCreatedAt == accountCreatedAt &&
      other.followers == followers &&
      other.verified == verified;

    @override
    int get hashCode =>
        handle.hashCode +
        (displayName == null ? 0 : displayName.hashCode) +
        accountId.hashCode +
        (accountCreatedAt == null ? 0 : accountCreatedAt.hashCode) +
        (followers == null ? 0 : followers.hashCode) +
        (verified == null ? 0 : verified.hashCode);

  factory SocialAuthor.fromJson(Map<String, dynamic> json) => _$SocialAuthorFromJson(json);

  Map<String, dynamic> toJson() => _$SocialAuthorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

