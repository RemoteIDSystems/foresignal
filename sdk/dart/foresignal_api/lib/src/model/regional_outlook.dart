//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/outlook_window.dart';
import 'package:foresignal_api/src/model/regional_outlook_skill.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'regional_outlook.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RegionalOutlook {
  /// Returns a new [RegionalOutlook] instance.
  RegionalOutlook({

    required  this.region,

    required  this.regionLevel,

    required  this.family,

     this.category,

    required  this.publishedAt,

    required  this.modelVersion,

     this.asOfSeq,

     this.manifestId,

    required  this.windows,

     this.history,

     this.skill,

     this.lowReportingDensity,

     this.lowDetectability,

     this.caveats,

     this.recordRef,

     this.id,

     this.source_,
  });

  @JsonKey(
    
    name: r'region',
    required: true,
    includeIfNull: false,
  )


  final String region;



  @JsonKey(
    
    name: r'regionLevel',
    required: true,
    includeIfNull: false,
  )


  final RegionalOutlookRegionLevelEnum regionLevel;



      /// Outlook family from `intel/outlook-families.json`
  @JsonKey(
    
    name: r'family',
    required: true,
    includeIfNull: false,
  )


  final String family;



  @JsonKey(
    
    name: r'category',
    required: false,
    includeIfNull: false,
  )


  final String? category;



  @JsonKey(
    
    name: r'publishedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime publishedAt;



  @JsonKey(
    
    name: r'modelVersion',
    required: true,
    includeIfNull: false,
  )


  final String modelVersion;



  @JsonKey(
    
    name: r'asOfSeq',
    required: false,
    includeIfNull: false,
  )


  final int? asOfSeq;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;



  @JsonKey(
    
    name: r'windows',
    required: true,
    includeIfNull: false,
  )


  final List<OutlookWindow> windows;



      /// Past windows with actuals (detail endpoint only)
  @JsonKey(
    
    name: r'history',
    required: false,
    includeIfNull: false,
  )


  final List<OutlookWindow>? history;



  @JsonKey(
    
    name: r'skill',
    required: false,
    includeIfNull: false,
  )


  final RegionalOutlookSkill? skill;



  @JsonKey(
    
    name: r'lowReportingDensity',
    required: false,
    includeIfNull: false,
  )


  final bool? lowReportingDensity;



  @JsonKey(
    
    name: r'lowDetectability',
    required: false,
    includeIfNull: false,
  )


  final bool? lowDetectability;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'recordRef',
    required: false,
    includeIfNull: false,
  )


  final String? recordRef;



      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



      /// The reference source the counts came from (acled, ucdp, usgs, gdacs or foresignal)
  @JsonKey(
    
    name: r'source',
    required: false,
    includeIfNull: false,
  )


  final String? source_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RegionalOutlook &&
      other.region == region &&
      other.regionLevel == regionLevel &&
      other.family == family &&
      other.category == category &&
      other.publishedAt == publishedAt &&
      other.modelVersion == modelVersion &&
      other.asOfSeq == asOfSeq &&
      other.manifestId == manifestId &&
      other.windows == windows &&
      other.history == history &&
      other.skill == skill &&
      other.lowReportingDensity == lowReportingDensity &&
      other.lowDetectability == lowDetectability &&
      other.caveats == caveats &&
      other.recordRef == recordRef &&
      other.id == id &&
      other.source_ == source_;

    @override
    int get hashCode =>
        region.hashCode +
        regionLevel.hashCode +
        family.hashCode +
        category.hashCode +
        publishedAt.hashCode +
        modelVersion.hashCode +
        asOfSeq.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode) +
        windows.hashCode +
        history.hashCode +
        (skill == null ? 0 : skill.hashCode) +
        lowReportingDensity.hashCode +
        lowDetectability.hashCode +
        caveats.hashCode +
        (recordRef == null ? 0 : recordRef.hashCode) +
        id.hashCode +
        (source_ == null ? 0 : source_.hashCode);

  factory RegionalOutlook.fromJson(Map<String, dynamic> json) => _$RegionalOutlookFromJson(json);

  Map<String, dynamic> toJson() => _$RegionalOutlookToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RegionalOutlookRegionLevelEnum {
@JsonValue(r'country')
country(r'country'),
@JsonValue(r'admin1')
admin1(r'admin1');

const RegionalOutlookRegionLevelEnum(this.value);

final String value;

@override
String toString() => value;
}


