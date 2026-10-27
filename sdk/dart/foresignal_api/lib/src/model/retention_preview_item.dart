//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'retention_preview_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RetentionPreviewItem {
  /// Returns a new [RetentionPreviewItem] instance.
  RetentionPreviewItem({

    required  this.subsystem,

     this.kind,

     this.cutoff,

    required  this.affectedCount,

     this.bytesEstimate,
  });

      /// `records` (STANDARD history, per kind), `track` (TRACK history leaves), `frames`, `media` or `chronicles` (stopped or archived chronicles whose history would be removed)
  @JsonKey(
    
    name: r'subsystem',
    required: true,
    includeIfNull: false,
  )


  final String subsystem;



      /// The record kind of a `records` item; null for whole-source scopes and other subsystems
  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



      /// Oldest data kept (for a `records` kind, under its longest window); null when no single cutoff applies
  @JsonKey(
    
    name: r'cutoff',
    required: false,
    includeIfNull: false,
  )


  final DateTime? cutoff;



  @JsonKey(
    
    name: r'affectedCount',
    required: true,
    includeIfNull: false,
  )


  final int affectedCount;



      /// Estimated bytes: records from the average row size of record_version, TRACK leaves and frames from their sizes, media from its rows, chronicles from their bytesUsed
  @JsonKey(
    
    name: r'bytesEstimate',
    required: false,
    includeIfNull: false,
  )


  final int? bytesEstimate;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RetentionPreviewItem &&
      other.subsystem == subsystem &&
      other.kind == kind &&
      other.cutoff == cutoff &&
      other.affectedCount == affectedCount &&
      other.bytesEstimate == bytesEstimate;

    @override
    int get hashCode =>
        subsystem.hashCode +
        (kind == null ? 0 : kind.hashCode) +
        (cutoff == null ? 0 : cutoff.hashCode) +
        affectedCount.hashCode +
        (bytesEstimate == null ? 0 : bytesEstimate.hashCode);

  factory RetentionPreviewItem.fromJson(Map<String, dynamic> json) => _$RetentionPreviewItemFromJson(json);

  Map<String, dynamic> toJson() => _$RetentionPreviewItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

