//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/edp_item.dart';
import 'package:foresignal_api/src/model/edp_snapshot.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_batch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpBatch {
  /// Returns a new [EdpBatch] instance.
  EdpBatch({

    required  this.protocol,

     this.sourceId,

     this.sentAt,

    required  this.items,

     this.nextCursor,

     this.snapshot,
  });

      /// foresignal-edp/1.N; N must not exceed the highest minor the instance supports (GET /v1/external/protocol), else PROTOCOL_UNSUPPORTED
  @JsonKey(
    
    name: r'protocol',
    required: true,
    includeIfNull: false,
  )


  final String protocol;



      /// Optional. The source is identified by the push URL and key (or the pull registration); when present this must equal the registered source id (400 SOURCE_MISMATCH).
  @JsonKey(
    
    name: r'sourceId',
    required: false,
    includeIfNull: false,
  )


  final String? sourceId;



  @JsonKey(
    
    name: r'sentAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? sentAt;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<EdpItem> items;



  @JsonKey(
    
    name: r'nextCursor',
    required: false,
    includeIfNull: false,
  )


  final String? nextCursor;



  @JsonKey(
    
    name: r'snapshot',
    required: false,
    includeIfNull: false,
  )


  final EdpSnapshot? snapshot;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpBatch &&
      other.protocol == protocol &&
      other.sourceId == sourceId &&
      other.sentAt == sentAt &&
      other.items == items &&
      other.nextCursor == nextCursor &&
      other.snapshot == snapshot;

    @override
    int get hashCode =>
        protocol.hashCode +
        sourceId.hashCode +
        sentAt.hashCode +
        items.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode) +
        (snapshot == null ? 0 : snapshot.hashCode);

  factory EdpBatch.fromJson(Map<String, dynamic> json) => _$EdpBatchFromJson(json);

  Map<String, dynamic> toJson() => _$EdpBatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

