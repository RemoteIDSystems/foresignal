//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/purge_report_citing_documents.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'purge_report.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PurgeReport {
  /// Returns a new [PurgeReport] instance.
  PurgeReport({

    required  this.tombstoneSeq,

    required  this.versionsDeleted,

     this.alertsRedacted,

     this.alertsDeleted,

     this.embeddingsDeleted,

     this.notificationsRedacted,

     this.citingDocuments,
  });

  @JsonKey(
    
    name: r'tombstoneSeq',
    required: true,
    includeIfNull: false,
  )


  final int tombstoneSeq;



  @JsonKey(
    
    name: r'versionsDeleted',
    required: true,
    includeIfNull: false,
  )


  final int versionsDeleted;



  @JsonKey(
    
    name: r'alertsRedacted',
    required: false,
    includeIfNull: false,
  )


  final int? alertsRedacted;



  @JsonKey(
    
    name: r'alertsDeleted',
    required: false,
    includeIfNull: false,
  )


  final int? alertsDeleted;



  @JsonKey(
    
    name: r'embeddingsDeleted',
    required: false,
    includeIfNull: false,
  )


  final int? embeddingsDeleted;



  @JsonKey(
    
    name: r'notificationsRedacted',
    required: false,
    includeIfNull: false,
  )


  final int? notificationsRedacted;



  @JsonKey(
    
    name: r'citingDocuments',
    required: false,
    includeIfNull: false,
  )


  final PurgeReportCitingDocuments? citingDocuments;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PurgeReport &&
      other.tombstoneSeq == tombstoneSeq &&
      other.versionsDeleted == versionsDeleted &&
      other.alertsRedacted == alertsRedacted &&
      other.alertsDeleted == alertsDeleted &&
      other.embeddingsDeleted == embeddingsDeleted &&
      other.notificationsRedacted == notificationsRedacted &&
      other.citingDocuments == citingDocuments;

    @override
    int get hashCode =>
        tombstoneSeq.hashCode +
        versionsDeleted.hashCode +
        alertsRedacted.hashCode +
        alertsDeleted.hashCode +
        embeddingsDeleted.hashCode +
        notificationsRedacted.hashCode +
        citingDocuments.hashCode;

  factory PurgeReport.fromJson(Map<String, dynamic> json) => _$PurgeReportFromJson(json);

  Map<String, dynamic> toJson() => _$PurgeReportToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

