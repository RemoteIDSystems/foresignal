//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/report_revision_renditions.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_revision.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportRevision {
  /// Returns a new [ReportRevision] instance.
  ReportRevision({

    required  this.reportId,

    required  this.revision,

    required  this.title,

    required  this.tlp,

    required  this.status,

    required  this.publishedAt,

     this.publishedBy,

     this.retractedAt,

     this.retractionReason,

     this.manifestId,

     this.pinnedRefs,

     this.bundleSha256,

     this.renditions,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'reportId',
    required: true,
    includeIfNull: false,
  )


  final String reportId;



  @JsonKey(
    
    name: r'revision',
    required: true,
    includeIfNull: false,
  )


  final int revision;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'tlp',
    required: true,
    includeIfNull: false,
  )


  final Tlp tlp;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ReportRevisionStatusEnum status;



  @JsonKey(
    
    name: r'publishedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime publishedAt;



  @JsonKey(
    
    name: r'publishedBy',
    required: false,
    includeIfNull: false,
  )


  final PrincipalSummary? publishedBy;



  @JsonKey(
    
    name: r'retractedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? retractedAt;



  @JsonKey(
    
    name: r'retractionReason',
    required: false,
    includeIfNull: false,
  )


  final String? retractionReason;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;



  @JsonKey(
    
    name: r'pinnedRefs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? pinnedRefs;



  @JsonKey(
    
    name: r'bundleSha256',
    required: false,
    includeIfNull: false,
  )


  final String? bundleSha256;



  @JsonKey(
    
    name: r'renditions',
    required: false,
    includeIfNull: false,
  )


  final ReportRevisionRenditions? renditions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportRevision &&
      other.reportId == reportId &&
      other.revision == revision &&
      other.title == title &&
      other.tlp == tlp &&
      other.status == status &&
      other.publishedAt == publishedAt &&
      other.publishedBy == publishedBy &&
      other.retractedAt == retractedAt &&
      other.retractionReason == retractionReason &&
      other.manifestId == manifestId &&
      other.pinnedRefs == pinnedRefs &&
      other.bundleSha256 == bundleSha256 &&
      other.renditions == renditions;

    @override
    int get hashCode =>
        reportId.hashCode +
        revision.hashCode +
        title.hashCode +
        tlp.hashCode +
        status.hashCode +
        publishedAt.hashCode +
        publishedBy.hashCode +
        (retractedAt == null ? 0 : retractedAt.hashCode) +
        (retractionReason == null ? 0 : retractionReason.hashCode) +
        (manifestId == null ? 0 : manifestId.hashCode) +
        pinnedRefs.hashCode +
        bundleSha256.hashCode +
        renditions.hashCode;

  factory ReportRevision.fromJson(Map<String, dynamic> json) => _$ReportRevisionFromJson(json);

  Map<String, dynamic> toJson() => _$ReportRevisionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReportRevisionStatusEnum {
@JsonValue(r'published')
published(r'published'),
@JsonValue(r'retracted')
retracted(r'retracted');

const ReportRevisionStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


