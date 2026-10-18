//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'media_rating.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MediaRating {
  /// Returns a new [MediaRating] instance.
  MediaRating({

    required  this.outletId,

    required  this.provider,

    required  this.dimension,

     this.valueNorm,

     this.valueRaw,

     this.label,

     this.ratedAt,

    required  this.licence,

    required  this.attribution,

     this.url,

    required  this.egress,

    required  this.importedAt,
  });

  @JsonKey(
    
    name: r'outletId',
    required: true,
    includeIfNull: false,
  )


  final String outletId;



  @JsonKey(
    
    name: r'provider',
    required: true,
    includeIfNull: false,
  )


  final MediaRatingProviderEnum provider;



      /// Rating dimension; provider axes that are not left–right are recorded as `{provider}:{axis}`, never forced into lean
  @JsonKey(
    
    name: r'dimension',
    required: true,
    includeIfNull: true,
  )


  final Object? dimension;



      /// Lean −2…+2 (left … right); factuality 1…5
  @JsonKey(
    
    name: r'valueNorm',
    required: false,
    includeIfNull: false,
  )


  final num? valueNorm;



  @JsonKey(
    
    name: r'valueRaw',
    required: false,
    includeIfNull: false,
  )


  final String? valueRaw;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;



  @JsonKey(
    
    name: r'ratedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? ratedAt;



  @JsonKey(
    
    name: r'licence',
    required: true,
    includeIfNull: false,
  )


  final String licence;



  @JsonKey(
    
    name: r'attribution',
    required: true,
    includeIfNull: false,
  )


  final String attribution;



  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'egress',
    required: true,
    includeIfNull: false,
  )


  final MediaRatingEgressEnum egress;



  @JsonKey(
    
    name: r'importedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime importedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MediaRating &&
      other.outletId == outletId &&
      other.provider == provider &&
      other.dimension == dimension &&
      other.valueNorm == valueNorm &&
      other.valueRaw == valueRaw &&
      other.label == label &&
      other.ratedAt == ratedAt &&
      other.licence == licence &&
      other.attribution == attribution &&
      other.url == url &&
      other.egress == egress &&
      other.importedAt == importedAt;

    @override
    int get hashCode =>
        outletId.hashCode +
        provider.hashCode +
        (dimension == null ? 0 : dimension.hashCode) +
        (valueNorm == null ? 0 : valueNorm.hashCode) +
        (valueRaw == null ? 0 : valueRaw.hashCode) +
        (label == null ? 0 : label.hashCode) +
        (ratedAt == null ? 0 : ratedAt.hashCode) +
        licence.hashCode +
        attribution.hashCode +
        (url == null ? 0 : url.hashCode) +
        egress.hashCode +
        importedAt.hashCode;

  factory MediaRating.fromJson(Map<String, dynamic> json) => _$MediaRatingFromJson(json);

  Map<String, dynamic> toJson() => _$MediaRatingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MediaRatingProviderEnum {
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

const MediaRatingProviderEnum(this.value);

final String value;

@override
String toString() => value;
}


enum MediaRatingEgressEnum {
@JsonValue(r'OPEN')
OPEN(r'OPEN'),
@JsonValue(r'LOCAL_ONLY')
LOCAL_ONLY(r'LOCAL_ONLY');

const MediaRatingEgressEnum(this.value);

final String value;

@override
String toString() => value;
}


