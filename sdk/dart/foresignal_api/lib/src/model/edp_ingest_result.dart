//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ingest_outcome.dart';
import 'package:foresignal_api/src/model/edp_item_rejection.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_ingest_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpIngestResult {
  /// Returns a new [EdpIngestResult] instance.
  EdpIngestResult({

    required  this.accepted,

    required  this.rejected,

     this.outcome,
  });

  @JsonKey(
    
    name: r'accepted',
    required: true,
    includeIfNull: false,
  )


  final int accepted;



  @JsonKey(
    
    name: r'rejected',
    required: true,
    includeIfNull: false,
  )


  final List<EdpItemRejection> rejected;



  @JsonKey(
    
    name: r'outcome',
    required: false,
    includeIfNull: false,
  )


  final IngestOutcome? outcome;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpIngestResult &&
      other.accepted == accepted &&
      other.rejected == rejected &&
      other.outcome == outcome;

    @override
    int get hashCode =>
        accepted.hashCode +
        rejected.hashCode +
        outcome.hashCode;

  factory EdpIngestResult.fromJson(Map<String, dynamic> json) => _$EdpIngestResultFromJson(json);

  Map<String, dynamic> toJson() => _$EdpIngestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

