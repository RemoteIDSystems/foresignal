//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/session_token.dart';
import 'package:foresignal_api/src/model/user.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'setup_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SetupResult {
  /// Returns a new [SetupResult] instance.
  SetupResult({

    required  this.user,

     this.session,
  });

  @JsonKey(
    
    name: r'user',
    required: true,
    includeIfNull: false,
  )


  final User user;



      /// A session for the new admin; null only if the sign-in asked for a second factor
  @JsonKey(
    
    name: r'session',
    required: false,
    includeIfNull: false,
  )


  final SessionToken? session;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SetupResult &&
      other.user == user &&
      other.session == session;

    @override
    int get hashCode =>
        user.hashCode +
        (session == null ? 0 : session.hashCode);

  factory SetupResult.fromJson(Map<String, dynamic> json) => _$SetupResultFromJson(json);

  Map<String, dynamic> toJson() => _$SetupResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

