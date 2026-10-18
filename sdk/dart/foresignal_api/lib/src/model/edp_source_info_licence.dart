//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_source_info_licence.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpSourceInfoLicence {
  /// Returns a new [EdpSourceInfoLicence] instance.
  EdpSourceInfoLicence({

     this.id,

     this.url,

     this.attribution,

     this.redistribution,
  });

  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final String? attribution;



  @JsonKey(
    
    name: r'redistribution',
    required: false,
    includeIfNull: false,
  )


  final EdpSourceInfoLicenceRedistributionEnum? redistribution;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpSourceInfoLicence &&
      other.id == id &&
      other.url == url &&
      other.attribution == attribution &&
      other.redistribution == redistribution;

    @override
    int get hashCode =>
        (id == null ? 0 : id.hashCode) +
        (url == null ? 0 : url.hashCode) +
        (attribution == null ? 0 : attribution.hashCode) +
        redistribution.hashCode;

  factory EdpSourceInfoLicence.fromJson(Map<String, dynamic> json) => _$EdpSourceInfoLicenceFromJson(json);

  Map<String, dynamic> toJson() => _$EdpSourceInfoLicenceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpSourceInfoLicenceRedistributionEnum {
@JsonValue(r'allowed')
allowed(r'allowed'),
@JsonValue(r'local-only')
localOnly(r'local-only'),
@JsonValue(r'unknown')
unknown(r'unknown');

const EdpSourceInfoLicenceRedistributionEnum(this.value);

final String value;

@override
String toString() => value;
}


