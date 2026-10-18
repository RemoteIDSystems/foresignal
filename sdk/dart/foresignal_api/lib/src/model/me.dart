//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/notification_preference.dart';
import 'package:foresignal_api/src/model/me_groups_inner.dart';
import 'package:foresignal_api/src/model/me_token.dart';
import 'package:foresignal_api/src/model/principal.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'me.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Me {
  /// Returns a new [Me] instance.
  Me({

    required  this.principal,

    required  this.token,

    required  this.scopes,

    required  this.groups,

    required  this.notificationPreferences,

     this.defaultTlp,
  });

  @JsonKey(
    
    name: r'principal',
    required: true,
    includeIfNull: false,
  )


  final Principal principal;



  @JsonKey(
    
    name: r'token',
    required: true,
    includeIfNull: false,
  )


  final MeToken token;



      /// Scopes the token may call (scope hierarchy expanded)
  @JsonKey(
    
    name: r'scopes',
    required: true,
    includeIfNull: false,
  )


  final List<MeScopesEnum> scopes;



  @JsonKey(
    
    name: r'groups',
    required: true,
    includeIfNull: false,
  )


  final List<MeGroupsInner> groups;



  @JsonKey(
    
    name: r'notificationPreferences',
    required: true,
    includeIfNull: false,
  )


  final List<NotificationPreference> notificationPreferences;



  @JsonKey(
    
    name: r'defaultTlp',
    required: false,
    includeIfNull: false,
  )


  final Tlp? defaultTlp;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Me &&
      other.principal == principal &&
      other.token == token &&
      other.scopes == scopes &&
      other.groups == groups &&
      other.notificationPreferences == notificationPreferences &&
      other.defaultTlp == defaultTlp;

    @override
    int get hashCode =>
        principal.hashCode +
        token.hashCode +
        scopes.hashCode +
        groups.hashCode +
        notificationPreferences.hashCode +
        defaultTlp.hashCode;

  factory Me.fromJson(Map<String, dynamic> json) => _$MeFromJson(json);

  Map<String, dynamic> toJson() => _$MeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MeScopesEnum {
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'admin')
admin(r'admin');

const MeScopesEnum(this.value);

final String value;

@override
String toString() => value;
}


