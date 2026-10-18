//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'package_manifest_authors_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PackageManifestAuthorsInner {
  /// Returns a new [PackageManifestAuthorsInner] instance.
  PackageManifestAuthorsInner({

    required  this.handle,

     this.displayName,

    required  this.instanceId,

    required  this.role,
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



  @JsonKey(
    
    name: r'instanceId',
    required: true,
    includeIfNull: false,
  )


  final String instanceId;



  @JsonKey(
    
    name: r'role',
    required: true,
    includeIfNull: false,
  )


  final PackageManifestAuthorsInnerRoleEnum role;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PackageManifestAuthorsInner &&
      other.handle == handle &&
      other.displayName == displayName &&
      other.instanceId == instanceId &&
      other.role == role;

    @override
    int get hashCode =>
        handle.hashCode +
        displayName.hashCode +
        instanceId.hashCode +
        role.hashCode;

  factory PackageManifestAuthorsInner.fromJson(Map<String, dynamic> json) => _$PackageManifestAuthorsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$PackageManifestAuthorsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PackageManifestAuthorsInnerRoleEnum {
@JsonValue(r'author')
author(r'author'),
@JsonValue(r'forwarder')
forwarder(r'forwarder');

const PackageManifestAuthorsInnerRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


