//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/user_data_restriction.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'user_update.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UserUpdate {
  /// Returns a new [UserUpdate] instance.
  UserUpdate({

     this.fullName,

     this.email,

     this.dataRestriction,
  });

  @JsonKey(
    
    name: r'fullName',
    required: false,
    includeIfNull: false,
  )


  final String? fullName;



  @JsonKey(
    
    name: r'email',
    required: false,
    includeIfNull: false,
  )


  final String? email;



      /// Replaces the user's data restriction; null lifts it, absent leaves it (R2-013)
  @JsonKey(
    
    name: r'dataRestriction',
    required: false,
    includeIfNull: false,
  )


  final UserDataRestriction? dataRestriction;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UserUpdate &&
      other.fullName == fullName &&
      other.email == email &&
      other.dataRestriction == dataRestriction;

    @override
    int get hashCode =>
        (fullName == null ? 0 : fullName.hashCode) +
        (email == null ? 0 : email.hashCode) +
        (dataRestriction == null ? 0 : dataRestriction.hashCode);

  factory UserUpdate.fromJson(Map<String, dynamic> json) => _$UserUpdateFromJson(json);

  Map<String, dynamic> toJson() => _$UserUpdateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

