//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportEvent {
  /// Returns a new [ReportEvent] instance.
  ReportEvent({

    required  this.reportRef,

    required  this.title,

    required  this.tlp,

     this.revision,

     this.from,

     this.message,

     this.reason,

    required  this.at,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'reportRef',
    required: true,
    includeIfNull: false,
  )


  final String reportRef;



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
    
    name: r'revision',
    required: false,
    includeIfNull: false,
  )


  final int? revision;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final PrincipalSummary? from;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



      /// Retraction reason
  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportEvent &&
      other.reportRef == reportRef &&
      other.title == title &&
      other.tlp == tlp &&
      other.revision == revision &&
      other.from == from &&
      other.message == message &&
      other.reason == reason &&
      other.at == at;

    @override
    int get hashCode =>
        reportRef.hashCode +
        title.hashCode +
        tlp.hashCode +
        (revision == null ? 0 : revision.hashCode) +
        (from == null ? 0 : from.hashCode) +
        (message == null ? 0 : message.hashCode) +
        (reason == null ? 0 : reason.hashCode) +
        at.hashCode;

  factory ReportEvent.fromJson(Map<String, dynamic> json) => _$ReportEventFromJson(json);

  Map<String, dynamic> toJson() => _$ReportEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

