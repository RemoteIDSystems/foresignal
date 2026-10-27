//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'user_delete_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UserDeleteResult {
  /// Returns a new [UserDeleteResult] instance.
  UserDeleteResult({

    required  this.deletedUserId,

    required  this.ownedConfigDisposition,

     this.reassignedTo,
  });

  @JsonKey(
    
    name: r'deletedUserId',
    required: true,
    includeIfNull: false,
  )


  final String deletedUserId;



      /// Default reassigns the user's owned triggers, channels, providers and views to an admin
  @JsonKey(
    
    name: r'ownedConfigDisposition',
    required: true,
    includeIfNull: false,
  )


  final String ownedConfigDisposition;



  @JsonKey(
    
    name: r'reassignedTo',
    required: false,
    includeIfNull: false,
  )


  final String? reassignedTo;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UserDeleteResult &&
      other.deletedUserId == deletedUserId &&
      other.ownedConfigDisposition == ownedConfigDisposition &&
      other.reassignedTo == reassignedTo;

    @override
    int get hashCode =>
        deletedUserId.hashCode +
        ownedConfigDisposition.hashCode +
        (reassignedTo == null ? 0 : reassignedTo.hashCode);

  factory UserDeleteResult.fromJson(Map<String, dynamic> json) => _$UserDeleteResultFromJson(json);

  Map<String, dynamic> toJson() => _$UserDeleteResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

