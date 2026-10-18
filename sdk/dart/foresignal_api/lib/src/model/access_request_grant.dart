//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'access_request_grant.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AccessRequestGrant {
  /// Returns a new [AccessRequestGrant] instance.
  AccessRequestGrant({

    required  this.deliveredAs,

     this.role,

     this.packageId,
  });

  @JsonKey(
    
    name: r'deliveredAs',
    required: true,
    includeIfNull: false,
  )


  final AccessRequestGrantDeliveredAsEnum deliveredAs;



  @JsonKey(
    
    name: r'role',
    required: false,
    includeIfNull: false,
  )


  final AccessRequestGrantRoleEnum? role;



      /// ULID identifier
  @JsonKey(
    
    name: r'packageId',
    required: false,
    includeIfNull: false,
  )


  final String? packageId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AccessRequestGrant &&
      other.deliveredAs == deliveredAs &&
      other.role == role &&
      other.packageId == packageId;

    @override
    int get hashCode =>
        deliveredAs.hashCode +
        (role == null ? 0 : role.hashCode) +
        (packageId == null ? 0 : packageId.hashCode);

  factory AccessRequestGrant.fromJson(Map<String, dynamic> json) => _$AccessRequestGrantFromJson(json);

  Map<String, dynamic> toJson() => _$AccessRequestGrantToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AccessRequestGrantDeliveredAsEnum {
@JsonValue(r'acl')
acl(r'acl'),
@JsonValue(r'report')
report(r'report'),
@JsonValue(r'chronicle-extract')
chronicleExtract(r'chronicle-extract');

const AccessRequestGrantDeliveredAsEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AccessRequestGrantRoleEnum {
@JsonValue(r'viewer')
viewer(r'viewer'),
@JsonValue(r'commenter')
commenter(r'commenter');

const AccessRequestGrantRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


