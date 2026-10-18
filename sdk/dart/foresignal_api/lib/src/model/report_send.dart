//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/report_send_recipients_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_send.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportSend {
  /// Returns a new [ReportSend] instance.
  ReportSend({

    required  this.id,

    required  this.reportId,

     this.revision,

    required  this.sentBy,

    required  this.sentAt,

     this.message,

    required  this.recipients,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// ULID identifier
  @JsonKey(
    
    name: r'reportId',
    required: true,
    includeIfNull: false,
  )


  final String reportId;



  @JsonKey(
    
    name: r'revision',
    required: false,
    includeIfNull: false,
  )


  final int? revision;



  @JsonKey(
    
    name: r'sentBy',
    required: true,
    includeIfNull: false,
  )


  final PrincipalSummary sentBy;



  @JsonKey(
    
    name: r'sentAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime sentAt;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



  @JsonKey(
    
    name: r'recipients',
    required: true,
    includeIfNull: false,
  )


  final List<ReportSendRecipientsInner> recipients;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportSend &&
      other.id == id &&
      other.reportId == reportId &&
      other.revision == revision &&
      other.sentBy == sentBy &&
      other.sentAt == sentAt &&
      other.message == message &&
      other.recipients == recipients;

    @override
    int get hashCode =>
        id.hashCode +
        reportId.hashCode +
        (revision == null ? 0 : revision.hashCode) +
        sentBy.hashCode +
        sentAt.hashCode +
        (message == null ? 0 : message.hashCode) +
        recipients.hashCode;

  factory ReportSend.fromJson(Map<String, dynamic> json) => _$ReportSendFromJson(json);

  Map<String, dynamic> toJson() => _$ReportSendToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

