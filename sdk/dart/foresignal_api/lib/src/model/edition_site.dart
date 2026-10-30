//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edition_site.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EditionSite {
  /// Returns a new [EditionSite] instance.
  EditionSite({

    required  this.briefingId,

    required  this.url,

     this.feedKeyUrl,

     this.shareUrl,

     this.latestRunId,

     this.updatedAt,
  });

  @JsonKey(
    
    name: r'briefingId',
    required: true,
    includeIfNull: false,
  )


  final String briefingId;



      /// The canonical site URL (`/v1/editions/{briefingId}/site`)
  @JsonKey(
    
    name: r'url',
    required: true,
    includeIfNull: false,
  )


  final String url;



      /// The site under the edition's feed key (`/v1/k/{key}/editions/{briefingId}/site`, 035), or null when it has none
  @JsonKey(
    
    name: r'feedKeyUrl',
    required: false,
    includeIfNull: false,
  )


  final String? feedKeyUrl;



      /// The read-only, revocable share link (112) to the site, or null when it has none
  @JsonKey(
    
    name: r'shareUrl',
    required: false,
    includeIfNull: false,
  )


  final String? shareUrl;



      /// The run the site shows, or null before the first successful run
  @JsonKey(
    
    name: r'latestRunId',
    required: false,
    includeIfNull: false,
  )


  final String? latestRunId;



      /// When that run finished
  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EditionSite &&
      other.briefingId == briefingId &&
      other.url == url &&
      other.feedKeyUrl == feedKeyUrl &&
      other.shareUrl == shareUrl &&
      other.latestRunId == latestRunId &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        briefingId.hashCode +
        url.hashCode +
        (feedKeyUrl == null ? 0 : feedKeyUrl.hashCode) +
        (shareUrl == null ? 0 : shareUrl.hashCode) +
        (latestRunId == null ? 0 : latestRunId.hashCode) +
        (updatedAt == null ? 0 : updatedAt.hashCode);

  factory EditionSite.fromJson(Map<String, dynamic> json) => _$EditionSiteFromJson(json);

  Map<String, dynamic> toJson() => _$EditionSiteToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

