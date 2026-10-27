//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/permission.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'permission_list.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PermissionList {
  /// Returns a new [PermissionList] instance.
  PermissionList({

    required  this.items,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<Permission> items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PermissionList &&
      other.items == items;

    @override
    int get hashCode =>
        items.hashCode;

  factory PermissionList.fromJson(Map<String, dynamic> json) => _$PermissionListFromJson(json);

  Map<String, dynamic> toJson() => _$PermissionListToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

