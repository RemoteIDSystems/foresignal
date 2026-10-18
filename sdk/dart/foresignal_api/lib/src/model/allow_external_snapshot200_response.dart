//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ingest_outcome.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'allow_external_snapshot200_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AllowExternalSnapshot200Response {
  /// Returns a new [AllowExternalSnapshot200Response] instance.
  AllowExternalSnapshot200Response({

    required  this.snapshotId,

    required  this.removed,

    required  this.outcome,
  });

  @JsonKey(
    
    name: r'snapshotId',
    required: true,
    includeIfNull: false,
  )


  final String snapshotId;



      /// Absence tombstones ingested
  @JsonKey(
    
    name: r'removed',
    required: true,
    includeIfNull: false,
  )


  final int removed;



  @JsonKey(
    
    name: r'outcome',
    required: true,
    includeIfNull: false,
  )


  final IngestOutcome outcome;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AllowExternalSnapshot200Response &&
      other.snapshotId == snapshotId &&
      other.removed == removed &&
      other.outcome == outcome;

    @override
    int get hashCode =>
        snapshotId.hashCode +
        removed.hashCode +
        outcome.hashCode;

  factory AllowExternalSnapshot200Response.fromJson(Map<String, dynamic> json) => _$AllowExternalSnapshot200ResponseFromJson(json);

  Map<String, dynamic> toJson() => _$AllowExternalSnapshot200ResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

