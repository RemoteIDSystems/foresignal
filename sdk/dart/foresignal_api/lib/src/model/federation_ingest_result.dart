//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/federation_ingest_result_dropped.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'federation_ingest_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FederationIngestResult {
  /// Returns a new [FederationIngestResult] instance.
  FederationIngestResult({

    required  this.accepted,

    required  this.duplicates,

    required  this.dropped,
  });

  @JsonKey(
    
    name: r'accepted',
    required: true,
    includeIfNull: false,
  )


  final int accepted;



      /// Already seen (origin_ref, origin_seq)
  @JsonKey(
    
    name: r'duplicates',
    required: true,
    includeIfNull: false,
  )


  final int duplicates;



  @JsonKey(
    
    name: r'dropped',
    required: true,
    includeIfNull: false,
  )


  final FederationIngestResultDropped dropped;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FederationIngestResult &&
      other.accepted == accepted &&
      other.duplicates == duplicates &&
      other.dropped == dropped;

    @override
    int get hashCode =>
        accepted.hashCode +
        duplicates.hashCode +
        dropped.hashCode;

  factory FederationIngestResult.fromJson(Map<String, dynamic> json) => _$FederationIngestResultFromJson(json);

  Map<String, dynamic> toJson() => _$FederationIngestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

