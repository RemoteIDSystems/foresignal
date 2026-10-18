//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'licence_terms.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LicenceTerms {
  /// Returns a new [LicenceTerms] instance.
  LicenceTerms({

    required  this.id,

    required  this.name,

    required  this.commercialUse,

    required  this.shareAlike,

    required  this.attributionRequired,

    required  this.redistribution,

     this.attributionText,

     this.termsUrl,
  });

      /// SPDX id where one exists (CC-BY-4.0, ODbL-1.0, CC0-1.0, CC-BY-NC-SA-4.0, CC-BY-SA-4.0, CC-BY-NC-4.0, PDDL-1.0, OGL-UK-3.0, NLOD-2.0, DL-DE-BY-2.0), else a slug (us-gov-public-domain, opensky-terms, acled-terms, aisstream-terms, unknown)
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



      /// UNKNOWN is treated as FORBIDDEN by every egress guard
  @JsonKey(
    
    name: r'commercialUse',
    required: true,
    includeIfNull: false,
  )


  final LicenceTermsCommercialUseEnum commercialUse;



  @JsonKey(
    
    name: r'shareAlike',
    required: true,
    includeIfNull: false,
  )


  final bool shareAlike;



  @JsonKey(
    
    name: r'attributionRequired',
    required: true,
    includeIfNull: false,
  )


  final bool attributionRequired;



      /// UNKNOWN is treated as LOCAL_ONLY by every egress guard
  @JsonKey(
    
    name: r'redistribution',
    required: true,
    includeIfNull: false,
  )


  final LicenceTermsRedistributionEnum redistribution;



  @JsonKey(
    
    name: r'attributionText',
    required: false,
    includeIfNull: false,
  )


  final String? attributionText;



  @JsonKey(
    
    name: r'termsUrl',
    required: false,
    includeIfNull: false,
  )


  final String? termsUrl;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LicenceTerms &&
      other.id == id &&
      other.name == name &&
      other.commercialUse == commercialUse &&
      other.shareAlike == shareAlike &&
      other.attributionRequired == attributionRequired &&
      other.redistribution == redistribution &&
      other.attributionText == attributionText &&
      other.termsUrl == termsUrl;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        commercialUse.hashCode +
        shareAlike.hashCode +
        attributionRequired.hashCode +
        redistribution.hashCode +
        (attributionText == null ? 0 : attributionText.hashCode) +
        (termsUrl == null ? 0 : termsUrl.hashCode);

  factory LicenceTerms.fromJson(Map<String, dynamic> json) => _$LicenceTermsFromJson(json);

  Map<String, dynamic> toJson() => _$LicenceTermsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// UNKNOWN is treated as FORBIDDEN by every egress guard
enum LicenceTermsCommercialUseEnum {
@JsonValue(r'ALLOWED')
ALLOWED(r'ALLOWED'),
@JsonValue(r'FORBIDDEN')
FORBIDDEN(r'FORBIDDEN'),
@JsonValue(r'UNKNOWN')
UNKNOWN(r'UNKNOWN');

const LicenceTermsCommercialUseEnum(this.value);

final String value;

@override
String toString() => value;
}


/// UNKNOWN is treated as LOCAL_ONLY by every egress guard
enum LicenceTermsRedistributionEnum {
@JsonValue(r'ALLOWED')
ALLOWED(r'ALLOWED'),
@JsonValue(r'LOCAL_ONLY')
LOCAL_ONLY(r'LOCAL_ONLY'),
@JsonValue(r'FORBIDDEN')
FORBIDDEN(r'FORBIDDEN'),
@JsonValue(r'UNKNOWN')
UNKNOWN(r'UNKNOWN');

const LicenceTermsRedistributionEnum(this.value);

final String value;

@override
String toString() => value;
}


