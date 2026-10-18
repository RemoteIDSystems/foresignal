//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/report.dart';
import 'package:foresignal_api/src/model/report_bundle_attachments_inner.dart';
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_bundle.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportBundle {
  /// Returns a new [ReportBundle] instance.
  ReportBundle({

    required  this.report,

    required  this.revision,

    required  this.tlp,

    required  this.envelopes,

     this.snapshots,

     this.attachments,

     this.manifestId,

     this.missing,
  });

  @JsonKey(
    
    name: r'report',
    required: true,
    includeIfNull: false,
  )


  final Report report;



  @JsonKey(
    
    name: r'revision',
    required: true,
    includeIfNull: false,
  )


  final int revision;



  @JsonKey(
    
    name: r'tlp',
    required: true,
    includeIfNull: false,
  )


  final Tlp tlp;



  @JsonKey(
    
    name: r'envelopes',
    required: true,
    includeIfNull: false,
  )


  final List<ObjectEnvelope> envelopes;



      /// Pinned ref → snapshot
  @JsonKey(
    
    name: r'snapshots',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? snapshots;



  @JsonKey(
    
    name: r'attachments',
    required: false,
    includeIfNull: false,
  )


  final List<ReportBundleAttachmentsInner>? attachments;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;



      /// Cited references the publisher could not resolve (missing or not visible), left out of the revision
  @JsonKey(
    
    name: r'missing',
    required: false,
    includeIfNull: false,
  )


  final List<String>? missing;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportBundle &&
      other.report == report &&
      other.revision == revision &&
      other.tlp == tlp &&
      other.envelopes == envelopes &&
      other.snapshots == snapshots &&
      other.attachments == attachments &&
      other.manifestId == manifestId &&
      other.missing == missing;

    @override
    int get hashCode =>
        report.hashCode +
        revision.hashCode +
        tlp.hashCode +
        envelopes.hashCode +
        snapshots.hashCode +
        attachments.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode) +
        missing.hashCode;

  factory ReportBundle.fromJson(Map<String, dynamic> json) => _$ReportBundleFromJson(json);

  Map<String, dynamic> toJson() => _$ReportBundleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

