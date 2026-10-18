//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'media_provider.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MediaProvider {
  /// Returns a new [MediaProvider] instance.
  MediaProvider({

    required  this.id,

    required  this.name,

    required  this.enabled,

     this.defaultEnabled,

    required  this.licence,

     this.licenceUrl,

     this.attribution,

    required  this.licenceAcknowledged,

     this.acknowledgedBy,

     this.acknowledgedAt,

    required  this.egress,

     this.permissionRecorded,

     this.credentialSet,

     this.lastImportAt,

     this.lastImportStatus,

     this.ratingsCount,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final MediaProviderIdEnum id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'defaultEnabled',
    required: false,
    includeIfNull: false,
  )


  final bool? defaultEnabled;



  @JsonKey(
    
    name: r'licence',
    required: true,
    includeIfNull: false,
  )


  final String licence;



  @JsonKey(
    
    name: r'licenceUrl',
    required: false,
    includeIfNull: false,
  )


  final String? licenceUrl;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final String? attribution;



  @JsonKey(
    
    name: r'licenceAcknowledged',
    required: true,
    includeIfNull: false,
  )


  final bool licenceAcknowledged;



  @JsonKey(
    
    name: r'acknowledgedBy',
    required: false,
    includeIfNull: false,
  )


  final String? acknowledgedBy;



  @JsonKey(
    
    name: r'acknowledgedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? acknowledgedAt;



  @JsonKey(
    
    name: r'egress',
    required: true,
    includeIfNull: false,
  )


  final MediaProviderEgressEnum egress;



      /// Operator recorded redistribution permission (lifts LOCAL_ONLY where terms allow)
  @JsonKey(
    
    name: r'permissionRecorded',
    required: false,
    includeIfNull: false,
  )


  final bool? permissionRecorded;



      /// MBFC: operator API key stored
  @JsonKey(
    
    name: r'credentialSet',
    required: false,
    includeIfNull: false,
  )


  final bool? credentialSet;



  @JsonKey(
    
    name: r'lastImportAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastImportAt;



  @JsonKey(
    
    name: r'lastImportStatus',
    required: false,
    includeIfNull: false,
  )


  final MediaProviderLastImportStatusEnum? lastImportStatus;



  @JsonKey(
    
    name: r'ratingsCount',
    required: false,
    includeIfNull: false,
  )


  final int? ratingsCount;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MediaProvider &&
      other.id == id &&
      other.name == name &&
      other.enabled == enabled &&
      other.defaultEnabled == defaultEnabled &&
      other.licence == licence &&
      other.licenceUrl == licenceUrl &&
      other.attribution == attribution &&
      other.licenceAcknowledged == licenceAcknowledged &&
      other.acknowledgedBy == acknowledgedBy &&
      other.acknowledgedAt == acknowledgedAt &&
      other.egress == egress &&
      other.permissionRecorded == permissionRecorded &&
      other.credentialSet == credentialSet &&
      other.lastImportAt == lastImportAt &&
      other.lastImportStatus == lastImportStatus &&
      other.ratingsCount == ratingsCount;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        enabled.hashCode +
        defaultEnabled.hashCode +
        licence.hashCode +
        (licenceUrl == null ? 0 : licenceUrl.hashCode) +
        (attribution == null ? 0 : attribution.hashCode) +
        licenceAcknowledged.hashCode +
        (acknowledgedBy == null ? 0 : acknowledgedBy.hashCode) +
        (acknowledgedAt == null ? 0 : acknowledgedAt.hashCode) +
        egress.hashCode +
        permissionRecorded.hashCode +
        credentialSet.hashCode +
        (lastImportAt == null ? 0 : lastImportAt.hashCode) +
        (lastImportStatus == null ? 0 : lastImportStatus.hashCode) +
        ratingsCount.hashCode;

  factory MediaProvider.fromJson(Map<String, dynamic> json) => _$MediaProviderFromJson(json);

  Map<String, dynamic> toJson() => _$MediaProviderToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MediaProviderIdEnum {
@JsonValue(r'wikidata')
wikidata(r'wikidata'),
@JsonValue(r'partisan-web')
partisanWeb(r'partisan-web'),
@JsonValue(r'allsides')
allsides(r'allsides'),
@JsonValue(r'mbfc')
mbfc(r'mbfc'),
@JsonValue(r'operator')
operator_(r'operator');

const MediaProviderIdEnum(this.value);

final String value;

@override
String toString() => value;
}


enum MediaProviderEgressEnum {
@JsonValue(r'OPEN')
OPEN(r'OPEN'),
@JsonValue(r'LOCAL_ONLY')
LOCAL_ONLY(r'LOCAL_ONLY');

const MediaProviderEgressEnum(this.value);

final String value;

@override
String toString() => value;
}


enum MediaProviderLastImportStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'partial')
partial(r'partial'),
@JsonValue(r'failed')
failed(r'failed');

const MediaProviderLastImportStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


