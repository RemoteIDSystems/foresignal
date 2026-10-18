//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'purge_report_citing_documents.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PurgeReportCitingDocuments {
  /// Returns a new [PurgeReportCitingDocuments] instance.
  PurgeReportCitingDocuments({

     this.askMessages,

     this.briefingRuns,
  });

  @JsonKey(
    
    name: r'askMessages',
    required: false,
    includeIfNull: false,
  )


  final int? askMessages;



  @JsonKey(
    
    name: r'briefingRuns',
    required: false,
    includeIfNull: false,
  )


  final int? briefingRuns;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PurgeReportCitingDocuments &&
      other.askMessages == askMessages &&
      other.briefingRuns == briefingRuns;

    @override
    int get hashCode =>
        askMessages.hashCode +
        briefingRuns.hashCode;

  factory PurgeReportCitingDocuments.fromJson(Map<String, dynamic> json) => _$PurgeReportCitingDocumentsFromJson(json);

  Map<String, dynamic> toJson() => _$PurgeReportCitingDocumentsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

