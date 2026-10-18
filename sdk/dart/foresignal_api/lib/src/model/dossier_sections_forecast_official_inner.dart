//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dossier_sections_forecast_official_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DossierSectionsForecastOfficialInner {
  /// Returns a new [DossierSectionsForecastOfficialInner] instance.
  DossierSectionsForecastOfficialInner({

    required  this.ref,

     this.title,

     this.kind,

     this.issuer = 'official',

     this.issuedAt,

     this.validFrom,

     this.validTo,

     this.note,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



  @JsonKey(
    defaultValue: 'official',
    name: r'issuer',
    required: true,
    includeIfNull: false,
  )


  final String issuer;



  @JsonKey(
    
    name: r'issuedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? issuedAt;



  @JsonKey(
    
    name: r'validFrom',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validFrom;



  @JsonKey(
    
    name: r'validTo',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validTo;



  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DossierSectionsForecastOfficialInner &&
      other.ref == ref &&
      other.title == title &&
      other.kind == kind &&
      other.issuer == issuer &&
      other.issuedAt == issuedAt &&
      other.validFrom == validFrom &&
      other.validTo == validTo &&
      other.note == note;

    @override
    int get hashCode =>
        ref.hashCode +
        title.hashCode +
        kind.hashCode +
        issuer.hashCode +
        (issuedAt == null ? 0 : issuedAt.hashCode) +
        (validFrom == null ? 0 : validFrom.hashCode) +
        (validTo == null ? 0 : validTo.hashCode) +
        (note == null ? 0 : note.hashCode);

  factory DossierSectionsForecastOfficialInner.fromJson(Map<String, dynamic> json) => _$DossierSectionsForecastOfficialInnerFromJson(json);

  Map<String, dynamic> toJson() => _$DossierSectionsForecastOfficialInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

