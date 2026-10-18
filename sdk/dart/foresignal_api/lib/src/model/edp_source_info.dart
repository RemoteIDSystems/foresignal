//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/edp_source_info_originator.dart';
import 'package:foresignal_api/src/model/edp_source_info_terms.dart';
import 'package:foresignal_api/src/model/edp_source_info_licence.dart';
import 'package:foresignal_api/src/model/edp_source_info_publisher.dart';
import 'package:foresignal_api/src/model/edp_source_info_access.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_source_info.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpSourceInfo {
  /// Returns a new [EdpSourceInfo] instance.
  EdpSourceInfo({

    required  this.id,

    required  this.name,

     this.description,

     this.version,

    required  this.publisher,

     this.homepage,

     this.licence,

     this.terms,

     this.access,

    required  this.originator,

     this.private = false,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final String? version;



  @JsonKey(
    
    name: r'publisher',
    required: true,
    includeIfNull: false,
  )


  final EdpSourceInfoPublisher publisher;



  @JsonKey(
    
    name: r'homepage',
    required: false,
    includeIfNull: false,
  )


  final String? homepage;



  @JsonKey(
    
    name: r'licence',
    required: false,
    includeIfNull: false,
  )


  final EdpSourceInfoLicence? licence;



  @JsonKey(
    
    name: r'terms',
    required: false,
    includeIfNull: false,
  )


  final EdpSourceInfoTerms? terms;



  @JsonKey(
    
    name: r'access',
    required: false,
    includeIfNull: false,
  )


  final EdpSourceInfoAccess? access;



  @JsonKey(
    
    name: r'originator',
    required: true,
    includeIfNull: false,
  )


  final EdpSourceInfoOriginator originator;



  @JsonKey(
    defaultValue: false,
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpSourceInfo &&
      other.id == id &&
      other.name == name &&
      other.description == description &&
      other.version == version &&
      other.publisher == publisher &&
      other.homepage == homepage &&
      other.licence == licence &&
      other.terms == terms &&
      other.access == access &&
      other.originator == originator &&
      other.private == private;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        (description == null ? 0 : description.hashCode) +
        (version == null ? 0 : version.hashCode) +
        publisher.hashCode +
        (homepage == null ? 0 : homepage.hashCode) +
        licence.hashCode +
        terms.hashCode +
        access.hashCode +
        originator.hashCode +
        private.hashCode;

  factory EdpSourceInfo.fromJson(Map<String, dynamic> json) => _$EdpSourceInfoFromJson(json);

  Map<String, dynamic> toJson() => _$EdpSourceInfoToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

